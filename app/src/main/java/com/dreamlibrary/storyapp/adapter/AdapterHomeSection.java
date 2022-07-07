package com.dreamlibrary.storyapp.adapter;

import static com.dreamlibrary.storyapp.util.Method.personalizationAd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.ChildHomeSection;
import com.dreamlibrary.storyapp.item.HomeSection;
import com.dreamlibrary.storyapp.item.MainChildHomeSection;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.github.ornolfr.ratingview.RatingView;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterHomeSection extends RecyclerView.Adapter<AdapterHomeSection.ViewHolder> {

    GridLayoutManager layoutManager;
    private Method method;
    private Activity activity;
    private String type;
    private int columnWidth;
    private List<HomeSection> homeSections;
    private List<ChildHomeSection> childAuthorSections;
    private OnAdapterClick onAdapterClick;
    private MaxInterstitialAd interstitialAd;
    int albumPosition = -1;

    public AdapterHomeSection(Activity activity, OnAdapterClick onAdapterClick, List<HomeSection> homeSections, String type) {
        this.activity = activity;
        this.type = type;
        this.homeSections = homeSections;
        this.onAdapterClick = onAdapterClick;
        method = new Method(activity);
        Resources r = activity.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((5 + 3) * padding)));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_author_section, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       /* ConstraintLayout.LayoutParams coParams = (ConstraintLayout.LayoutParams) holder.recyclerViewAuthor.getLayoutParams();
        coParams.height = columnWidth / 3 + 50;
        holder.recyclerViewAuthor.setLayoutParams(coParams);*/

        albumPosition = position;

        loadAlbums(homeSections.get(position).getId(), holder.recyclerViewAuthor, holder.titleText, albumPosition);
        holder.titleText.setTypeface(holder.titleText.getTypeface(), Typeface.BOLD);

        holder.titleText.setText(homeSections.get(position).getSectionTitle());

        holder.viewAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdapterClick.seeAllClick(position, homeSections.get(position), type);
            }
        });

    }

    @Override
    public int getItemCount() {
        return homeSections.size();
    }

    private void loadAlbums(String homeSectionId, final RecyclerView recyclerView, final TextView textView, int albumPosition) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("homesection_id", homeSectionId);
        jsObj.addProperty("method_name", "home_section_id");
        jsObj.addProperty("page", "1");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MainChildHomeSection> call = apiService.getChildHomeSection(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<MainChildHomeSection>() {
            @Override
            public void onResponse(@NotNull Call<MainChildHomeSection> call, @NotNull Response<MainChildHomeSection> response) {
                if (response.isSuccessful()) {
                    MainChildHomeSection mainChildAuthorSection = response.body();
                    childAuthorSections = new ArrayList<>();
                    childAuthorSections.addAll(mainChildAuthorSection.getChildAuthorSectionList());

                    Collections.shuffle(childAuthorSections);
                    ChildHomeSectionAdapter childAuthorAdapter = new ChildHomeSectionAdapter(childAuthorSections, activity, albumPosition, type);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(childAuthorAdapter);
                }
            }

            @Override
            public void onFailure(@NotNull Call<MainChildHomeSection> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                method.alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public interface OnAdapterClick {

        void seeAllClick(int position, HomeSection homeSection, String type);

        void onHomeClick(int position, ChildHomeSection childHomeSection, String type);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, viewAllText;
        RecyclerView recyclerViewAuthor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.titleText);
            viewAllText = itemView.findViewById(R.id.view_all);
            recyclerViewAuthor = itemView.findViewById(R.id.recyclerViewAuthor);

            if (albumPosition == 0) {
                layoutManager = new GridLayoutManager(activity, 1, RecyclerView.VERTICAL, false);
            } else if (albumPosition == 1) {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.HORIZONTAL, false);
            } else if (albumPosition == 2) {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.HORIZONTAL, false);
            } else if (albumPosition == 3) {
                layoutManager = new GridLayoutManager(activity, 1, RecyclerView.HORIZONTAL, false);
            } else if (albumPosition == 4) {
                layoutManager = new GridLayoutManager(activity, 4, RecyclerView.VERTICAL, false);
            } else if (albumPosition == 5) {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false);
            } else if (albumPosition == 6) {
                layoutManager = new GridLayoutManager(activity, 1, RecyclerView.HORIZONTAL, false);
            } else if (albumPosition == 7) {
                layoutManager = new GridLayoutManager(activity, 1, RecyclerView.VERTICAL, false);
            } else {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false);
            }

            recyclerViewAuthor.setLayoutManager(layoutManager);
            recyclerViewAuthor.setItemAnimator(new DefaultItemAnimator());
            recyclerViewAuthor.setHasFixedSize(true);

        }
    }


    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }


    class ChildHomeSectionAdapter extends RecyclerView.Adapter<ChildHomeSectionAdapter.ViewHolder> {

        private String type;
        List<ChildHomeSection> childHomeSections;
        private final Activity activity;
        private int pos;

        public ChildHomeSectionAdapter(List<ChildHomeSection> childHomeSections, Activity activity, int pos, String type) {
            this.type = type;
            this.childHomeSections = childHomeSections;
            this.activity = activity;
            this.pos = pos;
        }

        @Override
        public ChildHomeSectionAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            if (pos == 0) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 0) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter2, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 1) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter3, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 2) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter4, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 3) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter5, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 4) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter6, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 5) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else if (pos - 1 == 6) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter5, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            }else if (pos - 1 == 7) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter2, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            } else {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter, parent, false);
                return new ChildHomeSectionAdapter.ViewHolder(mView);
            }
        }

        @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
        @Override
        public void onBindViewHolder(@NotNull ChildHomeSectionAdapter.ViewHolder holder, final int position) {

            holder.textViewName.setText(childHomeSections.get(position).getBookTitle());

            /*String authorName = "";

            for (int i = 0; i < subCategoryLists.get(position).getAuthorArrayList().size(); i++) {
                authorName = authorName + subCategoryLists.get(position).getAuthorArrayList().get(i).getAuthorName() + ", ";
            }*/

            holder.textViewAuthor.setText(activity.getString(R.string.by) + "\u0020" + childHomeSections.get(position).getAuthorName());
            holder.textViewDescription.setText(Html.fromHtml(childHomeSections.get(position).getBookDescription()));

            holder.textViewRatingCount.setText(childHomeSections.get(position).getTotalRate());
            holder.ratingBar.setRating(Float.parseFloat(childHomeSections.get(position).getRateAverage()));

            if (!childHomeSections.get(position).getBookCoverImg().equals("")) {
                Glide.with(activity).load(childHomeSections.get(position).getBookCoverImg())
                        .placeholder(R.drawable.placeholder_portable)
                        .into(holder.imageView);
            }

            holder.cardView.setOnClickListener(v -> {
                Utils.show(activity);
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
                                                    onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                                }

                                                @Override
                                                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                                    // Called when fullscreen content failed to show.
                                                    Log.e("TAG", "The ad failed to show.");
                                                    onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
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
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                        }
                                    });

                                    break;
                                case "facebook":

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
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                            Log.e("fb_ad", "Interstitial ad dismissed.");
                                        }

                                        @Override
                                        public void onError(Ad ad, AdError adError) {
                                            // Ad error callback
                                            Utils.dismiss();
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
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
                                    UnityAds.show(activity, Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                                        @Override
                                        public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                            Utils.dismiss();
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
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
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                        }
                                    });
                                    break;
                                case "startapp":
                                    StartAppAd startAppAd = new StartAppAd(activity);
                                    startAppAd.loadAd(new AdEventListener() {
                                        @Override
                                        public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                            Utils.dismiss();
                                            startAppAd.showAd(new AdDisplayListener() {
                                                @Override
                                                public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                                    Utils.dismiss();
                                                    onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
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
                                                    onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                            Utils.dismiss();
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                        }
                                    });
                                    break;
                                case "applovins":
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
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                        }

                                        @Override
                                        public void onAdClicked(MaxAd ad) {
                                        }

                                        @Override
                                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                                            Utils.dismiss();
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                        }

                                        @Override
                                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                            Utils.dismiss();
                                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                                        }
                                    });
                                    // Load the first ad
                                    maxInterstitialAd.loadAd();
                                    break;
                            }

                        } else {
                            Utils.dismiss();
                            onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                        }

                    } else {
                        Utils.dismiss();
                        onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                    }

                } else {
                    Utils.dismiss();
                    onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
                }
            });
        }

        @Override
        public int getItemCount() {
            return childHomeSections.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageView;
            private RatingView ratingBar;
            private LinearLayout cardView;
            private RelativeLayout rRoot;
            private MaterialTextView textViewName, textViewAuthor, textViewDescription, textViewRatingCount;


            public ViewHolder(View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView_book_home_adapter);
                cardView = itemView.findViewById(R.id.cardView_book_home_adapter);
                rRoot = itemView.findViewById(R.id.rRoot);
                textViewName = itemView.findViewById(R.id.textView_title_book_home_adapter);
                textViewAuthor = itemView.findViewById(R.id.textView_author_book_home_adapter);
                textViewDescription = itemView.findViewById(R.id.textView_description_book_home_adapter);
                ratingBar = itemView.findViewById(R.id.ratingBar_book_home_adapter);
                textViewRatingCount = itemView.findViewById(R.id.textView_ratingCount_book_home_adapter);
            }
        }
    }
}