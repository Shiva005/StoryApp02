package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HomeSection implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("section_title")
    private String section_title;

    @SerializedName("book_list")
    private String book_list;


    public String getId() {
        return id;
    }

    public String getSectionTitle() {
        return section_title;
    }

    public String getBookList() {
        return book_list;
    }

}
