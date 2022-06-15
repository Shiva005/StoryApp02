package com.dreamlibrary.storyapp.activity;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.dreamlibrary.storyapp.BuildConfig;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.fragment.AuthorBookFragment;
import com.dreamlibrary.storyapp.fragment.BookFragment;
import com.dreamlibrary.storyapp.fragment.ContinueFragment;
import com.dreamlibrary.storyapp.fragment.DownloadFragment;
import com.dreamlibrary.storyapp.fragment.FavouriteFragment;
import com.dreamlibrary.storyapp.fragment.HomeFragment;
import com.dreamlibrary.storyapp.fragment.ProfileFragment;
import com.dreamlibrary.storyapp.fragment.SubCatBookFragment;
import com.dreamlibrary.storyapp.response.AppRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.BannerAds;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.Events;
import com.dreamlibrary.storyapp.util.GlobalBus;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ixidev.gdpr.GDPRChecker;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static MaterialToolbar toolbar;
    private Method method;
    private NavigationView navigationView;
    private LinearLayout linearLayout, itemsDiscover, itemsLibrary;
    private InputMethodManager imm;
    private BottomNavigationView bottomNavigationView;
    private String id = "", subId = "", title = "", type = "";
    private CardView cvSearch;
    public static TextView mainTitle;
    private ImageView premiumPlus, downloads, favourites, continueReadings;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void bindView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        toolbar = findViewById(R.id.toolbar_main);
        linearLayout = findViewById(R.id.linearLayout_main);
        itemsDiscover = findViewById(R.id.ll_itemsDiscover);
        itemsLibrary = findViewById(R.id.ll_itemsLibrary);
        mainTitle = findViewById(R.id.txt_title);
        cvSearch = findViewById(R.id.cv_search);
        premiumPlus = findViewById(R.id.iv_premiumPlus);
        downloads = findViewById(R.id.iv_downloads);
        favourites = findViewById(R.id.iv_favourite);
        continueReadings = findViewById(R.id.iv_continueReadings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        method = new Method(MainActivity.this);
        method.forceRTLIfSupported();
        bindView();
        initListeners();

        GlobalBus.getBus().register(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            id = intent.getStringExtra("id");
            subId = intent.getStringExtra("subId");
            title = intent.getStringExtra("title");
            type = intent.getStringExtra("type");
        }

        toolbar.setTitle(getResources().getString(R.string.app_name));

        setSupportActionBar(toolbar);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        checkLogin();

        if (method.isNetworkAvailable()) {
            selectBottomItem(1);
            downloads.setVisibility(View.GONE);
            itemsLibrary.setVisibility(View.GONE);
            mainTitle.setVisibility(View.GONE);
            itemsDiscover.setVisibility(View.VISIBLE);
            appDetail();
        } else {
            selectBottomItem(0);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new DownloadFragment(),
                    getResources().getString(R.string.download)).commitAllowingStateLoss();
            downloads.setVisibility(View.GONE);
            itemsLibrary.setVisibility(View.VISIBLE);
            mainTitle.setVisibility(View.VISIBLE);
            itemsDiscover.setVisibility(View.GONE);
            mainTitle.setText("Download list");
        }
    }

    private void initListeners() {
        cvSearch.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchBook.class)));
        premiumPlus.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                //showCustomSelectionDialog();
                method.alertBox("This feature is coming soon...");
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        downloads.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new DownloadFragment(),
                    getResources().getString(R.string.download)).commitAllowingStateLoss();
            mainTitle.setText("Download list");
            downloads.setVisibility(View.GONE);
            favourites.setVisibility(View.VISIBLE);
            continueReadings.setVisibility(View.VISIBLE);
        });
        favourites.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new FavouriteFragment(),
                    getResources().getString(R.string.favorite)).commitAllowingStateLoss();
            mainTitle.setText("Favourite books list");
            downloads.setVisibility(View.VISIBLE);
            favourites.setVisibility(View.GONE);
            continueReadings.setVisibility(View.VISIBLE);
        });
        continueReadings.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new ContinueFragment(),
                    getResources().getString(R.string.continues)).commitAllowingStateLoss();
            mainTitle.setText("Your last readings");
            downloads.setVisibility(View.VISIBLE);
            favourites.setVisibility(View.VISIBLE);
            continueReadings.setVisibility(View.GONE);
        });
    }

    private void showCustomSelectionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.item_choose_premium_selection);

        LinearLayout chooseIndian = dialog.findViewById(R.id.ll_premiumPlansIndian);
        LinearLayout chooseNonIndian = dialog.findViewById(R.id.ll_premiumPlansNonIndian);
        Button dialogButton = dialog.findViewById(R.id.btn_cancel);

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        dialogButton.setOnClickListener(v -> dialog.dismiss());
        chooseIndian.setOnClickListener(v -> {
            startActivity(new Intent(this, Subscription.class).putExtra("type", "indian"));
            dialog.dismiss();
        });
        chooseNonIndian.setOnClickListener(v -> {
            startActivity(new Intent(this, Subscription.class).putExtra("type", "non_indian"));
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            String title = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount() - 1).getTag();
            if (title != null) {
                //toolbar.setTitle(title);
                toolbar.setTitle(getResources().getString(R.string.app_name));
            }
            super.onBackPressed();
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Confirm");
            adb.setMessage("Are you sure you want to exit?");
            adb.setCancelable(false);

            adb.setPositiveButton("No", (dialogInterface, i) -> {
            });

            adb.setNegativeButton("Yes", (dialogInterface, i) -> finish());

            AlertDialog alertDialog = adb.create();
            alertDialog.show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        // Handle navigation view item clicks here.
        item.setChecked(!item.isChecked());

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.readOffline:
                selectBottomItem(0);
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new ContinueFragment(),
                        getResources().getString(R.string.continues)).commitAllowingStateLoss();
                itemsLibrary.setVisibility(View.VISIBLE);
                downloads.setVisibility(View.VISIBLE);
                mainTitle.setVisibility(View.VISIBLE);
                continueReadings.setVisibility(View.GONE);
                itemsDiscover.setVisibility(View.GONE);
                mainTitle.setText("Your last readings");
                return true;

            case R.id.discover:
                selectBottomItem(1);
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new HomeFragment(),
                        getResources().getString(R.string.home)).commitAllowingStateLoss();
                downloads.setVisibility(View.GONE);
                itemsLibrary.setVisibility(View.GONE);
                mainTitle.setVisibility(View.GONE);
                itemsDiscover.setVisibility(View.VISIBLE);
                return true;

            case R.id.profile:
                selectBottomItem(2);
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new ProfileFragment(),
                        getResources().getString(R.string.profile)).commitAllowingStateLoss();
                itemsLibrary.setVisibility(View.GONE);
                mainTitle.setVisibility(View.VISIBLE);
                itemsDiscover.setVisibility(View.GONE);
                mainTitle.setText("My Profile");
                return true;

            default:
                return true;
        }
    }

    public void selectBottomItem(int position) {
        if (bottomNavigationView.getMenu().size() > position) {
            bottomNavigationView.getMenu().getItem(position).setChecked(true);
        } else {
            Log.e("Main", "BottomNavigation: " + "Not such a position found..!");
        }
    }

    public void appDetail() {
        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(MainActivity.this));
        jsObj.addProperty("method_name", "get_app_details");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<AppRP> call = apiService.getAppData(API.toBase64(jsObj.toString()));

        call.enqueue(new Callback<AppRP>() {
            @Override
            public void onResponse(@NotNull Call<AppRP> call, @NotNull Response<AppRP> response) {
                try {
                    if (response.isSuccessful()) {

                        Constant.appRP = response.body();
                        assert Constant.appRP != null;

                        if (Constant.appRP.getStatus().equals("1")) {
                            initializeAds();

                            if (Constant.appRP.isApp_update_status() && Constant.appRP.getApp_new_version() > BuildConfig.VERSION_CODE) {

                                showAppDialog(Constant.appRP.getApp_update_desc(),
                                        Constant.appRP.getApp_redirect_url(),
                                        Constant.appRP.isCancel_update_status());
                            }
                            if (Constant.appRP.getAd_network().equals("admob")) {
                                checkForConsent();
                            } else {
                                BannerAds.showBannerAds(MainActivity.this, linearLayout);
                            }

                            try {
                                //Constant.AD_COUNT_SHOW = Integer.parseInt(Constant.appRP.getInterstitial_ad_click());
                                Constant.AD_COUNT_SHOW = 1000;
                            } catch (Exception e) {
                                Constant.AD_COUNT_SHOW = 0;
                            }

                            try {
                                switch (type) {
                                    case "category":
                                        SubCatBookFragment subCatBookFragment = new SubCatBookFragment();
                                        Bundle bundleCat = new Bundle();
                                        bundleCat.putString("id", id);
                                        bundleCat.putString("title", title);
                                        subCatBookFragment.setArguments(bundleCat);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, subCatBookFragment, title)
                                                .commitAllowingStateLoss();
                                        toolbar.setTitle(title);
                                        break;
                                    case "subCategory":
                                        BookFragment bookFragment = new BookFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("type", type);
                                        bundle.putString("title", title);
                                        bundle.putString("id", id);
                                        bundle.putString("subId", subId);
                                        bookFragment.setArguments(bundle);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, bookFragment,
                                                title).commitAllowingStateLoss();
                                        toolbar.setTitle(title);
                                        break;
                                    case "author":
                                        AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                                        Bundle bundleAuthor = new Bundle();
                                        bundleAuthor.putString("title", title);
                                        bundleAuthor.putString("id", id);
                                        bundleAuthor.putString("type", type);
                                        authorBookFragment.setArguments(bundleAuthor);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, authorBookFragment,
                                                title).commitAllowingStateLoss();
                                        break;
                                    default:
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new HomeFragment(),
                                                getResources().getString(R.string.home)).commitAllowingStateLoss();
                                        break;
                                }
                            } catch (Exception e) {
                                method.alertBox(getResources().getString(R.string.wrong));
                            }
                        }

                    } else {
                        method.alertBox(Constant.appRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(@NotNull Call<AppRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private void initializeAds() {
        switch (Constant.appRP.getAd_network()) {
            case "unityds":
                UnityAds.initialize(this, Constant.appRP.getUnity_Game_Id(), false, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        Log.d(TAG, "Unity Ads Initialization Complete");
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                    }
                });
                break;
            case "applovins":
                AppLovinSdk.getInstance(this).setMediationProvider(AppLovinMediationProvider.MAX);
                AppLovinSdk.getInstance(this).initializeSdk(config -> {

                });
                break;
            case "startapp":
                StartAppSDK.init(this, Constant.appRP.getStartApp_Id(), false);
                StartAppAd.disableSplash();
        }

    }

    public void checkForConsent() {
        new GDPRChecker()
                .withContext(MainActivity.this)
                .withPrivacyUrl(Constant.appRP.getPrivacy_policy_link())
                .withPublisherIds(Constant.appRP.getPublisher_id())
                .check();
        BannerAds.showBannerAds(MainActivity.this, linearLayout);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 11 || requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectBottomItem(0);
            } else {
                selectBottomItem(1);
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Subscribe
    public void getLogin(Events.Login login) {
        if (method != null) {
            checkLogin();
        }
    }

    private void checkLogin() {
        if (navigationView != null) {
            int position = 7;
            if (method.isLogin()) {
                navigationView.getMenu().getItem(position).setIcon(R.drawable.ic_logout);
                navigationView.getMenu().getItem(position).setTitle(getResources().getString(R.string.logout));
            } else {
                navigationView.getMenu().getItem(position).setIcon(R.drawable.ic_login);
                navigationView.getMenu().getItem(position).setTitle(getResources().getString(R.string.login));
            }
        }
    }

    private void showAppDialog(String description, String link, boolean isCancel) {

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_app);
        dialog.setCancelable(false);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        MaterialTextView textViewDes = dialog.findViewById(R.id.textView_description_dialog_update);
        MaterialButton buttonUpdate = dialog.findViewById(R.id.button_update_dialog_update);
        MaterialButton buttonCancel = dialog.findViewById(R.id.button_cancel_dialog_update);

        if (isCancel) {
            buttonCancel.setVisibility(View.VISIBLE);
        } else {
            buttonCancel.setVisibility(View.GONE);
        }
        textViewDes.setText(description);

        buttonUpdate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }
}

