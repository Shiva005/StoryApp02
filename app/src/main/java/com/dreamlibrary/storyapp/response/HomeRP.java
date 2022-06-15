package com.dreamlibrary.storyapp.response;

import com.dreamlibrary.storyapp.item.AuthorList;
import com.dreamlibrary.storyapp.item.BookList;
import com.dreamlibrary.storyapp.item.CategoryList;
import com.dreamlibrary.storyapp.item.NewSliderList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("slider_books")
    private List<NewSliderList> sliderLists;

    @SerializedName("continue_books")
    private List<BookList> continueLists;

    @SerializedName("latest_books")
    private List<BookList> latestList;

    @SerializedName("popular_books")
    private List<BookList> popularList;

    @SerializedName("category_list")
    private List<CategoryList> categoryLists;

    @SerializedName("author_list")
    private List<AuthorList> authorLists;

    @SerializedName("slider_books2")
    private List<NewSliderList> slider_books2;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<NewSliderList> getSliderLists() {
        return sliderLists;
    }

    public List<BookList> getContinueLists() {
        return continueLists;
    }

    public List<BookList> getLatestList() {
        return latestList;
    }

    public List<BookList> getPopularList() {
        return popularList;
    }

    public List<CategoryList> getCategoryLists() {
        return categoryLists;
    }

    public List<AuthorList> getAuthorLists() {
        return authorLists;
    }

    public List<NewSliderList> getSecondSliderLists() {
        return slider_books2;
    }
}
