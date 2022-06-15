package com.dreamlibrary.storyapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.login.LoginManager;
import com.folioreader.util.RecordPref;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.database.DatabaseHandler;
import com.dreamlibrary.storyapp.interfaces.FavouriteIF;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.DownloadList;
import com.dreamlibrary.storyapp.response.FavouriteRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.service.DownloadService;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Method {

    public static boolean loginBack = false, personalizationAd = false, isDownload = true;
    private final String myPreference = "login";
    private final String firstTime = "firstTime";
    public Activity activity;
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    public String pref_login = "pref_login";
    public String profileId = "profileId";
    public String profileName = "profileName";
    public String userImage = "userImage";
    public String loginType = "loginType";
    public String show_login = "show_login";
    public String notification = "notification";
    public String themSetting = "them";
    private final RecordPref recordPref;
    private String filename;
    private OnClick onClick;

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity) {
        this.activity = activity;
        recordPref = new RecordPref(activity);
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity, OnClick onClick) {
        this.activity = activity;
        this.onClick = onClick;
        recordPref = new RecordPref(activity);
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    // view format
    public static String Format(Integer number) {
        String[] suffix = new String[]{"K", "M", "B", "T"};
        int size = (number != 0) ? (int) Math.log10(number) : 0;
        if (size >= 3) {
            while (size % 3 != 0) {
                size = size - 1;
            }
        }
        double notation = Math.pow(10, size);
        String result = (size >= 3) ? +(Math.round((number / notation) * 100) / 100.0d) + suffix[(size / 3) - 1] : +number + "";
        return result;
    }

    public static boolean deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            return deleteDir(dir);
        } catch (Exception e) {
            Log.d("Cache Size", e.getMessage());
        }
        return false;
    }

    public static String getCache(Context context) {
        long size = 0;
        File[] files = context.getCacheDir().listFiles();
        if (files != null) {
            for (File f : files) {
                size = size + f.length();
            }
        }
        return readableFileSize(size);
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    //For printing larger log
    public static void largeLogcat(String tag, String content) {
        if (content.length() > 4000) {
            Log.d(tag, content.substring(0, 4000));
            largeLogcat(tag, content.substring(4000));
        } else {
            Log.d(tag, content);
        }
    }

    public int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public void login() {
        if (!pref.getBoolean(firstTime, false)) {
            editor.putBoolean(pref_login, false);
            editor.putBoolean(firstTime, true);
            editor.commit();
        }
    }

    public int getPercentage(int readPage, int totalPage) {

        if (readPage != 0 && totalPage != 0) {
            return (readPage * 100) / totalPage;
        }

        return 0;
    }

    //user login or not
    public boolean isLogin() {
        return pref.getBoolean(pref_login, false);
    }

    // <----------- For Cache Clearing -------------->

    //get login type
    public String getLoginType() {
        return pref.getString(loginType, "");
    }

    //get user id
    public String userId() {
        return pref.getString(profileId, null);
    }

    //get user image
    public String getUserImage() {
        return pref.getString(userImage, "");
    }

    //get user name
    public String getUserName() {
        return pref.getString(profileName, "User");
    }

    //book storage folder path
    public String bookStorage() {

        ContextWrapper wrapper = new ContextWrapper(activity);
        //String mPath = wrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/AndroidEBook";

//        String mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/AndroidEBook";
        String mPath = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/AndroidEBook/";

        File createFile = new File(mPath);

        if (!createFile.exists()) {
            createFile.mkdirs();
        }

        return createFile.getAbsolutePath();

        //return activity.getExternalFilesDir("AndroidEBook").toString();
    }

    //get device id
    @SuppressLint("HardwareIds")
    public String getDeviceId() {
        String deviceId;
        try {
            deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            deviceId = "NotFound";
        }
        return deviceId;
    }

    //rtl
    public void forceRTLIfSupported() {
        if (activity.getResources().getString(R.string.isRTL).equals("true")) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public String themMode() {
        return pref.getString(themSetting, "system");
    }

    //rtl
    public boolean isRtl() {
        return activity.getResources().getString(R.string.isRTL).equals("true");
    }

    //network check
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*<---------------------- download book ---------------------->*/

    public void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    //get screen width
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point point = new Point();
        point.x = display.getWidth();
        point.y = display.getHeight();
        columnWidth = point.x;
        return columnWidth;
    }

    public String getAuthorFormatted(String text) {

        if (text == null || text.isEmpty()) {
            return "---";
        }

       /* String mText = text.trim();
        if (mText.charAt(mText.length() - 1) == ',') {
            mText = mText.replace(",", "");
        }

        return mText;*/

        return text;
    }

    public void getExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
            }
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
            }
        }
    }

    /*<---------------------- download book ---------------------->*/

    public boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /*<---------------------- download book ---------------------->*/

    public void download(String id, String bookName, String bookImage, String bookAuthor, String bookUrl, String type) {

        //Book file save folder name
        File rootBook = new File(bookStorage());
        if (!rootBook.exists()) {
            rootBook.mkdirs();
        }

        if (type.equals("epub")) {
            filename = "filename-" + id + ".epub";
        } else {
            filename = "filename-" + id + ".pdf";
        }

        File file = new File(bookStorage(), filename);
        if (!file.exists()) {

            Method.isDownload = false;

            Intent serviceIntent = new Intent(activity, DownloadService.class);
            serviceIntent.setAction(DownloadService.ACTION_START);
            serviceIntent.putExtra("id", id);
            serviceIntent.putExtra("downloadUrl", bookUrl);
            serviceIntent.putExtra("file_path", rootBook.toString());
            serviceIntent.putExtra("file_name", filename);

            activity.startService(serviceIntent);

        } else {
            alertBox(activity.getResources().getString(R.string.you_have_allReady_download_book));
        }

        new DownloadImage().execute(bookImage, id, bookName, bookAuthor);

    }

    //---------------Interstitial Ad---------------//

    //add to favourite
    public void addToFav(String id, String userId, FavouriteIF favouriteIF) {

        Utils.show(activity);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("book_id", id);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "book_favourite");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<FavouriteRP> call = apiService.getFavouriteBook(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<FavouriteRP>() {
            @Override
            public void onResponse(@NotNull Call<FavouriteRP> call, @NotNull Response<FavouriteRP> response) {

                try {
                    FavouriteRP favouriteRP = response.body();
                    assert favouriteRP != null;

                    if (favouriteRP.getStatus().equals("1")) {
                        if (favouriteRP.getSuccess().equals("1")) {
                            favouriteIF.isFavourite(favouriteRP.getIs_favourite(), favouriteRP.getMsg());
                        } else {
                            favouriteIF.isFavourite("", favouriteRP.getMsg());
                        }
                        Toast.makeText(activity, favouriteRP.getMsg(), Toast.LENGTH_SHORT).show();
                    } else {
                        alertBox(favouriteRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<FavouriteRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public void onClickAd(final int position, final String type, final String id, final String subId, final String title, final String fileType, final String fileUrl, String otherData) {

        Utils.show(activity);

        if (Constant.appRP != null) {
            if (Constant.appRP.isInterstitial_ad()) {
                Constant.AD_COUNT = Constant.AD_COUNT + 1;
                if (Constant.AD_COUNT == Constant.AD_COUNT_SHOW) {
                    Constant.AD_COUNT = 0;

                    if (Constant.appRP.getAd_network().equals("admob")) {

                        AdRequest adRequest;
                        if (personalizationAd) {
                            adRequest = new AdRequest.Builder()
                                    .build();
                        } else {
                            Bundle extras = new Bundle();
                            extras.putString("npa", "1");
                            adRequest = new AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                    .build();
                        }

                        InterstitialAd.load(activity, Constant.appRP.getInterstitial_ad_id(), adRequest, new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                // The mInterstitialAd reference will be null until
                                // an ad is loaded.
                                Log.i("admob_error", "onAdLoaded");
                                Utils.dismiss();
                                interstitialAd.show(activity);
                                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        Log.e("TAG", "The ad was dismissed.");
                                        onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        Log.e("TAG", "The ad failed to show.");
                                        onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        Log.e("TAG", "The ad was shown.");
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                Log.i("admob_error", loadAdError.getMessage());
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }
                        });

                    }
                    else if (Constant.appRP.getAd_network().equals("facebook")) {

                        com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(activity, Constant.appRP.getInterstitial_ad_id());
                        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                            @Override
                            public void onInterstitialDisplayed(Ad ad) {
                                // Interstitial ad displayed callback
                                Log.e("fb_ad", "Interstitial ad displayed.");
                            }

                            @Override
                            public void onInterstitialDismissed(Ad ad) {
                                // Interstitial dismissed callback
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                Log.e("fb_ad", "Interstitial ad dismissed.");
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                // Ad error callback
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                Log.e("fb_ad", "Interstitial ad failed to load: " + adError.getErrorMessage());
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                // Interstitial ad is loaded and ready to be displayed
                                Log.d("fb_ad", "Interstitial ad is loaded and ready to be displayed!");
                                Utils.dismiss();
                                // Show the ad
                                interstitialAd.show();
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                // Ad clicked callback
                                Log.d("fb_ad", "Interstitial ad clicked!");
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                // Ad impression logged callback
                                Log.d("fb_ad", "Interstitial ad impression logged!");
                            }
                        };

                        // For auto play video ads, it's recommended to load the ad
                        // at least 30 seconds before it is shown
                        com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = interstitialAd.buildLoadAdConfig().
                                withAdListener(interstitialAdListener).withCacheFlags(CacheFlag.ALL).build();
                        interstitialAd.loadAd(loadAdConfig);

                    }
                    else if (Constant.appRP.getAd_network().equals("unityds")) {
                        UnityAds.show((Activity) activity, Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                            @Override
                            public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }

                            @Override
                            public void onUnityAdsShowStart(String s) {
                            }

                            @Override
                            public void onUnityAdsShowClick(String s) {
                            }

                            @Override
                            public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }
                        });
                    }
                    else if (Constant.appRP.getAd_network().equals("startapp")) {
                        StartAppAd startAppAd = new StartAppAd(activity);
                        startAppAd.loadAd(new AdEventListener() {
                            @Override
                            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                Utils.dismiss();

                                startAppAd.showAd(new AdDisplayListener() {
                                    @Override
                                    public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                        Utils.dismiss();
                                        onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                    }

                                    @Override
                                    public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                    }

                                    @Override
                                    public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                                    }

                                    @Override
                                    public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                        Utils.dismiss();
                                        onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                    }
                                });
                            }

                            @Override
                            public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }
                        });
                    }
                    else if (Constant.appRP.getAd_network().equals("applovins")) {
                        MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.appRP.getInterstitial_ad_id(), activity);
                        maxInterstitialAd.setListener(new MaxAdListener() {
                            @Override
                            public void onAdLoaded(MaxAd ad) {
                                Utils.dismiss();
                                maxInterstitialAd.showAd();
                            }

                            @Override
                            public void onAdDisplayed(MaxAd ad) {
                            }

                            @Override
                            public void onAdHidden(MaxAd ad) {
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }

                            @Override
                            public void onAdClicked(MaxAd ad) {
                            }

                            @Override
                            public void onAdLoadFailed(String adUnitId, MaxError error) {
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }

                            @Override
                            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                Utils.dismiss();
                                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                            }
                        });
                        // Load the first ad
                        maxInterstitialAd.loadAd();
                    }

                } else {
                    Utils.dismiss();
                    onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                }

            } else {
                Utils.dismiss();
                onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
            }

        } else {
            Utils.dismiss();
            onClick.position(position, type, id, subId, title, fileType, fileUrl, otherData);
        }
    }

    //alert message box
    public void alertBox(String message) {

        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(Html.fromHtml(message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error_message", e.toString());
        }

    }

    //account suspend
    public void suspend(String message) {

        if (isLogin()) {

            String loginType = getLoginType();
            assert loginType != null;
            if (loginType.equals("google")) {

                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(activity, task -> {

                        });
            } else if (loginType.equals("facebook")) {
                LoginManager.getInstance().logOut();
            }

            editor.putBoolean(pref_login, false);
            editor.commit();
            Events.Login loginNotify = new Events.Login("");
            GlobalBus.getBus().post(loginNotify);
        }

        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(Html.fromHtml(message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {
                                activity.startActivity(new Intent(activity, MainActivity.class));
                                activity.finishAffinity();
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error_message", e.toString());
        }

    }

    public String webViewText() {
        String color;
        if (isDarkMode()) {
            color = Constant.webViewTextDark;
        } else {
            color = Constant.webViewText;
        }
        return color;
    }

    public String webViewLink() {
        String color;
        if (isDarkMode()) {
            color = Constant.webViewLinkDark;
        } else {
            color = Constant.webViewLink;
        }
        return color;
    }

    public String isWebViewTextRtl() {
        String isRtl;
        if (isRtl()) {
            isRtl = "rtl";
        } else {
            isRtl = "ltr";
        }
        return isRtl;
    }

    public String getTextWithRating(String rate) {
        String mRate;
        if (rate == null || rate.isEmpty()) {
            return "0.0 ⭐";
        }
        if (rate.contains(".")) {
            mRate = rate + " ⭐";
        } else {
            mRate = rate + ".0 ⭐";
        }
        return mRate;
    }

    //check dark mode or not
    public boolean isDarkMode() {
        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                return true;
            default:
                return false;
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadImage extends AsyncTask<String, String, String> {

        private final DatabaseHandler db = new DatabaseHandler(activity);
        private String filePath;
        private String iconsStoragePath;
        private String id, bookName, bookAuthor;

        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL(params[0]);
                id = params[1];
                bookName = params[2];
                bookAuthor = params[3];

                iconsStoragePath = bookStorage();
                File sdIconStorageDir = new File(iconsStoragePath);
                //create storage directories, if they don't exist
                if (!sdIconStorageDir.exists()) {
                    sdIconStorageDir.mkdirs();
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                String fname = "Image-" + id;
                filePath = iconsStoragePath + "/" + fname + ".jpg";

                File file = new File(iconsStoragePath, filePath);
                if (file.exists()) {
                    Log.d("file_exists", "file_exists");
                } else {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                        //choose another format if PNG doesn't suit you
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        Log.w("TAG", "Error saving image file: " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                // Log exception
                Log.d("error_info", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (db.checkIdDownloadBook(id)) {
                db.addDownload(new DownloadList(id, bookName, filePath, bookAuthor, iconsStoragePath + "/" + filename));
                if (recordPref != null) {
                    recordPref.countDownload(true);
                }
            }
            super.onPostExecute(s);
        }
    }
}
