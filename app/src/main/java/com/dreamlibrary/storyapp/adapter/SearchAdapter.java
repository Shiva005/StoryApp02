package com.dreamlibrary.storyapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.util.Method;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter {

    private Method method;
    private Activity activity;
    private String type;
    private int columnWidth;
    private List<BookList> bookLists;

    public SearchAdapter(Activity activity, List<BookList> bookLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.bookLists = bookLists;
        method = new Method(activity, onClick);
        Resources r = activity.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((4 + 3) * padding)));
    }

    public void setArrayList(List<BookList> bookLists) {
        this.bookLists = bookLists;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(activity).inflate(R.layout.book_gridview_adapter, parent, false);
        return new ViewHolder(view);

    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {


        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.cardView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth / 3, columnWidth / 2));

        viewHolder.textViewName.setText(bookLists.get(position).getBook_title());

        String authorName = "";

        if(bookLists.get(position).getAuthorArrayList().size()==1){
            authorName = authorName + bookLists.get(position).getAuthorArrayList().get(0).getAuthorName();
        }else {
            for (int i = 0; i < bookLists.get(position).getAuthorArrayList().size(); i++) {
                authorName = authorName + bookLists.get(position).getAuthorArrayList().get(i).getAuthorName() + ",  ";
            }
        }

        Glide.with(activity).load(bookLists.get(position).getBook_cover_img())
                .placeholder(R.drawable.placeholder_portable)
                .into(viewHolder.imageView);

        viewHolder.cardView.setOnClickListener(v -> method.onClickAd(position, type, bookLists.get(position).getId(), "", "", "", "", ""));

    }

    @Override
    public int getItemCount() {
        return bookLists.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar_loading);
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private MaterialCardView cardView;
        private MaterialTextView textViewName;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_bookGrid_adapter);
            cardView = itemView.findViewById(R.id.cardView_bookGrid_adapter);
            textViewName = itemView.findViewById(R.id.textView_title_bookGrid_adapter);

        }
    }
}
