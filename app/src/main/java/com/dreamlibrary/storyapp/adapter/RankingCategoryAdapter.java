package com.dreamlibrary.storyapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.item.RankingCategoryModel;

import java.util.List;

public class RankingCategoryAdapter extends RecyclerView.Adapter<RankingCategoryAdapter.viewHolder> {
    private Context ctx;
    private List<RankingCategoryModel> categoryModels;
    int type;

    public RankingCategoryAdapter(Context ctx, List<RankingCategoryModel> categoryModels, int type) {
        this.ctx = ctx;
        this.categoryModels = categoryModels;
        this.type = type;
    }

    @NonNull
    @Override
    public RankingCategoryAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(type==0){
            view=LayoutInflater.from(ctx).inflate(R.layout.ranking_category_item, parent, false);
        }else if(type==1){
            view=LayoutInflater.from(ctx).inflate(R.layout.ranking_book_item, parent, false);
        }
        return new RankingCategoryAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingCategoryAdapter.viewHolder holder, int position) {
        final RankingCategoryModel categoryModel = categoryModels.get(position);
        holder.categoryTitle.setText(categoryModel.getCategoryTitle());
        Glide.with(ctx).load(categoryModel.getCategoryImage()).placeholder(R.drawable.no_data).into(holder.categoryImage);

    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private ImageView categoryImage;
        private TextView categoryTitle;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.iv_rankingCatImage);
            categoryTitle = itemView.findViewById(R.id.txt_rankingCatName);
        }
    }
}
