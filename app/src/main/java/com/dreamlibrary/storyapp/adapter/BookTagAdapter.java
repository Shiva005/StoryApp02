package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.SearchBook;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class BookTagAdapter extends RecyclerView.Adapter<BookTagAdapter.ViewHolder> {

    private Activity activity;
    private List<String> bookTagList;

    public BookTagAdapter(Activity activity, List<String> bookTagList) {
        this.activity = activity;
        this.bookTagList = bookTagList;
    }

    @NonNull
    @Override
    public BookTagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookTagAdapter.ViewHolder(LayoutInflater.from(activity).inflate(R.layout.book_home_adapter4, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookTagAdapter.ViewHolder holder, int position) {
        holder.textViewName.setText(bookTagList.get(position));

        holder.cardView.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, SearchBook.class).putExtra("searchTag",bookTagList.get(position)));
        });
    }

    @Override
    public int getItemCount() {
        return bookTagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout cardView;
        private MaterialTextView textViewName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_book_home_adapter);
            textViewName = itemView.findViewById(R.id.textView_title_book_home_adapter);
        }
    }

}
