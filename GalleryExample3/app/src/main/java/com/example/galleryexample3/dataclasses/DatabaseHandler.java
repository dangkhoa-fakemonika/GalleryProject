package com.example.galleryexample3.dataclasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class AlbumsTable{
    public static final String TABLE_NAME = "albums";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_IMAGE_URI = "imageuri";


}

class TagsTable{
    public static final String TABLE_NAME = "tags";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_IMAGE_URI = "imageuri";
}

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "hddb";
    private static final int DB_VERSION = 1;



    public DatabaseHandler(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        sqLiteDatabase.execSQL("");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumsTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public static class AlbumTableController {
        public void createAlbumTable(String albumName){

        }
    }
}
