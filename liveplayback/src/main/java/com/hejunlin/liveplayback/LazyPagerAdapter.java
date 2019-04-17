package com.hejunlin.liveplayback;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

class LazyPagerAdapter extends FragmentPagerAdapter {
    private List<FragmentCreator> list;
    private FragmentManager mFragmentManager;

    public void setFragments(List<FragmentCreator> list) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (Fragment f : mFragmentManager.getFragments()) {
            transaction.remove(f);
        }
        transaction.commitNow();
        this.list = list;
        notifyDataSetChanged();
    }


    public LazyPagerAdapter(FragmentManager fm, List<FragmentCreator> list) {
        super(fm);
        this.list = list;
        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int position) {
        FragmentCreator fragmentCreator = list.get(position);
        return fragmentCreator.createFragment();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}