package com.orion.testmybloodft.views.adapter;

/**
 * Created by Arun on 7/7/2017.
 */

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.orion.testmybloodft.fragment.FragmentPickedUp;
import com.orion.testmybloodft.fragment.FragmentToday;
import com.orion.testmybloodft.fragment.FragmentTomorrow;

/**
 * Adapter which displays scheduled order for today, tomorrow and picked up
 */

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    final int PAGE_COUNT = 3;
    // Tab Titles
    private String tabtitles[] = new String[]{"TODAY", "TOMORROW", "PICKED UP"};
    private Context mContext;

    public TabsPagerAdapter(FragmentManager fragmentManager,Context mContext) {
        super(fragmentManager);
        this.mContext = mContext;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            // Open FragmentToday.java
            case 0:
                FragmentToday today = new FragmentToday(mContext);
                return today;

            // Open FragmentTomorrow.java
            case 1:
                FragmentTomorrow tomorrow = new FragmentTomorrow(mContext);
                return tomorrow;

            // Open FragmentPickedUp.java
            case 2:
                FragmentPickedUp pickup = new FragmentPickedUp(mContext);
                Bundle fragmentBundle = new Bundle();
                fragmentBundle.putBoolean("Pickup", true);
                pickup.setArguments(fragmentBundle);

                return pickup;
        }
        return null;
    }



    @Override
    public int getCount() {
        return PAGE_COUNT;
    }



    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}


