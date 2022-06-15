package com.dreamlibrary.storyapp.fragment;

import static com.dreamlibrary.storyapp.util.Method.personalizationAd;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.activity.SearchBook;
import com.dreamlibrary.storyapp.adapter.AdapterAuthorSection;
import com.dreamlibrary.storyapp.adapter.AuthorAdapter;
import com.dreamlibrary.storyapp.adapter.SpinnerAuthorAdapter;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.AuthorList;
import com.dreamlibrary.storyapp.item.AuthorMainSection;
import com.dreamlibrary.storyapp.item.AuthorSection;
import com.dreamlibrary.storyapp.item.AuthorSpinnerList;
import com.dreamlibrary.storyapp.item.ChildAuthorSection;
import com.dreamlibrary.storyapp.response.AuthorRP;
import com.dreamlibrary.storyapp.response.AuthorSpinnerRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.EndlessRecyclerViewScrollListener;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private String authorType, authorId;
    private List<AuthorList> authorLists;
    private List<AuthorSection> authorSections = new ArrayList<>();
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private RecyclerView homeSectionRecyclerView;
    private AuthorAdapter authorAdapter;
    private AdapterAuthorSection adapterAuthorSection;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private InputMethodManager imm;

    private void bindView(View view) {
        conNoData = view.findViewById(R.id.con_noDataFound);
        recyclerView = view.findViewById(R.id.recyclerView_author_fragment);
        homeSectionRecyclerView = view.findViewById(R.id.recyclerView_author_section_fragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.author_fragment, container, false);
        bindView(view);
        method = new Method(getActivity(), onClick);
        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.author));
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        authorLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {

            AuthorBookFragment authorBookFragment = new AuthorBookFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putString("id", id);
            bundle.putString("type", type);
            authorBookFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                    authorBookFragment, title).addToBackStack(title).commitAllowingStateLoss();

        };


        conNoData.setVisibility(View.GONE);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (authorAdapter.getItemViewType(position) == 1) {
                    return 1;
                } else {
                    return 3;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    authorAdapter.hideHeader();
                }
            }
        });

        if (method.isNetworkAvailable()) {
            authorSection();
            author();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        setHasOptionsMenu(true);
        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            author();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showSearch(AuthorSpinnerRP authorSpinnerRP) {

        Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.search_dialog);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

        MaterialTextView textViewTitle = dialog.findViewById(R.id.textView_title_search_dialog);
        ImageView imageViewClose = dialog.findViewById(R.id.imageView_close_search_dialog);
        AppCompatSpinner spinnerAuthor = dialog.findViewById(R.id.spinner_search_dialog);
        TextInputEditText editText = dialog.findViewById(R.id.editText_search_dialog);
        MaterialButton button = dialog.findViewById(R.id.button_search_dialog);

        textViewTitle.setText(getResources().getString(R.string.search_author));
        editText.setHint(getResources().getString(R.string.search_book_name));

        authorSpinnerRP.getAuthorSpinnerLists().add(0, new AuthorSpinnerList("", getResources().getString(R.string.select_author_type)));

        SpinnerAuthorAdapter typeAdapter = new SpinnerAuthorAdapter(getActivity(), authorSpinnerRP.getAuthorSpinnerLists());


        spinnerAuthor.setAdapter(typeAdapter);

        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_spinner));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
                }
                authorType = authorSpinnerRP.getAuthorSpinnerLists().get(position).getAuthor_name();
                authorId = authorSpinnerRP.getAuthorSpinnerLists().get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        imageViewClose.setOnClickListener(v -> dialog.dismiss());

        button.setOnClickListener(v -> {

            editText.setError(null);

            String search = editText.getText().toString();
            if (authorType.equals(getResources().getString(R.string.select_author_type)) || authorType.equals("") || authorType.isEmpty()) {
                method.alertBox(getResources().getString(R.string.please_select_author));
            } else if (search.isEmpty()) {
                editText.requestFocus();
                editText.setError(getResources().getString(R.string.please_enter_book));
            } else {

                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Intent intent = new Intent(requireActivity(), SearchBook.class);
                intent.putExtra("id", authorId);
                intent.putExtra("search", search);
                intent.putExtra("type", "author_search");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void author() {

        if (getActivity() != null) {

            if (authorAdapter == null) {
                authorLists.clear();
                Utils.show(getContext());
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_author");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<AuthorRP> call = apiService.getAuthor(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<AuthorRP>() {
                @Override
                public void onResponse(@NotNull Call<AuthorRP> call, @NotNull Response<AuthorRP> response) {

                    if (getActivity() != null) {

                        try {

                            AuthorRP authorRP = response.body();
                            assert authorRP != null;

                            if (authorRP.getStatus().equals("1")) {

                                adsParam = authorRP.getAds_param();

                                if (authorRP.getAuthorLists().size() == 0) {
                                    if (authorAdapter != null) {
                                        authorAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    authorLists.addAll(authorRP.getAuthorLists());
                                }

                                if (authorAdapter == null) {
                                    if (authorLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {

//                                        Collections.reverse(authorLists);


                                        authorAdapter = new AuthorAdapter(getActivity(), authorLists, "author", onClick);
                                        recyclerView.setAdapter(authorAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    authorAdapter.notifyDataSetChanged();
                                }

                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(authorRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }
                    Utils.dismiss();
                }

                @Override
                public void onFailure(@NotNull Call<AuthorRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    Utils.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

    private void authorViewFragment(String type, String title, String id) {

        AuthorViewFragment authorViewFragment = new AuthorViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);

        if (type.equals("author_view")) {
            bundle.putString("title", title);
            bundle.putString("id", id);
        }

        authorViewFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                authorViewFragment, title).addToBackStack(title).commitAllowingStateLoss();
    }

    private void hideKeyBord() {
        if (requireActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void authorSection() {
        if (getActivity() != null) {

            if (authorAdapter == null) {
                authorSections.clear();
                Utils.show(getContext());
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "author_section");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<AuthorMainSection> call = apiService.getAuthorSection(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<AuthorMainSection>() {
                @Override
                public void onResponse(@NotNull Call<AuthorMainSection> call, @NotNull Response<AuthorMainSection> response) {

                    if (getActivity() != null) {

                        try {

                            AuthorMainSection mainSection = response.body();
                            assert mainSection != null;

                            if (!authorSections.isEmpty()) {
                                authorSections.clear();
                            }

                            if (!mainSection.getAuthorSectionsList().isEmpty()) {
                                authorSections.addAll(mainSection.getAuthorSectionsList());
                            } else {
                                homeSectionRecyclerView.setVisibility(View.GONE);
                            }

                            adapterAuthorSection = new AdapterAuthorSection(getActivity(), new AdapterAuthorSection.OnAdapterClick() {
                                @Override
                                public void seeAllClick(int position, AuthorSection authorSection, String type) {
                                    hideKeyBord();
                                    authorViewFragment("author_view", authorSection.getSectionTitle(), authorSection.getId());
                                }

                                @Override
                                public void onAuthorClick(int position, ChildAuthorSection childAuthorSection, String type) {
                                    AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("title", childAuthorSection.getAuthorName());
                                    bundle.putString("id", childAuthorSection.getAuthorId());
                                    bundle.putString("type", type);
                                    authorBookFragment.setArguments(bundle);

                                    Utils.show(getContext());

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

                                                    InterstitialAd.load(getContext(), Constant.appRP.getInterstitial_ad_id(), adRequest, new InterstitialAdLoadCallback() {
                                                        @Override
                                                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                                            // The mInterstitialAd reference will be null until
                                                            // an ad is loaded.
                                                            Log.i("admob_error", "onAdLoaded");
                                                            Utils.dismiss();
                                                            interstitialAd.show(getActivity());
                                                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                                @Override
                                                                public void onAdDismissedFullScreenContent() {
                                                                    // Called when fullscreen content is dismissed.
                                                                    Log.e("TAG", "The ad was dismissed.");
                                                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                                }

                                                                @Override
                                                                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                                                    // Called when fullscreen content failed to show.
                                                                    Log.e("TAG", "The ad failed to show.");
                                                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
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
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                        }
                                                    });

                                                }
                                                else if (Constant.appRP.getAd_network().equals("facebook")) {

                                                    com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(getContext(), Constant.appRP.getInterstitial_ad_id());
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
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                            Log.e("fb_ad", "Interstitial ad dismissed.");
                                                        }

                                                        @Override
                                                        public void onError(Ad ad, AdError adError) {
                                                            // Ad error callback
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
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
                                                    UnityAds.show(getActivity(), Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                                                        @Override
                                                        public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
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
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                        }
                                                    });
                                                }
                                                else if (Constant.appRP.getAd_network().equals("startapp")) {
                                                    StartAppAd startAppAd = new StartAppAd(getContext());
                                                    startAppAd.loadAd(new AdEventListener() {
                                                        @Override
                                                        public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                                            Utils.dismiss();
                                                            startAppAd.showAd(new AdDisplayListener() {
                                                                @Override
                                                                public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                                                    Utils.dismiss();
                                                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
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
                                                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                        }
                                                    });
                                                }
                                                else if (Constant.appRP.getAd_network().equals("applovins")) {
                                                    MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.appRP.getInterstitial_ad_id(), getActivity());
                                                    maxInterstitialAd.setListener(new MaxAdListener() {
                                                        @Override
                                                        public void onAdLoaded(MaxAd ad) {
                                                            Utils.dismiss();
                                                            maxInterstitialAd.showAd();
                                                        }

                                                        @Override
                                                        public void onAdDisplayed(MaxAd ad) {
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                    childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();

                                                        }

                                                        @Override
                                                        public void onAdHidden(MaxAd ad) {
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                        }

                                                        @Override
                                                        public void onAdClicked(MaxAd ad) {
                                                        }

                                                        @Override
                                                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                        }

                                                        @Override
                                                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                                            Utils.dismiss();
                                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                                        }
                                                    });
                                                    // Load the first ad
                                                    maxInterstitialAd.loadAd();
                                                }

                                            } else {
                                                Utils.dismiss();
                                                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                                            childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                            }

                                        } else {
                                            Utils.dismiss();
                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                    childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                        }

                                    } else {
                                        Utils.dismiss();
                                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                                                childAuthorSection.getAuthorName()).addToBackStack(childAuthorSection.getAuthorName()).commitAllowingStateLoss();
                                    }
                                }
                            }, authorSections, "author");
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                            homeSectionRecyclerView.setLayoutManager(layoutManager);
                            homeSectionRecyclerView.setAdapter(adapterAuthorSection);


                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }
                    }
                    Utils.dismiss();
                }

                @Override
                public void onFailure(@NotNull Call<AuthorMainSection> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    Utils.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });
        }
    }
}
