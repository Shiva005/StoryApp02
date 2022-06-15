package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MainChildAuthorSection implements Serializable {

    @SerializedName("EBOOK_APP")
    private List<ChildAuthorSection> childAuthorSectionList;

    public List<ChildAuthorSection> getChildAuthorSectionList() {
        return childAuthorSectionList;
    }
}
