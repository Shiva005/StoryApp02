package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChildAuthorSection implements Serializable {

    @SerializedName("total_records")
    private String total_records;
    @SerializedName("author_id")
    private String author_id;
    @SerializedName("author_name")
    private String author_name;
    @SerializedName("author_city_name")
    private String author_city_name;
    @SerializedName("author_description")
    private String author_description;
    @SerializedName("author_image")
    private String author_image;
    @SerializedName("author_youtube")
    private String author_youtube;
    @SerializedName("author_instagram")
    private String author_instagram;
    @SerializedName("author_facebook")
    private String author_facebook;
    @SerializedName("author_website")
    private String author_website;


    public String getTotalRecords() {
        return total_records;
    }

    public String getAuthorId() {
        return author_id;
    }

    public String getAuthorName() {
        return author_name;
    }

    public String getAuthorCity() {
        return author_city_name;
    }

    public String getAuthorDescription() {
        return author_description;
    }

    public String getAuthorImage() {
        return author_image;
    }

    public String getAuthorYoutube() {
        return author_youtube;
    }

    public String getAuthorInstagram() {
        return author_instagram;
    }

    public String getAuthorFacebook() {
        return author_facebook;
    }

    public String getAuthorWebsite() {
        return author_website;
    }
}
