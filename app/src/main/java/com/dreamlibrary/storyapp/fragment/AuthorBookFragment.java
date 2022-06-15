package com.dreamlibrary.storyapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.BookDetail;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.adapter.BookAdapterLV;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.response.AuthorDetailRP;
import com.dreamlibrary.storyapp.response.BookRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.EndlessRecyclerViewScrollListener;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorBookFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private Animation myAnim;
    private String title, id, type;
    private RecyclerView recyclerView;
    private List<BookList> bookLists;
    private BookAdapterLV bookAdapterLV;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private ConstraintLayout conNoData;
    private LayoutAnimationController animation;
    private MaterialTextView tvAuthName, textViewAuthorName;
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.author_book_fragment, container, false);

        assert getArguments() != null;
        title = getArguments().getString("title");
        id = getArguments().getString("id");
        type = getArguments().getString("type");

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(title);
        }

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) ->
                startActivity(new Intent(getActivity(), BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
        method = new Method(getActivity(), onClick);

        myAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        bookLists = new ArrayList<>();

        conNoData = view.findViewById(R.id.con_noDataFound);
        recyclerView = view.findViewById(R.id.recyclerView_authorBook_fragment);
        tvAuthName = view.findViewById(R.id.tvAuthName);
        textViewAuthorName = view.findViewById(R.id.textView_name_authorBook_fragment);

        conNoData.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        authorBook(id);
                    }, 1000);
                } else {
                    bookAdapterLV.hideHeader();
                }
            }
        });

        if (method.isNetworkAvailable()) {
            authorDetail(id);
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        setHasOptionsMenu(true);
        return view;

    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void authorDetail(String id) {

        if (getActivity() != null) {

            Utils.show(getContext());

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("author_id", id);
            jsObj.addProperty("method_name", "get_author_details");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<AuthorDetailRP> call = apiService.getAuthorDetail(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<AuthorDetailRP>() {
                @Override
                public void onResponse(@NotNull Call<AuthorDetailRP> call, @NotNull Response<AuthorDetailRP> response) {

                    if (getActivity() != null) {
                        try {
                            AuthorDetailRP authorDetailRP = response.body();
                            assert authorDetailRP != null;

                            if (authorDetailRP.getStatus().equals("1")) {
                                textViewAuthorName.setText(authorDetailRP.getAuthor_name());
                                tvAuthName.setText("Read all books of " + authorDetailRP.getAuthor_name());
                                authorBook(id);

                            } else {
                                method.alertBox(authorDetailRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<AuthorDetailRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    Utils.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    public void authorBook(String id) {

        if (getActivity() != null) {

            if (bookAdapterLV == null) {
                bookLists.clear();
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("author_id", id);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("method_name", "get_author_id");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<BookRP> call = apiService.getAuthorBook(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<BookRP>() {
                @Override
                public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                    if (getActivity() != null) {

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

                                if (bookAdapterLV == null) {
                                    if (bookLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        bookAdapterLV = new BookAdapterLV(getActivity(), bookLists, "author_by_list", onClick);
                                        recyclerView.setAdapter(bookAdapterLV);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    bookAdapterLV.notifyDataSetChanged();
                                }

                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(bookRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

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
    }
}