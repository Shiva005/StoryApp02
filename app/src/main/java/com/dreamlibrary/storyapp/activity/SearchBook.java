package com.dreamlibrary.storyapp.activity;

import static com.dreamlibrary.storyapp.util.Method.personalizationAd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.adapter.AdapterSearchSection;
import com.dreamlibrary.storyapp.adapter.BookAdapterGV;
import com.dreamlibrary.storyapp.adapter.BookAdapterLV;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.item.ChildHomeSection;
import com.dreamlibrary.storyapp.item.HomeMainSection;
import com.dreamlibrary.storyapp.item.HomeSection;
import com.dreamlibrary.storyapp.response.BookRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.BannerAds;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.EndlessRecyclerViewScrollListener;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
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

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchBook extends AppCompatActivity {
    private OnClick onClick;
    private Method method;
    private String searchText = " ";
    public MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private List<BookList> bookLists;
    private LinearLayout linearLayout;
    private ConstraintLayout conNoData;
    private BookAdapterGV bookAdapterGV;
    private BookAdapterLV bookAdapterLV;
    private boolean isListView = true;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private MaterialTextView textViewCount;
    private TextInputEditText inputLayout;
    private ImageView imageViewGridView, imageViewListView, searchButton, back;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String tagText;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        tagText = getIntent().getStringExtra("searchTag");
        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) ->
                startActivity(new Intent(SearchBook.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
        method = new Method(SearchBook.this, onClick);
        method.forceRTLIfSupported();

        bookLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(SearchBook.this, resId);

        back = findViewById(R.id.back);
        searchButton = findViewById(R.id.searchBtn);
        inputLayout = findViewById(R.id.inputLayout);

        conNoData = findViewById(R.id.include);
        imageViewListView = findViewById(R.id.imageView_listView_bookList);
        imageViewGridView = findViewById(R.id.imageView_gridView_bookList);
        recyclerView = findViewById(R.id.recyclerView_bookList);
        textViewCount = findViewById(R.id.textView_num_bookList);
        linearLayout = findViewById(R.id.linearLayout_bookList);
        BannerAds.showBannerAds(SearchBook.this, linearLayout);

        conNoData.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchBook.this);
        recyclerView.setLayoutManager(layoutManager);
        loadMoreData(layoutManager);

        imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
        imageViewGridView.setOnClickListener(v -> {
            isListView = false;
            viewChange();
            scrollListener.resetState();
            adsParam = "1";
            paginationIndex = 1;
            isOver = false;
            bookAdapterGV = null;
            GridLayoutManager layoutManagerGr = new GridLayoutManager(SearchBook.this, 3);
            layoutManagerGr.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (bookAdapterGV != null) {
                        if (bookAdapterGV.getItemViewType(position) == 1) {
                            return 1;
                        } else {
                            return 3;
                        }
                    }
                    return 3;
                }
            });
            recyclerView.setLayoutManager(layoutManagerGr);
            loadMoreData(layoutManagerGr);
            callData();
        });

        /*imageViewListView.setOnClickListener(v -> {
            isListView = true;
            viewChange();
            scrollListener.resetState();
            adsParam = "1";
            paginationIndex = 1;
            isOver = false;
            bookAdapterLV = null;
            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(SearchBook.this);
            recyclerView.setLayoutManager(layoutManager1);
            loadMoreData(layoutManager1);
            callData();
        });*/

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputLayout.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        inputLayout.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchButton.performClick();
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conNoData.setVisibility(View.GONE);
                searchText = getSearchText();
                hideKeyBord();
                if (!searchText.equals("")) {
                    isListView = false;
                    viewChange();
                    scrollListener.resetState();
                    adsParam = "1";
                    paginationIndex = 1;
                    isOver = false;
                    bookAdapterGV = null;
                    GridLayoutManager layoutManagerGr = new GridLayoutManager(SearchBook.this, 3);
                    layoutManagerGr.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            if (bookAdapterGV != null) {
                                if (bookAdapterGV.getItemViewType(position) == 1) {
                                    return 1;
                                } else {
                                    return 3;
                                }
                            }
                            return 3;
                        }
                    });
                    recyclerView.setLayoutManager(layoutManagerGr);
                    loadMoreData(layoutManagerGr);
                    callData();
                }
            }
        });

        isListView = true;
        if (tagText != null) {
            conNoData.setVisibility(View.GONE);
            inputLayout.setText(tagText);
            searchText = tagText;
            hideKeyBord();
            isListView = false;
            viewChange();
            scrollListener.resetState();
            adsParam = "1";
            paginationIndex = 1;
            isOver = false;
            bookAdapterGV = null;
            GridLayoutManager layoutManagerGr = new GridLayoutManager(SearchBook.this, 3);
            layoutManagerGr.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (bookAdapterGV != null) {
                        if (bookAdapterGV.getItemViewType(position) == 1) {
                            return 1;
                        } else {
                            return 3;
                        }
                    }
                    return 3;
                }
            });
            recyclerView.setLayoutManager(layoutManagerGr);
            loadMoreData(layoutManagerGr);
            callData();

        } else {
            loadSearchSection();
        }
    }

    private void hideKeyBord() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public String getSearchText() {

        String text = inputLayout.getText().toString();

        if (text.equals("") || text.isEmpty()) {
            method.alertBox("Please Insert Text");
        } else {
            return text;
        }
        return "";
    }

    public void loadMoreData(RecyclerView.LayoutManager layoutManager) {
        scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    if (isListView) {
                        bookAdapterLV.hideHeader();
                    } else {
                        bookAdapterGV.hideHeader();
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void viewChange() {
        if (isListView) {
            imageViewGridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));
            imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
        } else {
            imageViewGridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_hov));
            imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // action with ID action_refresh was selected
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    public void callData() {
        if (method.isNetworkAvailable()) {
            if (isListView) {
                //bookLV();
            } else {
                bookGV();
            }
        } else {
            Utils.dismiss();
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    /*public void bookLV() {

        if (bookAdapterLV == null) {
            bookLists.clear();
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SearchBook.this));
        jsObj.addProperty("search_text", searchText);
        jsObj.addProperty("page", paginationIndex);
        jsObj.addProperty("ads_param", adsParam);
        jsObj.addProperty("is_book", "list_book");
        jsObj.addProperty("method_name", "get_search_books");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookRP> call = apiService.getSearchBook(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<BookRP>() {
            @Override
            public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                try {
                    BookRP bookRP = response.body();
                    assert bookRP != null;

                    if (bookRP.getStatus().equals("1")) {

                        adsParam = bookRP.getAds_param();

                        if (bookRP.getBookLists().size() == 0) {
                            if (bookAdapterLV != null) {
                                bookAdapterLV.hideHeader();
                                isOver = true;
                            }
                        } else {
                            bookLists.addAll(bookRP.getBookLists());
                        }

                        String count = bookRP.getTotal_books() + " " + getResources().getString(R.string.items_capital);
                        textViewCount.setText(count);

                        if (bookAdapterLV == null) {
                            if (bookRP.getBookLists().size() == 0) {
                                conNoData.setVisibility(View.VISIBLE);
                            } else {
                                conNoData.setVisibility(View.GONE);
                                bookAdapterLV = new BookAdapterLV(SearchBook.this, bookLists, "cat_by_list", onClick);
                                recyclerView.setAdapter(bookAdapterLV);
                                recyclerView.setLayoutAnimation(animation);
                            }
                        } else {
                            bookAdapterLV.notifyDataSetChanged();
                        }

                    } else {
                        method.alertBox(bookRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox("LV: "+getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(@NotNull Call<BookRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }*/

    public void loadSearchSection() {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SearchBook.this));
        jsObj.addProperty("method_name", "search_section");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<HomeMainSection> call = apiService.getHomeSection(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<HomeMainSection>() {
            @Override
            public void onResponse(Call<HomeMainSection> call, Response<HomeMainSection> response) {
                try {
                    if (response.isSuccessful()) {
                        HomeMainSection section = response.body();
                        AdapterSearchSection adapterHomeSection = new AdapterSearchSection(SearchBook.this, new AdapterSearchSection.OnAdapterClick() {
                            @Override
                            public void seeAllClick(int position, HomeSection homeSection, String type) {
                                Toast.makeText(getApplicationContext(), "" + homeSection.getSectionTitle(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onHomeClick(int position, ChildHomeSection childHomeSection, String type) {

                                if (Constant.appRP != null) {
                                    Utils.show(SearchBook.this);
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

                                                InterstitialAd.load(SearchBook.this, Constant.appRP.getInterstitial_ad_id(), adRequest, new InterstitialAdLoadCallback() {
                                                    @Override
                                                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                                        // The mInterstitialAd reference will be null until
                                                        // an ad is loaded.
                                                        Log.i("admob_error", "onAdLoaded");
                                                        Utils.dismiss();
                                                        interstitialAd.show(SearchBook.this);
                                                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                            @Override
                                                            public void onAdDismissedFullScreenContent() {
                                                                // Called when fullscreen content is dismissed.
                                                                Log.e("TAG", "The ad was dismissed.");
                                                                startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                        .putExtra("bookId", childHomeSection.getId())
                                                                        .putExtra("position", position)
                                                                        .putExtra("type", type));
                                                            }

                                                            @Override
                                                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                                                // Called when fullscreen content failed to show.
                                                                Log.e("TAG", "The ad failed to show.");
                                                                startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                        .putExtra("bookId", childHomeSection.getId())
                                                                        .putExtra("position", position)
                                                                        .putExtra("type", type));
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
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                    }
                                                });

                                            } else if (Constant.appRP.getAd_network().equals("facebook")) {

                                                com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(SearchBook.this, Constant.appRP.getInterstitial_ad_id());
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
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                        Log.e("fb_ad", "Interstitial ad dismissed.");
                                                    }

                                                    @Override
                                                    public void onError(Ad ad, AdError adError) {
                                                        // Ad error callback
                                                        Utils.dismiss();
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
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

                                            } else if (Constant.appRP.getAd_network().equals("unityds")) {
                                                UnityAds.show(SearchBook.this, Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                                                    @Override
                                                    public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                                        Utils.dismiss();
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
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
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                    }
                                                });
                                            } else if (Constant.appRP.getAd_network().equals("startapp")) {
                                                StartAppAd startAppAd = new StartAppAd(SearchBook.this);
                                                startAppAd.loadAd(new AdEventListener() {
                                                    @Override
                                                    public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                                        Utils.dismiss();
                                                        startAppAd.showAd(new AdDisplayListener() {
                                                            @Override
                                                            public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                                                Utils.dismiss();
                                                                startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                        .putExtra("bookId", childHomeSection.getId())
                                                                        .putExtra("position", position)
                                                                        .putExtra("type", type));
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
                                                                startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                        .putExtra("bookId", childHomeSection.getId())
                                                                        .putExtra("position", position)
                                                                        .putExtra("type", type));
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                                        Utils.dismiss();
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                    }
                                                });
                                            } else if (Constant.appRP.getAd_network().equals("applovins")) {
                                                MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.appRP.getInterstitial_ad_id(), SearchBook.this);
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
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                    }

                                                    @Override
                                                    public void onAdClicked(MaxAd ad) {
                                                    }

                                                    @Override
                                                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                                                        Utils.dismiss();
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                    }

                                                    @Override
                                                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                                        Utils.dismiss();
                                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                                .putExtra("bookId", childHomeSection.getId())
                                                                .putExtra("position", position)
                                                                .putExtra("type", type));
                                                    }
                                                });
                                                // Load the first ad
                                                maxInterstitialAd.loadAd();
                                            }
                                        } else {
                                            Utils.dismiss();
                                            startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                    .putExtra("bookId", childHomeSection.getId())
                                                    .putExtra("position", position)
                                                    .putExtra("type", type));
                                        }

                                    } else {
                                        Utils.dismiss();
                                        startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                                .putExtra("bookId", childHomeSection.getId())
                                                .putExtra("position", position)
                                                .putExtra("type", type));
                                    }

                                } else {
                                    Utils.dismiss();
                                    startActivity(new Intent(getApplicationContext(), BookDetail.class)
                                            .putExtra("bookId", childHomeSection.getId())
                                            .putExtra("position", position)
                                            .putExtra("type", type));
                                }
                            }
                        }, section.getAuthorSectionsList(), "");
                        Collections.shuffle(section.getAuthorSectionsList());
                        recyclerView.setAdapter(adapterHomeSection);
                    }
                } catch (Exception t) {
                    Utils.dismiss();
                    Log.e("TAG", "Exception: " + t.getMessage());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            }

            @Override
            public void onFailure(Call<HomeMainSection> call, Throwable t) {
                Log.e("TAG", "onFailure: " + t.getMessage());
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    public void bookGV() {

        if (bookAdapterGV == null) {
            bookLists.clear();
            Utils.show(SearchBook.this);
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SearchBook.this));
        jsObj.addProperty("search_text", searchText);
        jsObj.addProperty("page", paginationIndex);
        jsObj.addProperty("ads_param", adsParam);
        jsObj.addProperty("is_book", "grid_book");
        jsObj.addProperty("method_name", "get_search_books");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookRP> call = apiService.getSearchBook(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<BookRP>() {
            @Override
            public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                try {
                    BookRP bookRP = response.body();
                    assert bookRP != null;

                    if (bookRP.getStatus().equals("1")) {

                        adsParam = bookRP.getAds_param();

                        if (bookRP.getBookLists().size() == 0) {
                            if (bookAdapterGV != null) {
                                bookAdapterGV.hideHeader();
                                isOver = true;
                            }
                        } else {
                            bookLists.addAll(bookRP.getBookLists());
                        }

                        String count = bookRP.getTotal_books() + " " + getResources().getString(R.string.items_capital);
                        textViewCount.setText(count);

                        if (bookAdapterGV == null) {
                            if (bookRP.getBookLists().size() == 0) {
                                conNoData.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                conNoData.setVisibility(View.GONE);
                                bookAdapterGV = new BookAdapterGV(SearchBook.this, bookLists, "cat_by_list", onClick);
                                Collections.shuffle(bookLists);
                                recyclerView.setAdapter(bookAdapterGV);
                                recyclerView.setLayoutAnimation(animation);
                            }
                        } else {
                            bookAdapterGV.notifyDataSetChanged();
                        }


                    } else {
                        method.alertBox(bookRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<BookRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
