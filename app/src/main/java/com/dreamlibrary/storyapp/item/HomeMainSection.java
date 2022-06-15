package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeMainSection implements Serializable {

    @SerializedName("EBOOK_APP")
    private List<HomeSection> authorSectionsList;

    public List<HomeSection> getAuthorSectionsList() {
        return authorSectionsList;
    }

}
