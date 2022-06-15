package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MainChildHomeSection implements Serializable {

    @SerializedName("EBOOK_APP")
    private List<ChildHomeSection> childAuthorSectionList;

    public List<ChildHomeSection> getChildAuthorSectionList() {
        return childAuthorSectionList;
    }
}
