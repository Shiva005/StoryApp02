package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MainSearchBook implements Serializable {

    @SerializedName("EBOOK_APP")
    List<ChildSearchBook> searchBooks;


    public List<ChildSearchBook> getSearchBooks() {

        return searchBooks;

    }

    ;


}
