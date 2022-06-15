package com.dreamlibrary.storyapp.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AuthorMainSection implements Serializable {

    @SerializedName("EBOOK_APP")
    private List<AuthorSection> authorSectionsList;

    public List<AuthorSection> getAuthorSectionsList() {
        return authorSectionsList;
    }

}
