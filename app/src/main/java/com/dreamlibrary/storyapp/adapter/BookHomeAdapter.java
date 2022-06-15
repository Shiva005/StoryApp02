package com.dreamlibrary.storyapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.database.DatabaseHandler;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BookHomeAdapter extends RecyclerView.Adapter<BookHomeAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<BookList> subCategoryLists;
    private DatabaseHandler databaseHandler;
    private String TAG_CONTINUE = "home_continue";
    BookHomeAdapterInterface bookHomeAdapterInterface;

    boolean isFromContinue = false;

    public BookHomeAdapter(Activity activity, List<BookList> subCategoryLists, String type, OnClick onClick) {
        this.activity = activity;
        this.subCategoryLists = subCategoryLists;
        this.type = type;
        method = new Method(activity, onClick);
        databaseHandler = new DatabaseHandler(activity);
        isFromContinue = false;
    }

    public BookHomeAdapter(Activity activity, List<BookList> subCategoryLists, String type, OnClick onClick, BookHomeAdapterInterface bookHomeAdapterInterface) {
        this.activity = activity;
        this.subCategoryLists = subCategoryLists;
        this.type = type;
        method = new Method(activity, onClick);
        databaseHandler = new DatabaseHandler(activity);
        this.bookHomeAdapterInterface = bookHomeAdapterInterface;
        isFromContinue = true;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View mView = null;

        if (type.equals(TAG_CONTINUE)) {
            mView = LayoutInflater.from(activity).inflate(R.layout.continue_book_adapter, parent, false);
        } else {
            mView = LayoutInflater.from(activity).inflate(R.layout.book_home_latest, parent, false);
        }

        //View view = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter, parent, false);

        return new ViewHolder(mView);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {

        holder.textViewName.setText(subCategoryLists.get(position).getBook_title());

        if (type.equals(TAG_CONTINUE)) {

            int percentage = method.getPercentage(databaseHandler.getPdf(subCategoryLists.get(position).getId()), databaseHandler.getPdfTotalPage(subCategoryLists.get(position).getId()));

            //holder.textProgress.setText(percentage + "%");
            //holder.progressBar.setProgress(percentage);

        }


        String authorName = "";

        if (subCategoryLists.get(position).getAuthorArrayList().size() == 1) {
            authorName = authorName + subCategoryLists.get(position).getAuthorArrayList().get(0).getAuthorName();
        } else {
            for (int i = 0; i < subCategoryLists.get(position).getAuthorArrayList().size(); i++) {
                authorName = authorName + subCategoryLists.get(position).getAuthorArrayList().get(i).getAuthorName() + ",  ";
            }
        }

        holder.textViewAuthor.setText(subCategoryLists.get(position).getBook_complt());
        holder.textViewTag.setText(subCategoryLists.get(position).getBook_tag());

        /*if (type.equals(TAG_CONTINUE)) {
            holder.textViewRatingCount.setText(method.getTextWithRating(subCategoryLists.get(position).getRate_avg()));
        } else {
            holder.textViewRatingCount.setText(subCategoryLists.get(position).getTotal_rate());
            holder.ratingBar.setRating(Float.parseFloat(subCategoryLists.get(position).getRate_avg()));
        }*/

        if (!subCategoryLists.get(position).getBook_cover_img().equals("")) {
            Glide.with(activity).load(subCategoryLists.get(position).getBook_cover_img())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(holder.imageView);
        }

        holder.cardView.setOnClickListener(v -> method.onClickAd(position, type, subCategoryLists.get(position).getId(), "", "", "", "", ""));

    }

    @Override
    public int getItemCount() {
        return subCategoryLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private RatingView ratingBar;
        private LinearLayout cardView;
        private RelativeLayout rRoot;
        private MaterialTextView textViewName;
        private TextView textProgress, textViewAuthor, textViewDesc,textViewTag;
        private ProgressBar progressBar;


        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_book_home_adapter);
            cardView = itemView.findViewById(R.id.cardView_book_home_adapter);
            rRoot = itemView.findViewById(R.id.rRoot);
            textViewName = itemView.findViewById(R.id.textView_title_book_home_adapter);
            textViewAuthor = itemView.findViewById(R.id.textView_author_book_home_adapter);
            textViewDesc = itemView.findViewById(R.id.textView_description_book_home_adapter);
            textViewTag = itemView.findViewById(R.id.textView_tag_book_home_adapter);
            ratingBar = itemView.findViewById(R.id.ratingBar_book_home_adapter);

            if (type.equals(TAG_CONTINUE)) {

                //textProgress = itemView.findViewById(R.id.textProgress);
                progressBar = itemView.findViewById(R.id.progressBar);

                //ratingBar.setVisibility(View.GONE);
                //textViewAuthor.setSelected(true);
            }
        }
    }

    public interface BookHomeAdapterInterface {
        void onBookFinishClick(int position);
    }

}
