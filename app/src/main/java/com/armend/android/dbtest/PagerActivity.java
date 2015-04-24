package com.armend.android.dbtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PagerActivity extends FragmentActivity {
    ViewPager mViewPager;
    Cursor jobs;
    CursorPagerAdapter adapter;
    private JSONArray jobsArray;

    private SharedPreferences myId;
    public static final String PREF = "MyPrefs" ;
    public static final String IDNR = "myidnumber" ;
    public static final String TOPIDNR = "mytopidnumber" ;
    public static final String BOTTOMID= "mybottomidnumber" ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final boolean isFavorite = getIntent().getBooleanExtra("isFavorite", false);

        if(isFavorite) {
            jobs = MainActivity.dbHelper.fetchFav();
        }else {
            jobs = MainActivity.dbHelper.fetchJobs(1);
        }

        myId = getSharedPreferences(PREF, 0);


        adapter = new CursorPagerAdapter(this, jobs) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.fragment_pager, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView fragKomp = (TextView) view.findViewById(R.id.frag_kompania);
                TextView fragPoz = (TextView) view.findViewById(R.id.frag_pozita);
                TextView fragQyt = (TextView) view.findViewById(R.id.frag_qyteti);
                TextView fragSkad = (TextView) view.findViewById(R.id.frag_skadimi);
                TextView fragOrari = (TextView) view.findViewById(R.id.frag_orari);
                TextView fragKont = (TextView) view.findViewById(R.id.frag_kontakt);
                TextView fragPersh = (TextView) view.findViewById(R.id.frag_pershkrimi);

                String fKompania = cursor.getString(cursor.getColumnIndexOrThrow("kompania"));
                String fPozita = cursor.getString(cursor.getColumnIndexOrThrow("pozita"));
                String fQyteti = cursor.getString(cursor.getColumnIndexOrThrow("qyteti"));
                String fSkadimi = cursor.getString(cursor.getColumnIndexOrThrow("skadimi"));
                String fKontakti = cursor.getString(cursor.getColumnIndexOrThrow("kontakt"));
                String fOrari = cursor.getString(cursor.getColumnIndexOrThrow("orari"));
                String fPersh = cursor.getString(cursor.getColumnIndexOrThrow("pershkrimi"));


                fragKomp.setText(fKompania);
                fragPoz.setText(fPozita);
                fragQyt.setText(fQyteti);
                fragSkad.setText(fSkadimi);
                fragOrari.setText(fOrari);
                fragKont.setText(fKontakti);
                fragPersh.setText(fPersh);
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {
                return view == o;
            }
        };


        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        mViewPager.setAdapter(adapter);
        if(isFavorite) {
            mViewPager.setCurrentItem(FavoriteActivity.favItemPos);
        }else{
            mViewPager.setCurrentItem(JobsActivity.itemPos);
        }

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {

               if(!isFavorite) {
                   int totalItems = jobs.getCount();
                   Log.d("PagerActivity", "totalItems= " + totalItems + "int i =" + i);
                   if ((totalItems - 2) == i) {
                       if (!(JobsActivity.bottomId == 1)) {


                           if (JobsActivity.isNetworkAvailable) {

                               getJobs(JobsActivity.bottomId, "shjob");

                           } else {

                               Toast.makeText(PagerActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                           }


                       }
                   }
               }




            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });




    }

    public void getJobs(final int numerid, final String jobtype) {
        // Tag used to cancel the request
        String tag_string_req = "req_jobs";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("PagerActivity", "Register Response: " + response.toString());

                try {
                    //JSONObject jObj = new JSONObject(response);

                    // Getting JSON Array node

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

                        if(JobsActivity.id==0){
                            JobsActivity.topId = Integer.parseInt(jobs.getString("id"));
                        }
                        JobsActivity.id = Integer.parseInt(jobs.getString("id"));

                        if(!(JobsActivity.id> JobsActivity.topId)){
                            JobsActivity.bottomId = JobsActivity.id;
                        }else{
                            JobsActivity.topId= JobsActivity.id;
                        }


                        Log.d("PagerActivity", "Register ID: " + Integer.toString(JobsActivity.id));
                        Log.d("PagerActivity", "Register BOTTOMID: " + Integer.toString(JobsActivity.bottomId));


                        MainActivity.dbHelper.createJob(pozita, kompania, qyteti, skadimi, orari, tags, kontakt, imgurl, JobsActivity.id, pershkrimi);

                    }


                    SharedPreferences.Editor editor = myId.edit();
                    editor.putInt(IDNR, JobsActivity.id);
                    editor.putInt(BOTTOMID, JobsActivity.bottomId);
                    editor.putInt(TOPIDNR, JobsActivity.topId).commit();



                    loadListView();

                } catch (JSONException e) {

                    Toast.makeText(PagerActivity.this, "Nothing new", Toast.LENGTH_SHORT).show();

                    e.printStackTrace();
                }



            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(PagerActivity.this, "Error connecting. Trying again.", Toast.LENGTH_SHORT).show();
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

    public void loadListView() {

        adapter.changeCursor(MainActivity.dbHelper.fetchJobs(1));

        JobsActivity.loaderManagerJobs.initLoader(JobsActivity.LOAD_FETCHJOBS, null,
               new FetchJobsLoaderCallbacks() );


    }

    @Override
    protected void onDestroy() {

        Log.d("PagerActivity", "THIS IS ONDESTROY OF");
        super.onDestroy();
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
            return new FetchJobs(PagerActivity.this);
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
}

