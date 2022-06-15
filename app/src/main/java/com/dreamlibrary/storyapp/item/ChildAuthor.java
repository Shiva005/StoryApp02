package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChildAuthor implements Serializable {

    @SerializedName("author_id")
    private String author_id;

    @SerializedName("author_name")
    private String author_name;

    @SerializedName("author_image")
    private String author_image;

    public String getAuthorId() {
        return author_id;
    }

    public String getAuthorName() {
        return author_name;
    }

    public String getAuthorImage() {
        return author_image;
    }

}
