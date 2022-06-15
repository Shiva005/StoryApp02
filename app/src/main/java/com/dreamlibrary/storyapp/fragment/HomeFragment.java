package com.dreamlibrary.storyapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.adapter.DiscoverHomeAdapter;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private DiscoverHomeAdapter discoverHomeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);

        viewPager = v.findViewById(R.id.viewPager);
        tabLayout = v.findViewById(R.id.tabs);

        //tabLayout.addTab(tabLayout.newTab().setText("Ranking"));
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Category"));

        discoverHomeAdapter = new DiscoverHomeAdapter(getChildFragmentManager());
        viewPager.setAdapter(discoverHomeAdapter);

        //viewPager.setCurrentItem(1);
        //tabLayout.selectTab(tabLayout.getTabAt(1));

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return v;
    }
}