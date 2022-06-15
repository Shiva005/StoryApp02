package com.dreamlibrary.storyapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dreamlibrary.storyapp.R;
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
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Author extends AppCompatActivity {
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
    private MaterialTextView textViewAuthorName;
    private ImageView authorBackgroundImage, authorProfileImage, backPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        id = getIntent().getStringExtra("id");

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(title);
        }

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) ->
                startActivity(new Intent(this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
        method = new Method(this, onClick);

        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(this, resId);

        bookLists = new ArrayList<>();

        conNoData = findViewById(R.id.con_noDataFound);
        recyclerView = findViewById(R.id.recyclerView_authorBook_fragment);
        textViewAuthorName = findViewById(R.id.textView_name_authorBook_fragment);
        authorBackgroundImage = findViewById(R.id.imageView_background_author);
        authorProfileImage = findViewById(R.id.imageView_authorImage_fragment);
        backPress = findViewById(R.id.imageView_backPress);

        backPress.setOnClickListener(v -> super.onBackPressed());

        conNoData.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

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
            //authorBook(id);
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    private void authorDetail(String id) {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(this));
        jsObj.addProperty("author_id", id);
        jsObj.addProperty("method_name", "get_author_details");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<AuthorDetailRP> call = apiService.getAuthorDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<AuthorDetailRP>() {
            @Override
            public void onResponse(@NotNull Call<AuthorDetailRP> call, @NotNull Response<AuthorDetailRP> response) {
                try {
                    AuthorDetailRP authorDetailRP = response.body();
                    assert authorDetailRP != null;

                    if (authorDetailRP.getStatus().equals("1")) {
                        textViewAuthorName.setText(authorDetailRP.getAuthor_name());
                        Glide.with(Author.this).load(authorDetailRP.getAuthor_image()).placeholder(R.drawable.placeholder_portable).into(authorProfileImage);
                        Glide.with(Author.this)
                                .load(authorDetailRP.getAuthor_image())
                                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                                .placeholder(R.drawable.placeholder_portable)
                                .into(authorBackgroundImage);

                        authorBook(authorDetailRP.getAuthor_id());

                    } else {
                        method.alertBox(authorDetailRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
                Utils.dismiss();
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

    public void authorBook(String id) {

        if (bookAdapterLV == null) {
            bookLists.clear();
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(this));
        jsObj.addProperty("author_id", id);
        jsObj.addProperty("page", paginationIndex);
        jsObj.addProperty("ads_param", adsParam);
        jsObj.addProperty("method_name", "get_author_id");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookRP> call = apiService.getAuthorBook(API.toBase64(jsObj.toString()));
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

                        if (bookAdapterLV == null) {
                            if (bookLists.size() == 0) {
                                conNoData.setVisibility(View.VISIBLE);
                            } else {
                                bookAdapterLV = new BookAdapterLV(Author.this, bookLists, "author_by_list", onClick);
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
