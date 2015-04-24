package com.armend.android.dbtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class JobsDb {
 
 public static final String KEY_ROWID = "_id";
 public static final String KEY_POZ = "pozita";
 public static final String KEY_KOM = "kompania";
 public static final String KEY_QYT = "qyteti";
 public static final String KEY_SKA = "skadimi";
 public static final String KEY_PER = "pershkrimi";
 public static final String KEY_ORA = "orari";
 public static final String KEY_TAG = "tags";
 public static final String KEY_KON = "kontakt";
 public static final String KEY_IMG = "imgurl";
 public static final String KEY_FAV = "favorite";

 
 private static final String TAG = "JobsDb";
 private DatabaseHelper mDbHelper;
 private SQLiteDatabase mDb;
 
 private static final String DATABASE_NAME = "mendi";
 private static final String SQLITE_TABLE = "jobs";
 private static final int DATABASE_VERSION = 1;
 
 private final Context mCtx;
 
 private static final String DATABASE_CREATE =
  "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
  KEY_ROWID + " integer PRIMARY KEY," +
  KEY_FAV + " integer DEFAULT 0," +
  KEY_POZ + "," +
  KEY_KOM + "," +
  KEY_QYT + "," +
  KEY_SKA + "," +
  KEY_PER + "," +
  KEY_ORA + "," +
  KEY_TAG + "," +
  KEY_KON + "," +
  KEY_IMG + ")";


private static class DatabaseHelper extends SQLiteOpenHelper {
 
  DatabaseHelper(Context context) {
   super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
 
 
  @Override
  public void onCreate(SQLiteDatabase db) {
   Log.w(TAG, DATABASE_CREATE);
   db.execSQL(DATABASE_CREATE);
  }
 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
   Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
     + newVersion + ", which will destroy all old data");
   db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
   onCreate(db);
  }
 }

 public JobsDb(Context ctx) {
  this.mCtx = ctx;
 }
 
 public JobsDb open() throws SQLException {
  mDbHelper = new DatabaseHelper(mCtx);
  mDb = mDbHelper.getWritableDatabase();
  return this;
 }
 
 public void close() {
  if (mDbHelper != null) {
   mDbHelper.close();
  }
 }
 
 public long createJob(String pozita, String kompania,
   String qyteti, String skadimi, String orari, String tags, String kontakt, String imgurl, int id, String pershkrimi) {
 
  ContentValues initialValues = new ContentValues();
  initialValues.put(KEY_ROWID, id);
  initialValues.put(KEY_POZ, pozita);
  initialValues.put(KEY_KOM, kompania);
  initialValues.put(KEY_QYT, qyteti);
  initialValues.put(KEY_SKA, skadimi);
  initialValues.put(KEY_ORA, orari);
  initialValues.put(KEY_TAG, tags);
  initialValues.put(KEY_KON, kontakt);
  initialValues.put(KEY_IMG, imgurl);
  initialValues.put(KEY_PER, pershkrimi);
 
  return mDb.insert(SQLITE_TABLE, null, initialValues);
 }

    public Cursor fetchJobs(final int id) throws SQLException {
  Log.w(TAG, "fetchJobsById");
  Cursor mCursor = null;

  String[] selection = new String[] {KEY_POZ,KEY_FAV,
          KEY_KOM, KEY_QYT, KEY_SKA,KEY_TAG, KEY_KON, KEY_ORA, KEY_IMG, KEY_ROWID, KEY_PER};
  if (id == 1){

      String orderBy = KEY_ROWID+" DESC";

         mCursor = mDb.query(SQLITE_TABLE,selection ,
                 null, null,
                 null, null, orderBy, null);

  }else
  if (id == 0){

   String orderBy = KEY_ROWID+" DESC";
   String limit = "10";


   mCursor = mDb.query(SQLITE_TABLE, selection,
     null, null,
     null, null, orderBy, limit);
 
  }
  else if(id>=11) {
   String limit = id+",10";


   mCursor = mDb.query(SQLITE_TABLE, selection,
     null, null,
     null, null, null, limit);
  }else{
    String limit = "0,"+id;


   mCursor = mDb.query(SQLITE_TABLE, selection,
     null, null,
     null, null, null, limit);
  }
  if (mCursor != null) {
   mCursor.moveToFirst();
  }
  return mCursor;
         
        
}

    public Cursor fetchJobsById(final int id) throws SQLException {
        Log.w(TAG, "fetchJobsById");
        Cursor mCursor = null;

        String[] selection = new String[] {KEY_POZ,KEY_FAV,
                KEY_KOM, KEY_QYT, KEY_SKA,KEY_TAG, KEY_ORA, KEY_KON, KEY_IMG, KEY_ROWID};
        if (id == 1){

            String orderBy = KEY_ROWID+" DESC";

            mCursor = mDb.query(SQLITE_TABLE,selection ,
                    null, null,
                    null, null, orderBy, null);

        }else
        if (id == 0){

            String orderBy = KEY_ROWID+" DESC";
            String limit = "10";


            mCursor = mDb.query(SQLITE_TABLE, selection,
                    null, null,
                    null, null, orderBy, limit);

        }
        else if(id>=11) {
            String limit = id+",10";


            mCursor = mDb.query(SQLITE_TABLE, selection,
                    null, null,
                    null, null, null, limit);
        }else{
            String limit = "0,"+id;


            mCursor = mDb.query(SQLITE_TABLE, selection,
                    null, null,
                    null, null, null, limit);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;


    }

    public long updateFav(int id, int fav) {

        String whereClause = KEY_ROWID+"="+id;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_FAV, fav);

        return mDb.update(SQLITE_TABLE,initialValues, whereClause, null);
    }

    public Cursor fetchFav() throws SQLException {
        Log.w(TAG, "fetchFavs");
        Cursor mCursor = null;

        String[] columns = new String[] {KEY_POZ,KEY_FAV,
                KEY_KOM, KEY_QYT, KEY_SKA, KEY_KON, KEY_PER, KEY_ORA, KEY_TAG, KEY_IMG, KEY_ROWID};

        String orderBy = KEY_ROWID+" DESC";
        String whereClause = "favorite=1";

        mCursor = mDb.query(SQLITE_TABLE,columns ,
                    whereClause, null,
                    null, null, orderBy, null);



        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;


    }

    public Cursor fetchJobsByFilter(String inputText) throws SQLException {
        Log.w(TAG, "fetchJobsByFilter");
        Cursor mCursor = null;

        String[] selection = new String[] {KEY_POZ,KEY_FAV,
                KEY_KOM, KEY_QYT, KEY_SKA, KEY_KON, KEY_TAG, KEY_ORA, KEY_IMG, KEY_ROWID};

        if (inputText == null  ||  inputText.length () == 0)  {
            mCursor = mDb.query(SQLITE_TABLE, selection,
                    null, null, null, null, null);

        }
        else {
            mCursor = mDb.query(true, SQLITE_TABLE, selection,
                    KEY_TAG+ " like '%" + inputText + "%'", null,
                    null, null, null, null);
        }

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;


    }
 
 }
 

