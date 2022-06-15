package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthorSection implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("section_title")
    private String section_title;

    @SerializedName("author_list")
    private String author_list;


    public String getId() {
        return id;
    }

    public String getSectionTitle() {
        return section_title;
    }

    public String getAuthorList() {
        return author_list;
    }

}
