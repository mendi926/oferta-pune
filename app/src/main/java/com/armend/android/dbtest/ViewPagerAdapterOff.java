package com.armend.android.dbtest;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 *  * Created by hp1 on 21-01-2015.
 *  
 */
public class ViewPagerAdapterOff extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created



    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapterOff(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        switch (position) // if the position is 0 we are returning the First tab
        {



            case 1:
                FavoriteActivity favoriteActivity = new FavoriteActivity();
                return favoriteActivity;

            default:
                JobsActivity jobsActivity = new JobsActivity();
                return jobsActivity;

        }


    }

          // This method return the titles for the Tabs in the Tab Strip

           @Override
            public CharSequence getPageTitle(int position) {
            return Titles[position];
            }

            // This method return the Number of tabs for the tabs Strip

            @Override
        public int getCount() {
            return NumbOfTabs;
            }
}