package com.armend.android.dbtest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quentindommerc.superlistview.SuperListview;
import com.quentindommerc.superlistview.SwipeDismissListViewTouchListener;

public class FavoriteActivity extends Fragment {


    public static JobCursorAdapter fadapter;
    private static final String TAG =  FavoriteActivity.class.getSimpleName();
    private SuperListview listView;
    public static int favItemPos;
    public static final int LOAD_FIRSTFAVFETCH = 2;
    public static final int LOAD_FETCHFAVS= 3;
    public static LoaderManager loaderManagerFavs;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fav_fragment,container,false);

        loaderManagerFavs = getLoaderManager();

        listView = (SuperListview)v.findViewById(R.id.flist);

        loaderManagerFavs.initLoader(LOAD_FIRSTFAVFETCH, null, new FirstFavFetchLoaderCallbacks());


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               favItemPos = position;
               boolean isFavorite = true;

               Intent pager = new Intent(getActivity(), PagerActivity.class);
               pager.putExtra("isFavorite", isFavorite);
               startActivity(pager);
            }
        });


        return v;
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private static class FirstFavFetch extends SQLiteCursorLoader {
        Context mContext;

        public FirstFavFetch (Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected Cursor loadCursor() {
            // query the list of runs
            return MainActivity.dbHelper.fetchFav();
        }

    }



    private class FirstFavFetchLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // we only ever load the runs, so assume this is the case
            return new FirstFavFetch(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // create an adapter to point at this cursor
            fadapter = new JobCursorAdapter(getActivity(), cursor, 1);
            listView.setAdapter(fadapter);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // stop using the cursor (via the adapter)


        }
    }


}