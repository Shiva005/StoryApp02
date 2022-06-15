package com.dreamlibrary.storyapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.dreamlibrary.storyapp.fragment.CategoryFragment;
import com.dreamlibrary.storyapp.fragment.DiscoverHomeFragment;

public class DiscoverHomeAdapter extends FragmentStatePagerAdapter {

    public DiscoverHomeAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            /*return new RankingFragment();
        }else if (position == 1) {*/
            return new DiscoverHomeFragment();
        } else if (position == 1) {
            return new CategoryFragment();
        }
        return new DiscoverHomeFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}

