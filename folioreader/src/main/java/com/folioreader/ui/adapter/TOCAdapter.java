package com.folioreader.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.TOCLinkWrapper;
import com.folioreader.util.MultiLevelExpIndListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;

import kotlin.reflect.jvm.internal.impl.load.java.Constant;

/**
 * Created by mahavir on 3/10/17.
 */

public class TOCAdapter extends MultiLevelExpIndListAdapter {

    private static final int LEVEL_ONE_PADDING_PIXEL = 15;
    private static final int freePersantage = 70;
    private static  int perValue = 0;

    private TOCCallback callback;
    private final Context mContext;
    private String selectedHref;
    private Config mConfig;
    public static int selectedPos  =0;

    ArrayList<TOCLinkWrapper> tocLinkWrapperss;

    InterstitialAd interstitialAd = null;
   void  loadAds(){

       AdRequest adRequest = new AdRequest.Builder()
               .build();

       InterstitialAd.load(((Activity)mContext), mContext.getResources().getString(R.string.interstitialAds), adRequest, new InterstitialAdLoadCallback() {
           @Override
           public void onAdLoaded(@NonNull InterstitialAd interstitialAds) {
               interstitialAd = interstitialAds;

           }

           @Override
           public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
               // Handle the error
               }
       });

   }

    public TOCAdapter(Context context, ArrayList<TOCLinkWrapper> tocLinkWrappers,
                      String selectedHref, Config config) {
        super(tocLinkWrappers);
        tocLinkWrapperss = tocLinkWrappers;
        mContext = context;
        this.selectedHref = selectedHref;
        this.mConfig = config;



        perValue = (tocLinkWrapperss.size() * freePersantage) /100;
        loadAds();
    }

    public void setCallback(TOCCallback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TOCRowViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_table_of_contents, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TOCRowViewHolder viewHolder = (TOCRowViewHolder) holder;
        TOCLinkWrapper tocLinkWrapper = (TOCLinkWrapper) getItemAt(position);

        if (tocLinkWrapper.getChildren() == null || tocLinkWrapper.getChildren().isEmpty()) {
            viewHolder.children.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.children.setVisibility(View.VISIBLE);
        }
        viewHolder.sectionTitle.setText(tocLinkWrapper.getTocLink().getTitle());

        if (mConfig.isNightMode()) {
            if (tocLinkWrapper.isGroup()) {
                viewHolder.children.setImageResource(R.drawable.ic_plus_white_24dp);
            } else {
                viewHolder.children.setImageResource(R.drawable.ic_minus_white_24dp);
            }
        } else {
            if (tocLinkWrapper.isGroup()) {
                viewHolder.children.setImageResource(R.drawable.ic_plus_black_24dp);
            } else {
                viewHolder.children.setImageResource(R.drawable.ic_minus_black_24dp);
            }
        }

        int leftPadding = getPaddingPixels(mContext, LEVEL_ONE_PADDING_PIXEL) * (tocLinkWrapper.getIndentation());
        viewHolder.view.setPadding(leftPadding, 0, 0, 0);

        // set color to each indentation level
        if (tocLinkWrapper.getIndentation() == 0) {
            viewHolder.view.setBackgroundColor(Color.WHITE);
            viewHolder.sectionTitle.setTextColor(Color.BLACK);
        } else if (tocLinkWrapper.getIndentation() == 1) {
            viewHolder.view.setBackgroundColor(Color.parseColor("#f7f7f7"));
            viewHolder.sectionTitle.setTextColor(Color.BLACK);
        } else if (tocLinkWrapper.getIndentation() == 2) {
            viewHolder.view.setBackgroundColor(Color.parseColor("#b3b3b3"));
            viewHolder.sectionTitle.setTextColor(Color.WHITE);
        } else if (tocLinkWrapper.getIndentation() == 3) {
            viewHolder.view.setBackgroundColor(Color.parseColor("#f7f7f7"));
            viewHolder.sectionTitle.setTextColor(Color.BLACK);
        }

        if (tocLinkWrapper.getChildren() == null || tocLinkWrapper.getChildren().isEmpty()) {
            viewHolder.children.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.children.setVisibility(View.VISIBLE);
        }

//        if (mConfig.isNightMode()) {
//            viewHolder.container.setBackgroundColor(ContextCompat.getColor(mContext,
//                    R.color.black));
////            viewHolder.layBack.setBackgroundResource(R.drawable.night_bg);
//
//
//            viewHolder.children.setBackgroundColor(ContextCompat.getColor(mContext,
//                    R.color.black));
//            viewHolder.sectionTitle.setTextColor(ContextCompat.getColor(mContext,
//                    R.color.white));
//        } else {
//            viewHolder.container.setBackgroundColor(ContextCompat.getColor(mContext,
//                    R.color.white));
//            viewHolder.children.setBackgroundColor(ContextCompat.getColor(mContext,
//                    R.color.white));
//            viewHolder.sectionTitle.setTextColor(ContextCompat.getColor(mContext,
//                    R.color.black));
//
////            viewHolder.layBack.setBackgroundResource(R.drawable.white_bg);
//        }

        Log.e("link-->>", tocLinkWrapper.getTocLink().getHref() + "<<>>" + selectedHref);

        if (tocLinkWrapper.getTocLink().getHref().equalsIgnoreCase(selectedHref)) {
            viewHolder.sectionTitle.setTextColor(mConfig.getThemeColor());
        }


        if (position == selectedPos) {
            viewHolder.sectionTitle.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        } else {
            viewHolder.sectionTitle.setTextColor(mContext.getResources().getColor(R.color.black));
        }



        if (position > perValue){
            viewHolder.imgAds.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ads));
        }
        else{
            viewHolder.imgAds.setImageDrawable(mContext.getResources().getDrawable(R.drawable.video_camera));

        }

    }

    public interface TOCCallback {
        void onTocClicked(int position);

        void onExpanded(int position);
    }

    public class TOCRowViewHolder extends RecyclerView.ViewHolder {
        public ImageView children,imgAds;
        TextView sectionTitle;
        private LinearLayout container, layBack;
        private View view;

        TOCRowViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imgAds = (ImageView) itemView.findViewById(R.id.imgAds);
            children = (ImageView) itemView.findViewById(R.id.children);
            layBack = (LinearLayout) itemView.findViewById(R.id.layBack);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            children.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPos = getAdapterPosition();
                    if (callback != null) callback.onExpanded(getAdapterPosition());
                }
            });

            sectionTitle = (TextView) itemView.findViewById(R.id.section_title);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPos = getAdapterPosition();

                    if (getAdapterPosition() > perValue){

                        if (interstitialAd != null){
                            interstitialAd.show(((Activity)mContext));
                            if (callback != null) callback.onTocClicked(getAdapterPosition());
                        }
                        else{
                            if (callback != null) callback.onTocClicked(getAdapterPosition());
                        }

                    }
                    else{
                        if (callback != null) callback.onTocClicked(getAdapterPosition());

                    }


                }
            });
        }
    }

    private static int getPaddingPixels(Context context, int dpValue) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dpValue * scale + 0.5f);
    }
}
