package com.dreamlibrary.storyapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.adapter.RankingCategoryAdapter;
import com.dreamlibrary.storyapp.item.RankingCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class RankingFragment extends Fragment {
    private RecyclerView rvRankingCategories, rvRankingBooks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        //Category Products starts here
        List<RankingCategoryModel> categoryModelList = new ArrayList<>();
        categoryModelList.add(new RankingCategoryModel(R.drawable.splash_screen, "Dairy"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.facebook_ic, "Rice"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.google_ic, "Pulse"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.insta_ic, "Grains"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.splash, "Fruits"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.library, "Bakery"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.ic_category, "Vegetables"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.stare, "Canned"));
        categoryModelList.add(new RankingCategoryModel(R.drawable.ic_author, "Beverages"));

        rvRankingCategories = view.findViewById(R.id.rec_rankingCategory);
        RankingCategoryAdapter categoryAdapter = new RankingCategoryAdapter(getContext(), categoryModelList, 0);
        rvRankingCategories.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1, RecyclerView.VERTICAL, false);
        rvRankingCategories.setLayoutManager(layoutManager);
        rvRankingCategories.setItemAnimator(new DefaultItemAnimator());
        rvRankingCategories.setAdapter(categoryAdapter);

        //Sub Category Products starts here

        rvRankingBooks = view.findViewById(R.id.rec_rankingBooks);
        RankingCategoryAdapter categoryAdapter2 = new RankingCategoryAdapter(getContext(), categoryModelList, 1);
        rvRankingBooks.setHasFixedSize(true);
        GridLayoutManager layoutManager2 = new GridLayoutManager(getContext(), 1, RecyclerView.VERTICAL, false);
        rvRankingBooks.setLayoutManager(layoutManager2);
        rvRankingBooks.setItemAnimator(new DefaultItemAnimator());
        rvRankingBooks.setAdapter(categoryAdapter2);
        return view;
    }
}