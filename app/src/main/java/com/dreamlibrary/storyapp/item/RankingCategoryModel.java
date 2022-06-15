package com.dreamlibrary.storyapp.item;

public class RankingCategoryModel {
    private int categoryImage;
    private String categoryTitle;

    public RankingCategoryModel(int categoryImage, String categoryTitle) {
        this.categoryImage = categoryImage;
        this.categoryTitle = categoryTitle;
    }

    public int getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(int categoryImage) {
        this.categoryImage = categoryImage;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }
}
