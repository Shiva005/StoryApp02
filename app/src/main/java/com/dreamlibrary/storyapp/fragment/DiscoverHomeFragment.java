package com.dreamlibrary.storyapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.BookDetail;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.adapter.AdapterHomeSection;
import com.dreamlibrary.storyapp.adapter.BookHomeAdapter;
import com.dreamlibrary.storyapp.adapter.SliderAdapter;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.ChildHomeSection;
import com.dreamlibrary.storyapp.item.HomeMainSection;
import com.dreamlibrary.storyapp.item.HomeSection;
import com.dreamlibrary.storyapp.response.HomeRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.EnchantedViewPager;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverHomeFragment extends Fragment {

    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000;
    private final Handler handler = new Handler();
    private Method method;
    private OnClick onClick;
    private SliderAdapter sliderAdapter;
    private SliderAdapter sliderAdapter2;
    private BookHomeAdapter latestAdapter;
    private InputMethodManager imm;
    private EnchantedViewPager enchantedViewPager;
    private EnchantedViewPager enchantedViewPager2;

    private ConstraintLayout conNoData, conSlider, conContinue, conLatest, conHomeSec, conSlider2;
    private LinearLayout conMain;
    private RecyclerView recyclerViewLatest, recyclerViewHomeSec;
    private Timer timer;
    private Timer timer2;
    private Boolean isTimerStart = false;
    private Boolean isTimerStart2 = false;
    private Runnable Update;
    private Runnable Update2;
    private TabLayout tabLayout, tabLayout2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover_home, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.home));
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            if (type.equals("home_cat")) {
                Log.e("CAT_FRAG", "onCreateView: 222");
            } else if (type.equals("home_author")) {
                AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("title", title);
                bundle.putString("id", id);
                bundle.putString("type", type);
                authorBookFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                        title).addToBackStack(title).commitAllowingStateLoss();
            } else {
                startActivity(new Intent(getActivity(), BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(getActivity(), onClick);
        conMain = view.findViewById(R.id.con_main_home);
        conNoData = view.findViewById(R.id.con_noDataFound);
        enchantedViewPager = view.findViewById(R.id.slider_home);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout2 = view.findViewById(R.id.tab_layout2);


        enchantedViewPager2 = view.findViewById(R.id.slider_home2);
        conSlider = view.findViewById(R.id.con_slider_home);
        conSlider2 = view.findViewById(R.id.con_slider_home2);
        conContinue = view.findViewById(R.id.con_continue_home);
        conLatest = view.findViewById(R.id.con_latest_home);
        conHomeSec = view.findViewById(R.id.con_home_sec_home);
        MaterialTextView textViewLatest = view.findViewById(R.id.textView_latestViewAll_home);
        recyclerViewLatest = view.findViewById(R.id.recyclerView_latest_home);
        recyclerViewHomeSec = view.findViewById(R.id.recyclerView_home_sec_home);

        conNoData.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);

        enchantedViewPager.useScale();
        enchantedViewPager.removeAlpha();

        recyclerViewLatest.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, RecyclerView.VERTICAL, false);
        recyclerViewLatest.setLayoutManager(gridLayoutManager);
        recyclerViewLatest.setFocusable(false);

        recyclerViewHomeSec.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerHomeSec = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerViewHomeSec.setLayoutManager(layoutManagerHomeSec);
        recyclerViewHomeSec.setFocusable(false);

        textViewLatest.setOnClickListener(v -> {
            hideKeyBord();
            bookFragment("latest", getResources().getString(R.string.latest), "", "");
        });

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                home(method.userId());
            } else {
                home("0");
            }
            loadHomeSection();
        } else {
            if (Utils.isShowing)
                Utils.dismiss();
        }

        setHasOptionsMenu(true);

        return view;
    }

    private void bookFragment(String type, String title, String id, String subId) {
        BookFragment bookFragment = new BookFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        if (type.equals("home_cat")) {
            bundle.putString("title", title);
            bundle.putString("id", id);
            bundle.putString("subId", subId);
        }

        bookFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                bookFragment, title).addToBackStack(title).commitAllowingStateLoss();
    }

    private void HomeSectionBook(String type, String title, String id) {

        BookFragment bookFragment = new BookFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);

        if (type.equals("home_section")) {
            bundle.putString("title", title);
            bundle.putString("id", id);
        }

        bookFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                bookFragment, title).addToBackStack(title).commitAllowingStateLoss();

    }

    private void hideKeyBord() {
        if (requireActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void home(String userId) {

        if (getActivity() != null) {

            Utils.show(getContext());
            tabLayout.setVisibility(View.GONE);
            tabLayout2.setVisibility(View.GONE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("method_name", "get_home");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<HomeRP> call = apiService.getHome(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<HomeRP>() {
                @Override
                public void onResponse(@NotNull Call<HomeRP> call, @NotNull Response<HomeRP> response) {

                    if (getActivity() != null) {

                        try {

                            if (response.isSuccessful()) {

                                HomeRP homeRP = response.body();
                                assert homeRP != null;

                                if (homeRP.getStatus().equals("1")) {

                                    if (homeRP.getSliderLists().size() != 0) {

                                        int columnWidth = method.getScreenWidth();
                                        enchantedViewPager.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, (int) (columnWidth / 2.1)));

                                        Update = () -> {
                                            isTimerStart = true;
                                            if (enchantedViewPager.getCurrentItem() == (sliderAdapter.getCount() - 1)) {
                                                enchantedViewPager.setCurrentItem(0, true);
                                            } else {
                                                enchantedViewPager.setCurrentItem(enchantedViewPager.getCurrentItem() + 1, true);
                                            }
                                        };

                                        sliderAdapter = new SliderAdapter(getActivity(), "slider", homeRP.getSliderLists(), onClick);
                                        enchantedViewPager.setAdapter(sliderAdapter);
                                        enchantedViewPager.setCurrentItem(1);

                                        tabLayout.setupWithViewPager(enchantedViewPager, true);


                                        if (sliderAdapter.getCount() > 1) {
                                            tabLayout.setVisibility(View.VISIBLE);
                                            //pageIndicatorView.setCount(homeRP.getSliderLists().size());
                                            timer = new Timer(); // This will create a new Thread
                                            timer.schedule(new TimerTask() { // task to be scheduled
                                                @Override
                                                public void run() {
                                                    handler.post(Update);
                                                }
                                            }, DELAY_MS, PERIOD_MS);
                                        } else {
                                            //pageIndicatorView.setVisibility(View.GONE);
                                        }

                                    } else {
                                        conSlider.setVisibility(View.GONE);
                                    }


                                    if (homeRP.getSecondSliderLists().size() != 0) {

                                        int columnWidth = method.getScreenWidth();
                                        enchantedViewPager2.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, (int) (columnWidth / 2.6)));

                                        Update2 = () -> {
                                            isTimerStart2 = true;
                                            if (enchantedViewPager2.getCurrentItem() == (sliderAdapter2.getCount() - 1)) {
                                                enchantedViewPager2.setCurrentItem(0, true);
                                            } else {
                                                enchantedViewPager2.setCurrentItem(enchantedViewPager2.getCurrentItem() + 1, true);
                                            }
                                        };

                                        sliderAdapter2 = new SliderAdapter(getActivity(), "slider", homeRP.getSecondSliderLists(), onClick);
                                        enchantedViewPager2.setAdapter(sliderAdapter2);
                                        enchantedViewPager2.setCurrentItem(1);

                                        tabLayout2.setupWithViewPager(enchantedViewPager2, true);

                                        if (sliderAdapter2.getCount() > 1) {
                                            tabLayout2.setVisibility(View.VISIBLE);
                                            //pageIndicatorView2.setCount(homeRP.getSecondSliderLists().size());
                                            timer2 = new Timer(); // This will create a new Thread
                                            timer2.schedule(new TimerTask() { // task to be scheduled
                                                @Override
                                                public void run() {
                                                    handler.post(Update2);
                                                }
                                            }, DELAY_MS, PERIOD_MS);
                                        }/* else {
                                            pageIndicatorView2.setVisibility(View.GONE);
                                        }*/

                                    } else {
                                        conSlider2.setVisibility(View.GONE);
                                    }

                                    if (homeRP.getLatestList().size() != 0) {
                                        latestAdapter = new BookHomeAdapter(getActivity(), homeRP.getLatestList(), "home_latest", onClick);
                                        recyclerViewLatest.setAdapter(latestAdapter);
                                    } else {
                                        conLatest.setVisibility(View.GONE);
                                    }
                                    conMain.setVisibility(View.VISIBLE);

                                } else if (homeRP.getStatus().equals("2")) {
                                    method.suspend(homeRP.getMessage());
                                } else {
                                    conNoData.setVisibility(View.VISIBLE);
                                    method.alertBox(homeRP.getMessage());
                                }

                                Log.e("TAG", "onResponse: " + "SUCCESS");

                            }
                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }
                    Utils.dismiss();

                }

                @Override
                public void onFailure(@NotNull Call<HomeRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());

                    conNoData.setVisibility(View.VISIBLE);
                    Utils.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }


    private void loadHomeSection() {

        if (getActivity() != null) {

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "home_section");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<HomeMainSection> call = apiService.getHomeSection(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<HomeMainSection>() {
                @Override
                public void onResponse(Call<HomeMainSection> call, Response<HomeMainSection> response) {

                    try {
                        if (response.isSuccessful()) {
                            HomeMainSection section = response.body();

                            AdapterHomeSection adapterHomeSection = new AdapterHomeSection(getActivity(), new AdapterHomeSection.OnAdapterClick() {
                                @Override
                                public void seeAllClick(int position, HomeSection homeSection, String type) {
                                    hideKeyBord();
                                    HomeSectionBook("home_section", homeSection.getSectionTitle(), homeSection.getId());
                                }

                                @Override
                                public void onHomeClick(int position, ChildHomeSection childHomeSection, String type) {
                                    startActivity(new Intent(getActivity(), BookDetail.class)
                                            .putExtra("bookId", childHomeSection.getId())
                                            .putExtra("position", position)
                                            .putExtra("type", type));
                                }
                            }, section.getAuthorSectionsList(), "");
                            recyclerViewHomeSec.setAdapter(adapterHomeSection);
                        }
                    } catch (Exception t) {
                        Log.e("TAG", "Exception: " + t.getMessage());
                        method.alertBox(getContext().getResources().getString(R.string.failed_try_again));
                    }
                }

                @Override
                public void onFailure(Call<HomeMainSection> call, Throwable t) {
                    Log.e("TAG", "onFailure: " + t.getMessage());
                    method.alertBox(getActivity().getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }
}
