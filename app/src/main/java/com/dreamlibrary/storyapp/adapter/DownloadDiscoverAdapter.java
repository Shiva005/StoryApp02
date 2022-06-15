package com.dreamlibrary.storyapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.database.DatabaseHandler;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.DownloadList;
import com.dreamlibrary.storyapp.util.Method;
import com.folioreader.util.RecordPref;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

public class DownloadDiscoverAdapter extends RecyclerView.Adapter<DownloadDiscoverAdapter.ViewHolder> {

    Context context;
    OnDownloadClick onDownloadClick;
    private Method method;
    private List<DownloadList> downloadLists;
    private Animation myAnim;
    private Activity activity;
    private DatabaseHandler db;
    private String type;
    private RecordPref recordPref;

    public DownloadDiscoverAdapter(Context context, Activity activity, List<DownloadList> downloadLists, OnClick onClick, String type) {
        this.context = context;
        this.activity = activity;
        this.downloadLists = downloadLists;
        //this.onDownloadClick = onDownloadClick;
        this.type = type;
        db = new DatabaseHandler(activity);
        method = new Method(activity, onClick);
        recordPref = new RecordPref(activity);
        myAnim = AnimationUtils.loadAnimation(activity, R.anim.bounce);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.download_item, parent, false));
    }

    @Override
    public void onBindViewHolder(DownloadDiscoverAdapter.ViewHolder holder, int position) {

        Glide.with(context).load("file://" + downloadLists.get(position).getImage()).placeholder(R.drawable.placeholder_portable).into(holder.book_image);
        holder.title_text.setText(downloadLists.get(position).getTitle());
        holder.author_text.setText(downloadLists.get(position).getAuthor());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.onClickAd(position, type, downloadLists.get(position).getId(), "", downloadLists.get(position).getTitle(), "", downloadLists.get(position).getUrl(), "");
            }
        });

        holder.right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.right_btn.startAnimation(myAnim);

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                builder.setMessage(activity.getResources().getString(R.string.delete_msg));
                builder.setCancelable(false);
                builder.setPositiveButton(activity.getResources().getString(R.string.delete),
                        (arg0, arg1) -> {

                            if (!db.checkIdDownloadBook(downloadLists.get(position).getId())) {
                                db.deleteDownloadBook(downloadLists.get(position).getId());
                                File file = new File(downloadLists.get(position).getUrl());
                                File file_image = new File(downloadLists.get(position).getImage());
                                file.delete();
                                file_image.delete();
                                downloadLists.remove(position);
                                recordPref.countDownload(false);
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(activity, activity.getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                            }

                        });
                builder.setNegativeButton(activity.getResources().getString(R.string.cancel),
                        (dialog, which) -> dialog.dismiss());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return downloadLists.size();
    }

    public interface OnDownloadClick {
        void OnClickItem(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView book_image, right_btn;
        TextView title_text, author_text;

        public ViewHolder(View itemView) {
            super(itemView);

            book_image = itemView.findViewById(R.id.book_image);
            right_btn = itemView.findViewById(R.id.right_btn);
            title_text = itemView.findViewById(R.id.title_text);
            author_text = itemView.findViewById(R.id.author_text);
            right_btn.setImageResource(R.drawable.ic_round_delete_icon);
        }
    }

}
