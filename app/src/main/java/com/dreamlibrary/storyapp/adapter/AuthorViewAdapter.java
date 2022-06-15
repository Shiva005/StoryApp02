package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.ChildAuthorSection;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class AuthorViewAdapter extends RecyclerView.Adapter<AuthorViewAdapter.ViewHolder> {

    Context context;
    Activity activity;
    List<ChildAuthorSection> childAuthorSections;
    OnAdapterItemClick onAdapterItemClick;

    public AuthorViewAdapter(Context context, Activity activity, List<ChildAuthorSection> childAuthorSections, OnAdapterItemClick onAdapterItemClick) {
        this.context = context;
        this.activity = activity;
        this.childAuthorSections = childAuthorSections;
        this.onAdapterItemClick = onAdapterItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.author_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(AuthorViewAdapter.ViewHolder holder, int position) {



    }

    @Override
    public int getItemCount() {
        return childAuthorSections.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView_author_adapter;
        MaterialTextView textView_authorName_adapter;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView_author_adapter = itemView.findViewById(R.id.textView_authorName_adapter);
            textView_authorName_adapter = itemView.findViewById(R.id.textView_authorName_adapter);

        }
    }

    public interface OnAdapterItemClick {
        void onAuthorClick(int position, ChildAuthorSection childAuthorSection, String type);
    }
}
