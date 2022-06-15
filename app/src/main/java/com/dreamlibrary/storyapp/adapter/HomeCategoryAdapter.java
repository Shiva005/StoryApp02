package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<BookList> subCategoryLists;
    private OnClickRv onClickRv;

    public HomeCategoryAdapter(Activity activity, List<BookList> subCategoryLists, String type, OnClickRv onClickRv) {
        this.activity = activity;
        this.subCategoryLists = subCategoryLists;
        this.type = type;
        this.onClickRv = onClickRv;
        method = new Method(activity);
    }

    @NonNull
    @Override
    public HomeCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeCategoryAdapter.ViewHolder(LayoutInflater.from(activity).inflate(R.layout.book_home_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeCategoryAdapter.ViewHolder holder, int position) {
        holder.textViewName.setText(subCategoryLists.get(position).getBook_title());


        String authorName = "";

        if (subCategoryLists.get(position).getAuthorArrayList().size() == 1) {
            authorName = authorName + subCategoryLists.get(position).getAuthorArrayList().get(0).getAuthorName();
        } else {
            for (int i = 0; i < subCategoryLists.get(position).getAuthorArrayList().size(); i++) {
                authorName = authorName + subCategoryLists.get(position).getAuthorArrayList().get(i).getAuthorName() + ",  ";
            }
        }

        holder.textViewAuthor.setText(activity.getString(R.string.by) + "\u0020" + method.getAuthorFormatted(authorName));


//        holder.textViewRatingCount.setText(subCategoryLists.get(position).getTotal_rate());
//        holder.ratingBar.setRating(Float.parseFloat(subCategoryLists.get(position).getRate_avg()));

        if (!subCategoryLists.get(position).getBook_cover_img().equals("")) {
            Glide.with(activity).load(subCategoryLists.get(position).getBook_cover_img())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(holder.imageView);
        }

        holder.cardView.setOnClickListener(v -> {
            onClickRv.onClick(position, subCategoryLists.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return subCategoryLists.size();
    }

    public interface OnClickRv {
        void onClick(int position, BookList bookList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private RatingView ratingBar;
        private MaterialCardView cardView;
        private RelativeLayout rRoot;
        private MaterialTextView textViewName, textViewAuthor, textViewRatingCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_book_home_adapter);
            cardView = itemView.findViewById(R.id.cardView_book_home_adapter);
            rRoot = itemView.findViewById(R.id.rRoot);
            textViewName = itemView.findViewById(R.id.textView_title_book_home_adapter);
            textViewAuthor = itemView.findViewById(R.id.textView_author_book_home_adapter);
            ratingBar = itemView.findViewById(R.id.ratingBar_book_home_adapter);
            textViewRatingCount = itemView.findViewById(R.id.textView_ratingCount_book_home_adapter);
        }
    }

}
