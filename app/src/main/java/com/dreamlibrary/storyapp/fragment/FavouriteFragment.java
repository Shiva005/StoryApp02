package com.dreamlibrary.storyapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.BookDetail;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.adapter.FavouriteAdapter;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.response.BookRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.EndlessRecyclerViewScrollListener;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavouriteFragment extends Fragment {

    private FavouriteAdapter favouriteAdapter;
    private OnClick onClick;
    private LayoutAnimationController animation;
    private Method method;
    private RecyclerView recyclerView_favorite;
    private List<BookList> bookLists;
    private ConstraintLayout conNoData;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int paginationIndex = 1;
    private String adsParam = "1";
    private Boolean isOver = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.favorite));
        }

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
        bookLists = new ArrayList<>();
        method = new Method(getActivity(), onClick);

        recyclerView_favorite = view.findViewById(R.id.recyclerView_favorite);
        conNoData = view.findViewById(R.id.con_noDataFound);
        conNoData.setVisibility(View.GONE);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (favouriteAdapter != null) {
                    if (favouriteAdapter.getItemViewType(position) == 1) {
                        return 1;
                    } else {
                        return 3;
                    }
                }
                return 3;
            }
        });
        recyclerView_favorite.setLayoutManager(layoutManager);
        loadMoreData(layoutManager);
        callData();
        setHasOptionsMenu(true);
        return view;
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
                    favouriteAdapter.hideHeader();
                }
            }
        };
        recyclerView_favorite.addOnScrollListener(scrollListener);
    }
    public void callData() {
        if (method.isNetworkAvailable()) {
            Utils.show(getContext());
            Favourite();
        } else {
            Utils.dismiss();
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    private void Favourite() {

        if (getActivity() != null) {
            if (favouriteAdapter == null) {
                bookLists.clear();
            }
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", method.userId());
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("is_book", "grid_book");
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_favourite_book");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<BookRP> call = apiService.getLatestBook(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<BookRP>() {
                @Override
                public void onResponse(Call<BookRP> call, Response<BookRP> response) {

                    if (getActivity() != null) {

                        try {

                            BookRP bookRP = response.body();
                            assert bookRP != null;

                            if (bookRP.getStatus().equals("1")) {

                                adsParam = bookRP.getAds_param();

                                if (bookRP.getBookLists().size() == 0) {
                                    if (favouriteAdapter != null) {
                                        favouriteAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    bookLists.addAll(bookRP.getBookLists());
                                }
                                if (favouriteAdapter == null) {
                                    if (bookLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        favouriteAdapter = new FavouriteAdapter(getActivity(), bookLists, position ->
                                                startActivity(new Intent(getActivity(), BookDetail.class)
                                                                .putExtra("bookId", bookLists.get(position).getId())
                                                                .putExtra("type", "favourite_book")
                                                                .putExtra("position", position)));
                                        recyclerView_favorite.setAdapter(favouriteAdapter);
                                        recyclerView_favorite.setLayoutAnimation(animation);
                                    }
                                } else {
                                    favouriteAdapter.notifyDataSetChanged();
                                }
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(bookRP.getMessage());
                            }
                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }
                        Utils.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<BookRP> call, Throwable t) {
                    Log.e("fail", t.toString());
                }
            });
        }
    }
}