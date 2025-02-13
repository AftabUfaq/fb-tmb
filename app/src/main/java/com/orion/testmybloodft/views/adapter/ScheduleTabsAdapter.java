package com.orion.testmybloodft.views.adapter;

/**
 * Created by Arun on 7/7/2017.
 */

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.orion.testmybloodft.fragment.FragmentProfile;
import com.orion.testmybloodft.fragment.FragmentTestBilling;

/**
 * Adapter which displays information about patient
 */

public class ScheduleTabsAdapter extends FragmentStatePagerAdapter {

    final int PAGE_COUNT = 2;
    // Tab Titles
    private String tabtitles[] = new String[] { "PATIENT INFORMATION", "INVOICE DETAILS"};
    private Context mContext;

    public ScheduleTabsAdapter(FragmentManager fm, Context mContext) {
        super(fm);
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            // Open FragmentProfile.java
            case 0:
                FragmentProfile profile = new FragmentProfile(mContext);
                return profile;

            // Open FragmentTestBilling.java
            case 1:
                FragmentTestBilling billing = new FragmentTestBilling(mContext);
                return billing;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}


