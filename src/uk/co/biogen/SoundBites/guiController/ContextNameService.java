package uk.co.biogen.SoundBites.guiController;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class ContextNameService
{
  public static void publishContextName(Context broadcaster, String contextName)
  {
    Log.d("attn", "Broadcasting context name " + contextName);
    
    Intent name = new Intent();
    name.setAction("uk.co.biogen.SOUNDBITES_CONTEXT_NAME");
    name.putExtra("cnsContextName", contextName);
    broadcaster.sendBroadcast(name);
  }
}

/*  
 * As ContentProvider - needed if the context data footprint becomes greater,
 * as will happen if the application works with an ontology.
 */

/*
package uk.co.biogen.SoundBites.guiController;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class ContextNameService extends ContentProvider
{
  CNSDatabase cnsDB;
  
  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs)
  {
    return 0;
  }

  @Override
  public String getType(Uri uri)
  {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values)
  {
    return null;
  }

  @Override
  public boolean onCreate()
  {
    cnsDB = new CNSDatabase(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder)
  {
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs)
  {
    return 0;
  }
  
  public void setContextName(String name)
  {
//    contextName = name;
  }
  
  private static class CNSDatabase extends SQLiteOpenHelper
  {
//    private static final String DEBUG_TAG = "ContextNameServiceDB";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "contextNameDB";
    
    public static final String TABLE_NAME = "tblName";
    public static final String ID = "_id";
    public static final String COL_TITLE = "name";
    public static final String COL_URL = "url";
    private static final String CREATE_TABLE_NAME = "create table " + TABLE_NAME
    + " (" + ID + " integer primary key autoincrement, " + COL_TITLE
    + " text not null, " + COL_URL + " text not null);";
     
    private static final String DB_SCHEMA = CREATE_TABLE_NAME;
 
    public CNSDatabase(Context context)
    {
      super(context, DB_NAME, null, DB_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db)
    {
      db.execSQL(DB_SCHEMA);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
      // no upgrade policy
      Log.d("soundBites", "Upgrading database (" + oldVersion + " -> " +
        newVersion + ". Existing contents will be lost.");
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
      onCreate(db);
    }
    
    public void publishContextName(String contextName)
    {
      // convenience method for immediately publishing a context name
    }
  }
}
*/
