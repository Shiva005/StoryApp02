package com.dreamlibrary.storyapp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.ChildHomeSection;
import com.dreamlibrary.storyapp.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class HomeSectionVerticalAdapter extends RecyclerView.Adapter<HomeSectionVerticalAdapter.ViewHolder> {

  /*  private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;*/
    Context context;
    List<ChildHomeSection> childHomeSections;
    OnAdapterClick onAdapterClick;

    public HomeSectionVerticalAdapter(Context context, List<ChildHomeSection> childHomeSections, OnAdapterClick onAdapterClick) {
        this.context = context;
        this.childHomeSections = childHomeSections;
        this.onAdapterClick = onAdapterClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*if (viewType == VIEW_TYPE_ITEM) {*/
            View view = LayoutInflater.from(context).inflate(R.layout.book_listview_adapter, parent, false);
            return new HomeSectionVerticalAdapter.ViewHolder(view);
      /*  } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;*/
    }

    @Override
    public void onBindViewHolder(HomeSectionVerticalAdapter.ViewHolder holder, int position) {
/*        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {*/

            Glide.with(context).load(childHomeSections.get(position).getBookCoverImg())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(holder.imageView);

            holder.textViewName.setText(childHomeSections.get(position).getBookTitle());
            holder.textViewAuthor.setText(childHomeSections.get(position).getAuthorName());
            holder.ratingBar.setRating(Float.parseFloat(childHomeSections.get(position).getRateAverage()));
            holder.textViewRatingCount.setText(childHomeSections.get(position).getTotalRate());
            holder.textViewViewCount.setText(Method.Format(Integer.parseInt(childHomeSections.get(position).getBookViews())));
            holder.textViewDescription.setText(Html.fromHtml(childHomeSections.get(position).getBookDescription()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAdapterClick.OnClickItem(position, childHomeSections.get(position), "");
                }
            });

       /* }*/
    }

    @Override
    public int getItemCount() {
        return childHomeSections.size();
    }

 /*   public void hideHeader() {
        BookAdapterLV.ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemViewType(int position) {
        if (childHomeSections.size() == position) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar_loading);
        }
    }*/

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private RatingView ratingBar;
        private ConstraintLayout constraintLayout;
        private MaterialTextView textViewName, textViewAuthor, textViewRatingCount, textViewViewCount, textViewDescription;


        public ViewHolder(View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.con_bookListView_adapter);
            imageView = itemView.findViewById(R.id.imageView_bookListView_adapter);
            textViewName = itemView.findViewById(R.id.textView_name_bookListView_adapter);
            textViewAuthor = itemView.findViewById(R.id.textView_author_bookListView_adapter);
            ratingBar = itemView.findViewById(R.id.ratingBar_bookListView_adapter);
            textViewRatingCount = itemView.findViewById(R.id.textView_ratingCount_bookListView_adapter);
            textViewViewCount = itemView.findViewById(R.id.textView_view_bookListView_adapter);
            textViewDescription = itemView.findViewById(R.id.textView_description_bookListView_adapter);

        }
    }

    public interface OnAdapterClick {
        void OnClickItem(int position, ChildHomeSection childHomeSection, String type);
    }
}
