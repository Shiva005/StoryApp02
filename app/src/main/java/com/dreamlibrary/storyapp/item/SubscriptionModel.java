package com.dreamlibrary.storyapp.item;

public class SubscriptionModel {
    private String planName;
    private String planAmount;
    private String subscription;
    private String banner;
    private String interstitial;
    private String video;

    public SubscriptionModel(String planName, String planAmount, String subscription, String banner, String interstitial, String video) {
        this.planName = planName;
        this.planAmount = planAmount;
        this.subscription = subscription;
        this.banner = banner;
        this.interstitial = interstitial;
        this.video = video;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanAmount() {
        return planAmount;
    }

    public void setPlanAmount(String planAmount) {
        this.planAmount = planAmount;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getInterstitial() {
        return interstitial;
    }

    public void setInterstitial(String interstitial) {
        this.interstitial = interstitial;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
