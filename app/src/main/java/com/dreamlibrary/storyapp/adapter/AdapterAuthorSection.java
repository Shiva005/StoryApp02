package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.AuthorSection;
import com.dreamlibrary.storyapp.item.ChildAuthorSection;
import com.dreamlibrary.storyapp.item.MainChildAuthorSection;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Method;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterAuthorSection extends RecyclerView.Adapter<AdapterAuthorSection.ViewHolder> {

    LinearLayoutManager layoutManager;
    private Method method;
    private Activity activity;
    private String type;
    private int columnWidth;
    private List<AuthorSection> authorSections;
    private List<ChildAuthorSection> childAuthorSections;
    private OnAdapterClick onAdapterClick;

    public AdapterAuthorSection(Activity activity, OnAdapterClick onAdapterClick, List<AuthorSection> authorSections, String type) {
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

        loadAlbums(authorSections.get(position).getId(), holder.recyclerViewAuthor, holder.titleText);
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

    private void loadAlbums(String homeSectionId, final RecyclerView recyclerView, final TextView textView) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("authorsection_id", homeSectionId);
        jsObj.addProperty("method_name", "author_section_id");
        jsObj.addProperty("page", "1");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MainChildAuthorSection> call = apiService.getChildAuthorSection(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<MainChildAuthorSection>() {
            @Override
            public void onResponse(@NotNull Call<MainChildAuthorSection> call, @NotNull Response<MainChildAuthorSection> response) {

                //if (getActivity() != null) {


                if (response.isSuccessful()) {

                    MainChildAuthorSection mainChildAuthorSection = response.body();

                    childAuthorSections = new ArrayList<>();
                    childAuthorSections.addAll(mainChildAuthorSection.getChildAuthorSectionList());

                    ChildAuthorAdapter childAuthorAdapter = new ChildAuthorAdapter(childAuthorSections);
                    recyclerView.setAdapter(childAuthorAdapter);

                }


                //}
            }

            @Override
            public void onFailure(@NotNull Call<MainChildAuthorSection> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                method.alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public interface OnAdapterClick {

        void seeAllClick(int position, AuthorSection authorSection, String type);

        void onAuthorClick(int position, ChildAuthorSection childAuthorSection, String type);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, viewAllText;
        RecyclerView recyclerViewAuthor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.titleText);
            viewAllText = itemView.findViewById(R.id.view_all);
            recyclerViewAuthor = itemView.findViewById(R.id.recyclerViewAuthor);


            layoutManager = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
            recyclerViewAuthor.setLayoutManager(layoutManager);
            recyclerViewAuthor.setItemAnimator(new DefaultItemAnimator());
            recyclerViewAuthor.setHasFixedSize(true);

        }
    }


    private class ChildAuthorAdapter extends RecyclerView.Adapter<ChildAuthorAdapter.ViewHolder> {


        List<ChildAuthorSection> childAuthorSections;

        public ChildAuthorAdapter(List<ChildAuthorSection> childAuthorSections) {
            this.childAuthorSections = childAuthorSections;
        }

        @NonNull
        @Override
        public ChildAuthorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.author_adapter, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ChildAuthorAdapter.ViewHolder holder, int position) {
            final ChildAuthorAdapter.ViewHolder viewHolder = (ChildAuthorAdapter.ViewHolder) holder;


           /* ConstraintLayout.LayoutParams coParams = new ConstraintLayout.LayoutParams(columnWidth / 3, columnWidth / 3 + 50);
            coParams.setMargins(20, 0, 20, 0);
            viewHolder.itemView.setLayoutParams(coParams);*/
            viewHolder.imageView.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth / 3, columnWidth / 3));

            viewHolder.textViewName.setText(childAuthorSections.get(position).getAuthorName());
            Glide.with(activity).load(childAuthorSections.get(position).getAuthorImage())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(viewHolder.imageView);


            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onAdapterClick.onAuthorClick(position, childAuthorSections.get(position), type);

                   /* AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("title", childAuthorSections.get(position).getAuthorName());
                    bundle.putString("id", childAuthorSections.get(position).getAuthorId());
                    bundle.putString("type", type);
                    authorBookFragment.setArguments(bundle);
                    fragmentManager.beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                            childAuthorSections.get(position).getAuthorName()).addToBackStack(childAuthorSections.get(position).getAuthorName()).commitAllowingStateLoss();*/
                }
            });

        }

        @Override
        public int getItemCount() {
            return childAuthorSections.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private ConstraintLayout constraintLayout;
            private MaterialTextView textViewName;

            public ViewHolder(View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView_author_adapter);
                constraintLayout = itemView.findViewById(R.id.con_author_adapter);
                textViewName = itemView.findViewById(R.id.textView_authorName_adapter);

            }
        }
    }


}
