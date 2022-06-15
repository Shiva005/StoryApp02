package com.dreamlibrary.storyapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.adapter.CategoryAdapter;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.CategoryList;
import com.dreamlibrary.storyapp.response.CatRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.EndlessRecyclerViewScrollListener;
import com.dreamlibrary.storyapp.util.Method;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ConstraintLayout conNoData;
    private String catType, catId;
    private ProgressBar progressBar;
    private List<CategoryList> categoryLists;
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private Boolean isOver = false;
    private int paginationIndex = 1;
    private String adsParam = "1";
    private InputMethodManager imm;
    private LayoutAnimationController animation;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.category));
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        categoryLists = new ArrayList<>();

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            if (otherData.equals("true")) {
                SubCatBookFragment subCatBookFragment = new SubCatBookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("title", title);
                subCatBookFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, subCatBookFragment, title)
                        .addToBackStack(title).commitAllowingStateLoss();
            } else {
                BookFragment bookFragment = new BookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", type);
                bundle.putString("title", title);
                bundle.putString("id", id);
                bundle.putString("subId", subId);
                bookFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, bookFragment, title)
                        .addToBackStack(title).commitAllowingStateLoss();
            }
        };
        method = new Method(getActivity(), onClick);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_category_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_category_fragment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    categoryAdapter.hideHeader();
                }
            }
        });

        callData();

        setHasOptionsMenu(true);
        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            category();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    private void category() {

        if (getActivity() != null) {

            if (categoryAdapter == null) {
                categoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_category");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<CatRP> call = apiService.getCategory(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<CatRP>() {
                @Override
                public void onResponse(@NotNull Call<CatRP> call, @NotNull Response<CatRP> response) {

                    if (getActivity() != null) {

                        try {

                            CatRP catRP = response.body();
                            assert catRP != null;

                            if (catRP.getStatus().equals("1")) {

                                adsParam = catRP.getAds_param();

                                if (catRP.getCategoryLists().size() == 0) {
                                    if (categoryAdapter != null) {
                                        categoryAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    categoryLists.addAll(catRP.getCategoryLists());
                                }

                                if (categoryAdapter == null) {
                                    if (categoryLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        categoryAdapter = new CategoryAdapter(getActivity(), categoryLists, "category", onClick);
                                        recyclerView.setAdapter(categoryAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    categoryAdapter.notifyDataSetChanged();
                                }

                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(catRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(@NotNull Call<CatRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

}
