package com.armend.android.dbtest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by AH on 4/20/2015.
 */
public class JobCursorAdapter extends CursorAdapter {

    int currentId;
    int tabNr;
    Context mContext;

    public JobCursorAdapter(Context context, Cursor cursor, int number) {
        super(context, cursor, 0);
        mContext = context;
        tabNr = number;
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.job_list_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView mKompania = (TextView) view.findViewById(R.id.kompania);
        TextView mPozita = (TextView) view.findViewById(R.id.pozita);
        TextView mQyteti = (TextView) view.findViewById(R.id.qyteti);
        TextView mSkadimi = (TextView) view.findViewById(R.id.skadimi);
        // Extract properties from cursor
        currentId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String kompania = cursor.getString(cursor.getColumnIndexOrThrow("kompania"));
        String pozita = cursor.getString(cursor.getColumnIndexOrThrow("pozita"));
        String qyteti = cursor.getString(cursor.getColumnIndexOrThrow("qyteti"));
        String skadimi = cursor.getString(cursor.getColumnIndexOrThrow("skadimi"));
        String imgUrl = cursor.getString(cursor.getColumnIndexOrThrow("imgurl"));
        int favorite = cursor.getInt(cursor.getColumnIndexOrThrow("favorite"));
        // Populate fields with extracted properties
        mKompania.setText(kompania);
        mPozita.setText(pozita);
        mQyteti.setText(qyteti);
        mSkadimi.setText(getDate(skadimi));

        ImageView imageView = (ImageView) view.findViewById(R.id.logoimg);
        Bitmap cacheHit = MainActivity.mThumbnailThread.checkCache(imgUrl);
        if (cacheHit != null) {
            imageView.setImageBitmap(cacheHit);
        } else {
            MainActivity.mThumbnailThread.queueThumbnail(imageView, imgUrl);
        }

        // pre-load images
        for (int i=Math.max(0, currentId-10); i< Math.min(cursor.getCount()-1, currentId+10); i++) {
            MainActivity.mThumbnailThread.queuePreload(imgUrl);
        }

        //MainActivity.mThumbnailThread.queueThumbnail(imageView, imgUrl);


        final CheckBox favButt = (CheckBox) view.findViewById(R.id.fav_butt);
        favButt.setChecked(favorite==1);

        favButt.setTag(currentId);

        favButt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                int itemId = (int)buttonView.getTag();
                if(isChecked){

                    int[] intArray = new int[] {itemId,1};
                    new UpdateFav().execute(intArray);

                }else {

                    int[] intArray = new int[] {itemId,0};
                    new UpdateFav().execute(intArray);
                    }

                Log.d("JobCursorAdapter", "currentId is:" + itemId+" isChecked:"+isChecked+"tabNr: "+tabNr);
            }
        });


    }

    public String getDate(String date){

        Date startDate = new Date();
        Date endDate = new Date();
        String mTimeLeft = date;

        SimpleDateFormat dateParse = new SimpleDateFormat("dd.MM.yyyy");



        try {
            endDate = dateParse.parse(date);
        }
        catch(Exception e){
            e.printStackTrace();

        }


        long duration  = endDate.getTime() - startDate.getTime();

        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);

        int hours = Math.round(diffInHours);
        int days = Math.round(diffInDays);

        if(days>1){
            mTimeLeft = days+" ditë të mbetura";
        }else if(hours>=1){
            mTimeLeft = hours+" orë të mbetura";
        }else if(hours<=0){
            mTimeLeft = "Afati ka skaduar";
        }

        return mTimeLeft;


    }

    private static class FetchFavs extends SQLiteCursorLoader {

        public FetchFavs (Context context) {
            super(context);
        }

        @Override
        protected Cursor loadCursor() {
            // query the list of runs
            return MainActivity.dbHelper.fetchFav();
        }


    }

    private class FetchFavsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // we only ever load the runs, so assume this is the case
            return new FetchFavs(mContext);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // create an adapter to point at this cursor
            FavoriteActivity.fadapter.changeCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // stop using the cursor (via the adapter)
            // listView.setAdapter(null);

        }
    }

    private static class FetchJobs extends SQLiteCursorLoader {

        public FetchJobs (Context context) {
            super(context);
        }

        @Override
        protected Cursor loadCursor() {
            // query the list of runs
            return MainActivity.dbHelper.fetchJobsById(1);
        }


    }

    private class FetchJobsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // we only ever load the runs, so assume this is the case
            return new FetchJobs(mContext);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // create an adapter to point at this cursor
            JobsActivity.adapter.changeCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // stop using the cursor (via the adapter)
            // listView.setAdapter(null);

        }
    }

    public class UpdateFav extends AsyncTask<int[], Void, Void> {

        @Override
        protected Void doInBackground(int[]... params) {
            // using this.mContext
            int itemId = params[0][0];
            int valueNr = params[0][1];

            Log.d("UpdateFav", "itemId: "+itemId+" valueNr: "+valueNr);

            MainActivity.dbHelper.updateFav(itemId,valueNr);

            return null;
        }

        @Override
        protected void onPostExecute(Void nul) {

            JobsActivity.loaderManagerJobs.restartLoader(JobsActivity.LOAD_FETCHJOBS, null,
                    new FetchJobsLoaderCallbacks());
            FavoriteActivity.loaderManagerFavs.restartLoader(FavoriteActivity.LOAD_FETCHFAVS, null,
                    new FetchFavsLoaderCallbacks());

            Log.d("UpdateFav", "THIS IS ONPOSTEXECUTE");

        }
    }


}
