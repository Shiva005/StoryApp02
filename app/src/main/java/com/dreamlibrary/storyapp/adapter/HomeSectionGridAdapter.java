package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.ChildHomeSection;
import com.dreamlibrary.storyapp.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class HomeSectionGridAdapter extends RecyclerView.Adapter<HomeSectionGridAdapter.ViewHolder> {

    Context context;
    List<ChildHomeSection> childHomeSections;
    OnAdapterClick onAdapterClick;
    private int columnWidth;
    private Method method;
    private Activity activity;


    public HomeSectionGridAdapter(Context context,Activity activity, List<ChildHomeSection> childHomeSections, OnAdapterClick onAdapterClick) {
        this.context = context;
        this.activity = activity;
        this.childHomeSections = childHomeSections;
        this.onAdapterClick = onAdapterClick;
        method = new Method(activity);
        Resources r = activity.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((4 + 3) * padding)));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_gridview_adapter, parent, false);
        return new HomeSectionGridAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeSectionGridAdapter.ViewHolder holder, int position) {

        holder.cardView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth / 3, columnWidth / 2));
        Glide.with(context).load(childHomeSections.get(position).getBookCoverImg())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView);

        holder.textViewName.setText(childHomeSections.get(position).getBookTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdapterClick.OnClickItem(position, childHomeSections.get(position), "");
            }
        });

    }

    @Override
    public int getItemCount() {
        return childHomeSections.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private MaterialTextView textViewName;
        private MaterialCardView cardView;


        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_bookGrid_adapter);
            textViewName = itemView.findViewById(R.id.textView_title_bookGrid_adapter);
            cardView = itemView.findViewById(R.id.cardView_bookGrid_adapter);
        }
    }

    public interface OnAdapterClick {
        void OnClickItem(int position, ChildHomeSection childHomeSection, String type);
    }
}
