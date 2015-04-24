package com.armend.android.dbtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.quentindommerc.superlistview.OnMoreListener;
import com.quentindommerc.superlistview.SuperListview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class JobsActivity extends VisibleFragment{


    public static JobCursorAdapter adapter;
    private static final String TAG =  JobsActivity.class.getSimpleName();
    private JSONArray jobsArray;
    public static int id;
    public static int topId;
    public static int bottomId;
    public static SuperListview listView;
    private int mTotalCount = 0;
    private SharedPreferences myId;
    public static final String PREF = "MyPrefs" ;
    public static final String IDNR = "myidnumber" ;
    public static final String TOPIDNR = "mytopidnumber" ;
    public static final String BOTTOMID= "mybottomidnumber" ;
    private int scrollPosition = 0;
    public static int itemPos;
    public static LoaderManager loaderManagerJobs;
    public static boolean isNetworkAvailable;


    public static final int LOAD_FIRSTFETCH = 0;
    public static final int LOAD_FETCHJOBS = 1;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.jobs_fragment,container,false);

        loaderManagerJobs = getLoaderManager();


        listView = (SuperListview)v.findViewById(R.id.list);


        loaderManagerJobs.initLoader(LOAD_FIRSTFETCH, null, new FirstFetchLoaderCallbacks());


        myId = this.getActivity().getSharedPreferences(PREF, 0);

        id = myId.getInt(IDNR, 0);
        topId = myId.getInt(TOPIDNR, 0);
        bottomId =myId.getInt(BOTTOMID, 0);





        listView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (isNetworkAvailable) {
                    getJobs(topId, "rjob");

                } else {
                    listView.getSwipeToRefresh().setRefreshing(false);
                    Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                }



            }
        });




            // when there is only 10 items to see in the list, this is triggered
        listView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {

                if (!(bottomId == 1)) {
                    if (isNetworkAvailable) {
                        getJobs(bottomId, "shjob");
                    }
                    else {
                        listView.hideMoreProgress();
                        Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    listView.hideMoreProgress();
                }

                // Fetch more from Api or DB

            }
        }, 1);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                itemPos = position;

                Intent pager = new Intent(getActivity(), PagerActivity.class);
                startActivity(pager);
            }
        });

        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        isNetworkAvailable = cm.getBackgroundDataSetting() &&
                cm.getActiveNetworkInfo() != null;
        Log.i(TAG, "In JobsActivity! network available: " + isNetworkAvailable);

        if (id == 0) {
            if (isNetworkAvailable) {
                getJobs(id, "shjob");
            }else {

                Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
            }
        }


        return v;
	}


    public void getJobs(final int numerid, final String jobtype) {
        // Tag used to cancel the request
        String tag_string_req = "req_jobs";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());

                try {
                    if(!(response.toString().equals("0"))) {

                       jobsArray = new JSONArray(response);

                       // looping through All Contacts
                       for (int i = 0; i < jobsArray.length(); i++) {
                           JSONObject jobs = jobsArray.getJSONObject(i);

                           String pozita = jobs.getString("pozita");
                           String kompania = jobs.getString("kompania");
                           String qyteti = jobs.getString("qyteti");
                           String skadimi = jobs.getString("skadimi");
                           String orari = jobs.getString("orari");
                           String tags = jobs.getString("tags");
                           String kontakt = jobs.getString("kontakt");
                           String imgurl = jobs.getString("imgurl");
                           String pershkrimi = jobs.getString("pershkrimi");


                           if(id==0){
                               topId = Integer.parseInt(jobs.getString("id"));
                           }
                           id = Integer.parseInt(jobs.getString("id"));

                           if(!(id>topId)){
                               bottomId = id;
                           }else{
                               topId=id;
                           }


                           Log.d(TAG, "Register ID: " + Integer.toString(id));
                           Log.d(TAG, "Register BOTTOMID: " + Integer.toString(bottomId));


                           MainActivity.dbHelper.createJob(pozita, kompania, qyteti, skadimi, orari, tags, kontakt, imgurl, id, pershkrimi);

                       }


                    SharedPreferences.Editor editor = myId.edit();
                    editor.putInt(IDNR, id);
                    editor.putInt(BOTTOMID, bottomId);
                    editor.putInt(TOPIDNR, topId).commit();



                       loadListView();
                    }  else {
                        Toast.makeText(getActivity(), "Nothing new", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {

                    e.printStackTrace();
                }


                listView.hideMoreProgress();

                listView.getSwipeToRefresh().setRefreshing(false);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                listView.hideMoreProgress();
                listView.getSwipeToRefresh().setRefreshing(false);
                Toast.makeText(getActivity(), "Error. Trying again now.", Toast.LENGTH_SHORT).show();
                getJobs(numerid,jobtype);
            }


        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "job");
                params.put("id", Integer.toString(numerid));
                params.put("jobtype", jobtype);

                return params;
            }



        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    @SuppressWarnings("ResourceAsColor")
    public  void loadListView() {

        //JobsActivity.cjobs = MainActivity.dbHelper.fetchJobsById(1);
        //JobsActivity.adapter.changeCursor(cjobs);

        loaderManagerJobs.initLoader(LOAD_FETCHJOBS, null, new FetchJobsLoaderCallbacks());
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }



    private static class FirstFetch extends SQLiteCursorLoader {
        Context mContext;

        public FirstFetch (Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected Cursor loadCursor() {
            // query the list of runs
            return MainActivity.dbHelper.fetchJobsById(1);
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

    private class FirstFetchLoaderCallbacks implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // we only ever load the runs, so assume this is the case
            return new FirstFetch(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // create an adapter to point at this cursor
            adapter = new JobCursorAdapter(getActivity(), cursor, 0);
            listView.setAdapter(adapter);

            MainActivity.searchEdTxt.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    JobsActivity.adapter.getFilter().filter(s.toString());
                }
            });

            JobsActivity.adapter.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence constraint) {
                    return MainActivity.dbHelper.fetchJobsByFilter(constraint.toString());
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // stop using the cursor (via the adapter)

        }
    }

    private class FetchJobsLoaderCallbacks implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // we only ever load the runs, so assume this is the case
            return new FetchJobs(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // create an adapter to point at this cursor
            adapter.changeCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // stop using the cursor (via the adapter)
            // listView.setAdapter(null);

        }
    }

    }






