package com.dreamlibrary.storyapp.adapter;

import static com.dreamlibrary.storyapp.util.Method.personalizationAd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.multidex.BuildConfig;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.database.DatabaseHandler;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.response.RemoveContinueBook;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContinueAdapter extends RecyclerView.Adapter{

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_Ad = 2;
    private Activity context;
    private List<BookList> bookLists;
    private OnFavouriteClick onFavouriteClick;
    private Method method;
    private int columnWidth;

    public ContinueAdapter(Activity context, List<BookList> bookLists, OnFavouriteClick onFavouriteClick) {
        this.context = context;
        this.bookLists = bookLists;
        this.onFavouriteClick = onFavouriteClick;
        method=new Method(context);

        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((4 + 3) * padding)));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.continue_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        } else if (viewType == VIEW_TYPE_Ad) {
            View view = LayoutInflater.from(context).inflate(R.layout.adview_adapter, parent, false);
            return new AdOption(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            Glide.with(context).load(bookLists.get(position).getBook_cover_img()).placeholder(R.drawable.placeholder_portable).into(viewHolder.book_image);
            viewHolder.title_text.setText(bookLists.get(position).getBook_title());
            viewHolder.author_text.setText(bookLists.get(position).getAuthor_name());

            viewHolder.finish_read_bt.setOnClickListener(v->{
                DatabaseHandler db = new DatabaseHandler(context);
                BookList continueLists = bookLists.get(position);

                Dialog dialog = new Dialog(context,R.style.full_screen_dialog);
                //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.finish_reading_book_lay);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(false);
                dialog.show();

                //code to put the dialog at the bottom
                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.BOTTOM;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(wlp);

                TextView yesBt = dialog.findViewById(R.id.yesBt);
                TextView noBt = dialog.findViewById(R.id.noBt);

                yesBt.setOnClickListener(v1 -> {
                    Log.e("TAG", "onClick: continue id:  " + continueLists.getId());
                    Utils.show(context);
                    db.removeEpub(continueLists.getId());

                    JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(context));
                    jsObj.addProperty("user_id", method.userId());
                    jsObj.addProperty("package_name", "com.eng.audiobook");
                    jsObj.addProperty("method_name", "remove_user_continue_book");
                    jsObj.addProperty("book_id", "" + continueLists.getId());
                    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                    Call<RemoveContinueBook> call = apiService.removeContinueReading(API.toBase64(jsObj.toString()));
                    call.enqueue(new Callback<RemoveContinueBook>() {
                        @Override
                        public void onResponse(Call<RemoveContinueBook> call, Response<RemoveContinueBook> response) {
                            Utils.dismiss();

                            if (context != null) {
                                try {
                                    if (response.isSuccessful()) {
                                        RemoveContinueBook continueBook = response.body();

                                        bookLists.remove(position);
                                        notifyDataSetChanged();

                                        Toast.makeText(context, "Book has finished.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e("TAG", "onResponse: response is not success");
                                    }
                                } catch (Exception e) {
                                    Log.e("TAG", "onResponse: Exception: " + e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<RemoveContinueBook> call, Throwable t) {
                            Log.e("TAG", "onFailure: " + t.getMessage());
                        }
                    });
                    dialog.dismiss();
                });
                noBt.setOnClickListener(v1 -> {
                    dialog.dismiss();
                    Utils.dismiss();
                });
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.show(context);
                    if (Constant.appRP != null) {
                        if (Constant.appRP.isInterstitial_ad()) {
                            Constant.AD_COUNT = Constant.AD_COUNT + 1;
                            if (Constant.AD_COUNT == Constant.AD_COUNT_SHOW) {
                                Constant.AD_COUNT = 0;
                                switch (Constant.appRP.getAd_network()) {
                                    case "admob":
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

                                        InterstitialAd.load(context, Constant.appRP.getInterstitial_ad_id(), adRequest, new InterstitialAdLoadCallback() {
                                            @Override
                                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                                // The mInterstitialAd reference will be null until
                                                // an ad is loaded.
                                                Log.i("admob_error", "onAdLoaded");
                                                Utils.dismiss();
                                                interstitialAd.show((Activity) context);
                                                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                    @Override
                                                    public void onAdDismissedFullScreenContent() {
                                                        // Called when fullscreen content is dismissed.
                                                        Log.e("TAG", "The ad was dismissed.");
                                                        onFavouriteClick.OnClickItem(position);
                                                    }

                                                    @Override
                                                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                                        // Called when fullscreen content failed to show.
                                                        Log.e("TAG", "The ad failed to show.");
                                                        onFavouriteClick.OnClickItem(position);
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
                                                onFavouriteClick.OnClickItem(position);
                                            }
                                        });

                                        break;
                                    case "facebook":

                                        com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(context, Constant.appRP.getInterstitial_ad_id());
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
                                                onFavouriteClick.OnClickItem(position);
                                                Log.e("fb_ad", "Interstitial ad dismissed.");
                                            }

                                            @Override
                                            public void onError(Ad ad, AdError adError) {
                                                // Ad error callback
                                                Utils.dismiss();
                                                onFavouriteClick.OnClickItem(position);
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

                                        break;
                                    case "unityds":
                                        UnityAds.show((Activity) context, Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                                            @Override
                                            public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                                Utils.dismiss();
                                                onFavouriteClick.OnClickItem(position);
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
                                                onFavouriteClick.OnClickItem(position);
                                            }
                                        });
                                        break;
                                    case "startapp":
                                        StartAppAd startAppAd = new StartAppAd(context);
                                        startAppAd.loadAd(new AdEventListener() {
                                            @Override
                                            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                                Utils.dismiss();
                                                startAppAd.showAd(new AdDisplayListener() {
                                                    @Override
                                                    public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                                        Utils.dismiss();
                                                        onFavouriteClick.OnClickItem(position);
                                                    }

                                                    @Override
                                                    public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {

                                                    }

                                                    @Override
                                                    public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                                                        Utils.dismiss();
                                                    }

                                                    @Override
                                                    public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                                        Utils.dismiss();
                                                        onFavouriteClick.OnClickItem(position);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                                Utils.dismiss();
                                                onFavouriteClick.OnClickItem(position);
                                            }
                                        });
                                        break;
                                    case "applovins":
                                        MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.appRP.getInterstitial_ad_id(), (Activity) context);
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
                                                onFavouriteClick.OnClickItem(position);
                                            }

                                            @Override
                                            public void onAdClicked(MaxAd ad) {
                                            }

                                            @Override
                                            public void onAdLoadFailed(String adUnitId, MaxError error) {
                                                Utils.dismiss();
                                                onFavouriteClick.OnClickItem(position);
                                            }

                                            @Override
                                            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                                Utils.dismiss();
                                                onFavouriteClick.OnClickItem(position);
                                            }
                                        });
                                        // Load the first ad
                                        maxInterstitialAd.loadAd();
                                        break;
                                }

                            } else {
                                Utils.dismiss();
                                onFavouriteClick.OnClickItem(position);
                            }

                        } else {
                            Utils.dismiss();
                            onFavouriteClick.OnClickItem(position);
                        }

                    } else {
                        Utils.dismiss();
                        onFavouriteClick.OnClickItem(position);
                    }
                }
            });

        }
        else if (holder.getItemViewType() == VIEW_TYPE_Ad) {
            AdOption adOption = (AdOption) holder;
            if (adOption.conAdView.getChildCount() == 0) {
                switch (bookLists.get(position).getNative_ad_type()) {
                    case "admob":

                        @SuppressLint("InflateParams")
                        View view = context.getLayoutInflater().inflate(R.layout.admob_ad, null, true);

                        TemplateView templateView = view.findViewById(R.id.my_template);
                        if (templateView.getParent() != null) {
                            ((ViewGroup) templateView.getParent()).removeView(templateView); // <- fix
                        }
                        adOption.conAdView.addView(templateView);
                        AdLoader adLoader = new AdLoader.Builder(context, bookLists.get(position).getNative_ad_id())
                                .forNativeAd(nativeAd -> {
                                    NativeTemplateStyle styles = new
                                            NativeTemplateStyle.Builder()
                                            .build();

                                    templateView.setStyles(styles);
                                    templateView.setNativeAd(nativeAd);

                                })
                                .build();

                        AdRequest adRequest;
                        if (Method.personalizationAd) {
                            adRequest = new AdRequest.Builder()
                                    .build();
                        } else {
                            Bundle extras = new Bundle();
                            extras.putString("npa", "1");
                            adRequest = new AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                    .build();
                        }
                        adLoader.loadAd(adRequest);
                        break;
                    case "facebook": {
                        LayoutInflater inflater = LayoutInflater.from(context);
                        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, adOption.conAdView, false);

                        NativeAd nativeAd = new NativeAd(context, bookLists.get(position).getNative_ad_id());

                        // Add the AdOptionsView
                        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);

                        // Create native UI using the ad metadata.
                        MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
                        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
                        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

                        adOption.conAdView.addView(adView);

                        NativeAdListener nativeAdListener = new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(Ad ad) {
                                // Native ad finished downloading all assets
                                Log.e("status_data", "Native ad finished downloading all assets.");
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                // Native ad failed to load
                                Log.e("status_data", "Native ad failed to load: " + adError.getErrorMessage());
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                // Native ad is loaded and ready to be displayed
                                Log.d("status_data", "Native ad is loaded and ready to be displayed!");
                                // Race condition, load() called again before last ad was displayed
                                if (nativeAd != ad) {
                                    return;
                                }
                                // Inflate Native Ad into Container
                                Log.d("status_data", "on load" + " " + ad.toString());

                                NativeAdLayout nativeAdLayout = new NativeAdLayout(context);
                                AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
                                adChoicesContainer.removeAllViews();
                                adChoicesContainer.addView(adOptionsView, 0);

                                // Set the Text.
                                nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                nativeAdBody.setText(nativeAd.getAdBodyText());
                                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                                // Create a list of clickable views
                                List<View> clickableViews = new ArrayList<>();
                                clickableViews.add(nativeAdTitle);
                                clickableViews.add(nativeAdCallToAction);

                                // Register the Title and CTA button to listen for clicks.
                                nativeAd.registerViewForInteraction(
                                        adOption.conAdView,
                                        nativeAdMedia,
                                        nativeAdIcon,
                                        clickableViews);
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                // Native ad clicked
                                Log.d("status_data", "Native ad clicked!");
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                // Native ad impression
                                Log.d("status_data", "Native ad impression logged!");
                            }
                        };

                        // Request an ad
                        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
                        break;
                    }
                    case "startapp": {

                        LayoutInflater inflater = LayoutInflater.from(context);
                        CardView adView = (CardView) inflater.inflate(R.layout.native_start_item, adOption.conAdView, false);

                        adOption.conAdView.addView(adView);

                        ImageView icon = adView.findViewById(R.id.icon);
                        TextView title = adView.findViewById(R.id.title);
                        TextView description = adView.findViewById(R.id.description);
                        Button button = adView.findViewById(R.id.button);

                        final StartAppNativeAd nativeAd = new StartAppNativeAd(context);

                        nativeAd.loadAd(new NativeAdPreferences()
                                .setAdsNumber(1)
                                .setAutoBitmapDownload(true)
                                .setPrimaryImageSize(1), new AdEventListener() {
                            @Override
                            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                ArrayList<NativeAdDetails> ads = nativeAd.getNativeAds();    // get NativeAds list
                                NativeAdDetails nativeAdDetails = ads.get(0);
                                if (nativeAdDetails != null) {
                                    icon.setImageBitmap(nativeAdDetails.getImageBitmap());
                                    title.setText(nativeAdDetails.getTitle());
                                    description.setText(nativeAdDetails.getDescription());
                                    button.setText(nativeAdDetails.isApp() ? "Install" : "Open");

                                    nativeAdDetails.registerViewForInteraction(adView);

                                }
                            }

                            @Override
                            public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                if (BuildConfig.DEBUG) {
                                    Log.e("onFailedToReceiveAd: ", "" + ad.getErrorMessage());
                                }
                            }
                        });
                        break;
                    }
                    case "applovins": {
                        LayoutInflater inflater = LayoutInflater.from(context);
                        FrameLayout nativeAdLayout = (FrameLayout) inflater.inflate(R.layout.activity_native_max_template, adOption.conAdView, false);
                        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(bookLists.get(position).getNative_ad_id(), context);
                        nativeAdLoader.loadAd();
                        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                            @Override
                            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                                super.onNativeAdLoaded(maxNativeAdView, maxAd);
                                // Add ad view to view.
                                nativeAdLayout.removeAllViews();
                                nativeAdLayout.addView(maxNativeAdView);
                                adOption.conAdView.addView(nativeAdLayout);
                            }

                            @Override
                            public void onNativeAdLoadFailed(String s, MaxError maxError) {
                                super.onNativeAdLoadFailed(s, maxError);
                            }

                            @Override
                            public void onNativeAdClicked(MaxAd maxAd) {
                                super.onNativeAdClicked(maxAd);
                            }
                        });
                        break;
                    }
                }
            }
        }
    }


    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar_loading);
        }
    }

    @Override
    public int getItemCount() {
        if (bookLists.size() != 0) {
            return bookLists.size() + 1;
        } else {
            return bookLists.size();
        }
    }
    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemViewType(int position) {
        if (bookLists.size() == position) {
            return VIEW_TYPE_LOADING;
        } else if (bookLists.get(position).isIs_ads()) {
            return VIEW_TYPE_Ad;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView book_image,finish_read_bt;
        TextView title_text, author_text;

        public ViewHolder(View itemView) {
            super(itemView);

            finish_read_bt = itemView.findViewById(R.id.finish_read_bt);
            book_image = itemView.findViewById(R.id.imageView_book_home_adapter);
            title_text = itemView.findViewById(R.id.textView_title_book_home_adapter);
            author_text = itemView.findViewById(R.id.textView_author_book_home_adapter);
        }
    }

    public interface OnFavouriteClick {
        void OnClickItem(int position);
    }

    public class AdOption extends RecyclerView.ViewHolder {

        private ConstraintLayout conAdView;

        public AdOption(View itemView) {
            super(itemView);
            conAdView = itemView.findViewById(R.id.con_adView);
        }
    }
}
