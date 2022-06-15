package com.dreamlibrary.storyapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.Subscription;
import com.dreamlibrary.storyapp.item.SubscriptionModel;

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.viewHolder> {
    private Context ctx;
    private List<SubscriptionModel> subscriptionModels;
    private String type;

    public SubscriptionAdapter(Context ctx, List<SubscriptionModel> subscriptionModels, String type) {
        this.ctx = ctx;
        this.subscriptionModels = subscriptionModels;
        this.type = type;
    }

    @NonNull
    @Override
    public SubscriptionAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_premium_layout, parent, false);
        return new SubscriptionAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionAdapter.viewHolder holder, int position) {
        final SubscriptionModel subscriptionModel = subscriptionModels.get(position);
        holder.planName.setText(subscriptionModel.getPlanName());
        if(type.equals("indian")) {
            holder.planAmount.setText("₹" + subscriptionModel.getPlanAmount());
        }else{
            holder.planAmount.setText("$" + (int)Math.ceil(Double.parseDouble(subscriptionModel.getPlanAmount())/76));
        }
        holder.subscription.setText(subscriptionModel.getSubscription());
        holder.banner.setText(subscriptionModel.getBanner());
        holder.interstitial.setText(subscriptionModel.getInterstitial());
        holder.video.setText(subscriptionModel.getVideo());
        holder.subscribeNow.setOnClickListener(v->{
            if(type.equals("indian")) {
                ((Subscription) ctx).startPayment(Integer.parseInt(subscriptionModel.getPlanAmount()));
            }else {
                ((Subscription) ctx).getClientSecret((int)Math.ceil(Double.parseDouble(subscriptionModel.getPlanAmount())/76));
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionModels.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView planName, planAmount, subscription, banner, interstitial, video;
        private Button subscribeNow;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            planName = itemView.findViewById(R.id.txt_planName);
            planAmount = itemView.findViewById(R.id.txt_planAmount);
            subscription = itemView.findViewById(R.id.txt_planSubsDays);
            banner = itemView.findViewById(R.id.txt_planNoBanner);
            interstitial = itemView.findViewById(R.id.txt_planNoInterstitial);
            video = itemView.findViewById(R.id.txt_planNoVideo);
            subscribeNow = itemView.findViewById(R.id.btn_buyNow);
        }
    }
}
