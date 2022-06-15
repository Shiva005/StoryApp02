package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChildHomeSection implements Serializable {

    @SerializedName("total_records")
    private String total_records;
    @SerializedName("id")
    private String id;
    @SerializedName("cat_id")
    private String cat_id;
    @SerializedName("featured")
    private String featured;
    @SerializedName("book_title")
    private String book_title;
    @SerializedName("book_description")
    private String book_description;
    @SerializedName("book_cover_img")
    private String book_cover_img;
    @SerializedName("book_file_type")
    private String book_file_type;
    @SerializedName("book_file_url")
    private String book_file_url;
    @SerializedName("total_rate")
    private String total_rate;
    @SerializedName("rate_avg")
    private String rate_avg;
    @SerializedName("book_views")
    private String book_views;
    @SerializedName("author_id")
    private String author_id;
    @SerializedName("author_name")
    private String author_name;
    @SerializedName("author_description")
    private String author_description;
    @SerializedName("cid")
    private String cid;
    @SerializedName("category_name")
    private String category_name;


    public String getTotalRecords() {
        return total_records;
    }

    public String getId() {
        return id;
    }

    public String getCatId() {
        return cat_id;
    }

    public String getFeatured() {
        return featured;
    }

    public String getBookTitle() {
        return book_title;
    }

    public String getBookDescription() {
        return book_description;
    }

    public String getBookCoverImg() {
        return book_cover_img;
    }

    public String getBookFileType() {
        return book_file_type;
    }

    public String getBookFileUrl() {
        return book_file_url;
    }

    public String getTotalRate() {
        return total_rate;
    }

    public String getRateAverage() {
        return rate_avg;
    }

    public String getBookViews() {
        return book_views;
    }

    public String getAuthorId() {
        return author_id;
    }

    public String getAuthorName() {
        return author_name;
    }

    public String getAuthorDescription() {
        return author_description;
    }

    public String getCid() {
        return cid;
    }

    public String getCategoryName() {
        return category_name;
    }
}
