package com.dreamlibrary.storyapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.ChildHomeSection;
import com.dreamlibrary.storyapp.item.HomeSection;
import com.dreamlibrary.storyapp.item.MainChildHomeSection;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterSearchSection extends RecyclerView.Adapter<AdapterSearchSection.ViewHolder> {

    GridLayoutManager layoutManager;
    private Method method;
    private Activity activity;
    private String type;
    private int columnWidth;
    private List<HomeSection> authorSections;
    private List<ChildHomeSection> childAuthorSections;
    private OnAdapterClick onAdapterClick;
    int albumPosition = -1;

    public AdapterSearchSection(Activity activity, OnAdapterClick onAdapterClick, List<HomeSection> authorSections, String type) {
        this.activity = activity;
        this.type = type;
        this.authorSections = authorSections;
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
        loadAlbums(authorSections.get(position).getId(), holder.recyclerViewAuthor, holder.titleText, albumPosition);
        holder.titleText.setTypeface(holder.titleText.getTypeface(), Typeface.BOLD);

        holder.titleText.setText(authorSections.get(position).getSectionTitle());

        holder.viewAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdapterClick.seeAllClick(position, authorSections.get(position), type);
            }
        });

    }

    @Override
    public int getItemCount() {
        return authorSections.size();
    }

    private void loadAlbums(String homeSectionId, final RecyclerView recyclerView, final TextView textView,int albumPosition) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("searchsection_id", homeSectionId);
        jsObj.addProperty("method_name", "search_section_id");
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

                    ChildHomeSectionAdapter childAuthorAdapter = new ChildHomeSectionAdapter(childAuthorSections,activity, albumPosition, type);
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
            viewAllText.setVisibility(View.GONE);

            if (albumPosition == 0) {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.HORIZONTAL, false);
            }else if (albumPosition == 1) {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false);
            } else if (albumPosition == 2) {
                layoutManager = new GridLayoutManager(activity, 3, RecyclerView.HORIZONTAL, false);
            }else if (albumPosition == 3) {
                layoutManager = new GridLayoutManager(activity, 1, RecyclerView.HORIZONTAL, false);
            }else if (albumPosition == 4) {
                layoutManager = new GridLayoutManager(activity, 2, RecyclerView.HORIZONTAL, false);
            }else if (albumPosition == 5) {
                layoutManager = new GridLayoutManager(activity, 2, RecyclerView.HORIZONTAL, false);
            }else {
                layoutManager = new GridLayoutManager(activity, 1, RecyclerView.VERTICAL, false);
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


    private class ChildHomeSectionAdapter extends RecyclerView.Adapter<ChildHomeSectionAdapter.ViewHolder> {


        List<ChildHomeSection> childHomeSections;
        private Activity activity;
        private int pos;
        private String type;

        public ChildHomeSectionAdapter(List<ChildHomeSection> childHomeSections, Activity activity, int pos, String type) {
            this.childHomeSections = childHomeSections;
            this.activity = activity;
            this.pos = pos;
            this.type = type;
        }

        @NotNull
        @Override
        public ChildHomeSectionAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

            if (pos == 0) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter, parent, false);
                return new ViewHolder(mView);
            } else if (pos-1 == 0) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter2, parent, false);
                return new ViewHolder(mView);
            } else if (pos-1 == 1) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter3, parent, false);
                return new ViewHolder(mView);
            }else if (pos-1 == 2) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter5, parent, false);
                return new ViewHolder(mView);
            }else if (pos-1 == 3) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter4, parent, false);
                return new ViewHolder(mView);
            }else if (pos-1 == 4) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter2, parent, false);
                return new ViewHolder(mView);
            }else if (pos-1 == 5) {
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter4, parent, false);
                return new ViewHolder(mView);
            }else{
                View mView = LayoutInflater.from(activity).inflate(R.layout.book_listview_adapter, parent, false);
                return new ViewHolder(mView);
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
            holder.textViewViewDescription.setText(childHomeSections.get(position).getBookViews());

            if (!childHomeSections.get(position).getBookCoverImg().equals("")) {
                Glide.with(activity).load(childHomeSections.get(position).getBookCoverImg())
                        .placeholder(R.drawable.placeholder_portable)
                        .into(holder.imageView);
            }

            if (holder.cardView!= null)
            holder.cardView.setOnClickListener(v -> {
                onAdapterClick.onHomeClick(position, childHomeSections.get(position), type);
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
            private MaterialTextView textViewName, textViewAuthor, textViewDescription,textViewViewDescription,textViewRatingCount;


            public ViewHolder(View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView_bookListView_adapter);
                cardView = itemView.findViewById(R.id.cardView_imageView_bookListView_adapter);
                textViewName = itemView.findViewById(R.id.textView_name_bookListView_adapter);
                textViewAuthor = itemView.findViewById(R.id.textView_author_bookListView_adapter);
                textViewDescription = itemView.findViewById(R.id.textView_description_bookListView_adapter);
                textViewViewDescription = itemView.findViewById(R.id.textView_view_bookListView_adapter);
                ratingBar = itemView.findViewById(R.id.ratingBar_bookListView_adapter);
                textViewRatingCount = itemView.findViewById(R.id.textView_ratingCount_bookListView_adapter);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
