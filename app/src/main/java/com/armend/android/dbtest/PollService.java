package com.armend.android.dbtest;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    
    private static final int POLL_INTERVAL = 1000 * 15; // 60* 5= 5 minutes
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static final String ACTION_SHOW_NOTIFICATION = "com.armend.android.dbtest.SHOW_NOTIFICATION";
    
    public static final String PERM_PRIVATE = "com.armend.android.dbtest.PRIVATE";

    String oferta;
    String kliko;

    private SharedPreferences myId;

    public static final String PREF = "MyPrefs" ;
    public static final String IDNR = "myidnumber" ;
    public static final String TOPIDNR = "mytopidnumber" ;
    public static final String BOTTOMID= "mybottomidnumber" ;

    int id;
    int topId;
    int bottomId;
    JobsDb dbHelper;

    private JSONArray jobsArray;
    
    public PollService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() &&
            cm.getActiveNetworkInfo() != null;        
        Log.i(TAG, "In PollService! network available: " + isNetworkAvailable);
        if (!isNetworkAvailable) return;

        myId = PollService.this.getSharedPreferences(PREF, 0);

        id = myId.getInt(IDNR, 0);
        topId = myId.getInt(TOPIDNR, 0);
        bottomId =myId.getInt(BOTTOMID, 0);




        getJobs(topId, "rjob");


        }


    
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, 
                    System.currentTimeMillis(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
        
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(PollService.PREF_IS_ALARM_ON, isOn)
            .commit();
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
    
    void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);
        
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
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

                        dbHelper = new JobsDb(PollService.this);
                        Log.d(TAG, "Database created");
                        dbHelper.open();

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

                            if (id == 0) {
                                topId = Integer.parseInt(jobs.getString("id"));
                            }
                            id = Integer.parseInt(jobs.getString("id"));

                            if (!(id > topId)) {
                                bottomId = id;
                            } else {
                                topId = id;
                            }


                            Log.d(TAG, "Register ID: " + Integer.toString(id));
                            Log.d(TAG, "Register BOTTOMID: " + Integer.toString(bottomId));


                            dbHelper.createJob(pozita, kompania, qyteti, skadimi, orari, tags, kontakt, imgurl, id, pershkrimi);
                        }

                        dbHelper.close();


                        SharedPreferences.Editor editor = myId.edit();
                        editor.putInt(IDNR, id);
                        editor.putInt(BOTTOMID, bottomId);
                        editor.putInt(TOPIDNR, topId).commit();

                        int newJobs = jobsArray.length();

                        Resources r = getResources();

                        if(newJobs>1){
                            oferta = r.getString(R.string.oferta_pune);
                            kliko = r.getString(R.string.kliko);
                        }else{
                            oferta = r.getString(R.string.oferta_pune1);
                            kliko = r.getString(R.string.kliko1);
                        }

                        Log.i(TAG, "jobsArray.length: " + jobsArray);
                        Log.i(TAG, "Got a new result");


                        PendingIntent pi = PendingIntent
                                .getActivity(PollService.this, 0, new Intent(PollService.this, MainActivity.class), 0);

                        Notification notification = new NotificationCompat.Builder(PollService.this)
                                .setTicker(newJobs + oferta)
                                .setSmallIcon(R.drawable.info)
                                .setContentText(newJobs + kliko )
                                .setContentIntent(pi)
                                .setAutoCancel(true)
                                .build();

                        showBackgroundNotification(0, notification);
                    }

                } catch (JSONException e) {



                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(PollService.this, "Error connecting. Trying again.", Toast.LENGTH_SHORT).show();
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
}
