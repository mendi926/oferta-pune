package com.armend.android.dbtest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {

// Declaring Your View and Variables


    MenuItem searchButt;
    MenuItem clearButt;
    public static EditText searchEdTxt;


    public static JobsDb dbHelper;
    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapterOff adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Ofertat","Favorites"};
    int NumbOfTabs =2;
    private static final String TAG =  MainActivity.class.getSimpleName();
    InputMethodManager imm;

    public static ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

       /* dbHelper = new JobsDb(this);
        Log.d(TAG, "Database created");
        dbHelper.open();*/

        new OpenDb(this).execute();


// Creating The Toolbar and setting it as the Toolbar for the activity

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchEdTxt = (EditText)findViewById(R.id.myFilter);

        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                imageView.setImageBitmap(thumbnail);
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();



// Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapterOff(getSupportFragmentManager(),Titles,NumbOfTabs);

// Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(adapter);


// Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

// Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

// Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);


        imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    @Override
    protected void onDestroy() {

        Log.d("MAINACTIVITYOFF LOG:", "This is onDestroy()");
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Write your code here
        if(View.VISIBLE==searchEdTxt.getVisibility()){
            Log.d("MainActivity", "backbutton pressed");
            clearButt.setVisible(false);
            searchButt.setVisible(true);
            searchEdTxt.clearFocus();
            searchEdTxt.getText().clear();
            searchEdTxt.setVisibility(View.GONE);
            JobsActivity.listView.getSwipeToRefresh().setEnabled(true);
            JobsActivity.adapter.changeCursor(MainActivity.dbHelper.fetchJobsById(1));
        }else{
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        searchButt = menu.findItem(R.id.action_search);

        clearButt = menu.findItem(R.id.action_clear);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.action_search:
                Log.d("MainActivity", "Search pressed");
                searchButt.setVisible(false);
                clearButt.setVisible(true);
                searchEdTxt.setVisibility(View.VISIBLE);
                searchEdTxt.requestFocus();
                JobsActivity.listView.getSwipeToRefresh().setEnabled(false);
                pager.setCurrentItem(0, true);
                imm.showSoftInput(searchEdTxt, InputMethodManager.SHOW_IMPLICIT);
                return true;

            case R.id.action_clear:
                Log.d("MainActivity", "Clear pressed");
                clearButt.setVisible(false);
                searchButt.setVisible(true);
                searchEdTxt.clearFocus();
                searchEdTxt.getText().clear();
                searchEdTxt.setVisibility(View.GONE);
                JobsActivity.listView.getSwipeToRefresh().setEnabled(true);
                JobsActivity.adapter.changeCursor(MainActivity.dbHelper.fetchJobsById(1));
                imm.hideSoftInputFromWindow(searchEdTxt.getWindowToken(), 0);
                return true;

            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(this);
                PollService.setServiceAlarm(this, shouldStartAlarm);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    this.invalidateOptionsMenu();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(this)) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }

        return super.onPrepareOptionsMenu(menu);
    }



    public class OpenDb extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        public OpenDb(Context context) {
            super();
            mContext = context;

        }

        protected Void doInBackground(Void... params) {
            // using this.mContext
            dbHelper = new JobsDb(mContext);
            Log.d(TAG, "Database created");
            dbHelper.open();
            return null;
        }
    }

}
