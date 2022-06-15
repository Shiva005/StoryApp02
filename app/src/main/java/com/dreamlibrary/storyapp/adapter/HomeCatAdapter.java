package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.CategoryList;
import com.dreamlibrary.storyapp.util.Method;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeCatAdapter extends RecyclerView.Adapter<HomeCatAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<CategoryList> categoryLists;

    public HomeCatAdapter(Activity activity, List<CategoryList> categoryLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.categoryLists = categoryLists;
        method = new Method(activity, onClick);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.home_category_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.textView.setText(categoryLists.get(position).getCategory_name());
        //String itemCount = activity.getResources().getString(R.string.items) + " " + "(" + categoryLists.get(position).getTotal_books() + ")";
        //holder.textViewItem.setText(itemCount);

        Glide.with(activity).load(categoryLists.get(position).getCat_image_thumb())
                .placeholder(R.drawable.placeholder_portable)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.cardView.setCardBackgroundColor(method.getDominantColor(drawableToBitmap(resource)));
                        return true;
                    }
                }).into(holder.imageView);



        holder.cardView.setOnClickListener(v -> {
            method.onClickAd(position, type, categoryLists.get(position).getCid(), "0", categoryLists.get(position).getCategory_name(), "", "", categoryLists.get(position).getSub_cat_status());
        });

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return categoryLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private MaterialCardView cardView;
        private MaterialTextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_homeCategory_adapter);
            textView = itemView.findViewById(R.id.textView_homeCategory_adapter);
            //textViewItem = itemView.findViewById(R.id.textView_item_homeCategory_adapter);
            cardView = itemView.findViewById(R.id.cardView_homeCategory_adapter);
            //textViewItem.setVisibility(View.GONE);
            imageView.setVisibility(View.INVISIBLE);
        }
    }
}
