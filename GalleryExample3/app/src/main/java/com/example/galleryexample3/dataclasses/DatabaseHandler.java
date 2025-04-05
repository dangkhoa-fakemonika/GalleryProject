package com.example.galleryexample3.dataclasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

class AlbumsTable{
    public static final String TABLE_NAME = "albums";
    public static final String COL_NAME = "name";
    public static final String COL_IMAGE_URI = "imageUri";

    public static String createTableQuery(){
        return "CREATE TABLE " + TABLE_NAME + "(" +
                COL_NAME + " TEXT, " +
                COL_IMAGE_URI + " TEXT)";
    }
}

class AlbumsInfoTable{
    public static final String TABLE_NAME = "albums_info";
    public static final String COL_NAME = "name";
    // more info

    public static String createTableQuery(){
        return "CREATE TABLE " + TABLE_NAME + "(" +
                COL_NAME + " TEXT)";
    }
}

class TagsTable{
    public static final String TABLE_NAME = "tags";
    public static final String COL_NAME = "name";
    public static final String COL_IMAGE_URI = "imageuri";

    public static String createTableQuery(){
        return "CREATE TABLE " + TABLE_NAME + "(" +
                COL_NAME + " TEXT, " +
                COL_IMAGE_URI + " TEXT) ";
    }
}

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "hddb";
    private static final int DB_VERSION = 3;
    private static SQLiteDatabase sqLiteDatabase;

    public DatabaseHandler(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        if (sqLiteDatabase == null)
            sqLiteDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(AlbumsTable.createTableQuery());
        sqLiteDatabase.execSQL(AlbumsInfoTable.createTableQuery());
        sqLiteDatabase.execSQL(TagsTable.createTableQuery());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumsTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumsInfoTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TagsTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public AlbumTableController albums(){
        return new AlbumTableController();
    }

    public TagsController tags(){
        return new TagsController();
    }


    public static class AlbumTableController {

        public AlbumTableController(){

        }

        public void addImageToAlbum(String albumName, String imageURI){
            createAlbum(albumName);

            ContentValues values = new ContentValues();
            values.put(AlbumsTable.COL_NAME, albumName);
            values.put(AlbumsTable.COL_IMAGE_URI, imageURI);
            sqLiteDatabase.insert(AlbumsTable.TABLE_NAME, null, values);
        }

        public void createAlbum(String albumName){
            Cursor cur = sqLiteDatabase.query(AlbumsInfoTable.TABLE_NAME, new String[] {AlbumsInfoTable.COL_NAME}, AlbumsInfoTable.COL_NAME + "=?", new String[]{albumName}, null, null, null);

            if (cur.getCount() == 0){
                ContentValues values = new ContentValues();
                values.put(AlbumsInfoTable.COL_NAME, albumName);
                sqLiteDatabase.insert(AlbumsInfoTable.TABLE_NAME, null, values);
            }

            cur.close();
        }

        public void deleteAlbum(String albumName){
            sqLiteDatabase.delete(AlbumsTable.TABLE_NAME, AlbumsTable.COL_NAME + " =?", new String[]{albumName});
            sqLiteDatabase.delete(AlbumsInfoTable.TABLE_NAME, AlbumsInfoTable.COL_NAME + " =?", new String[]{albumName});
        }

        public ArrayList<String> getImagesOfAlbum(String albumName){
            Cursor cur = sqLiteDatabase.query(AlbumsTable.TABLE_NAME, new String[] {AlbumsTable.COL_IMAGE_URI}, AlbumsTable.COL_NAME + " =?", new String[]{albumName}, null, null, null);
            ArrayList<String> result = new ArrayList<>();

            if (cur.getCount() == 0)
                return result;

            cur.moveToFirst();
            do {
                int uriCol = cur.getColumnIndex(AlbumsTable.COL_IMAGE_URI);
                String uri = null;
                if (uriCol != -1)
                    uri = cur.getString(uriCol);
                result.add(uri);
            } while (cur.moveToNext());

            cur.close();
            return result;
        }

        public ArrayList<String> getAllAlbums(){
            Cursor cur = sqLiteDatabase.query(AlbumsInfoTable.TABLE_NAME, new String[] {AlbumsInfoTable.COL_NAME}, null, null, null, null, null);
            ArrayList<String> result = new ArrayList<>();

            if (cur.getCount() == 0)
                return result;
            
            cur.moveToFirst();
            do {
                int uriCol = cur.getColumnIndex(AlbumsInfoTable.COL_NAME);
                String uri = null;
                if (uriCol != -1)
                    uri = cur.getString(uriCol);
                result.add(uri);
            } while (cur.moveToNext());

            cur.close();
            return result;
        }

        public ArrayList<String> getAllAlbumsTemp(){
            Cursor cur = sqLiteDatabase.query(true, AlbumsTable.TABLE_NAME, new String[] {AlbumsTable.COL_NAME},  null,null, null, null, null, null);
            ArrayList<String> result = new ArrayList<>();

            if (cur.getCount() == 0)
                return result;

            cur.moveToFirst();
            do {
                int uriCol = cur.getColumnIndex(AlbumsTable.COL_NAME);
                String uri = null;
                if (uriCol != -1)
                    uri = cur.getString(uriCol);
                result.add(uri);
            } while (cur.moveToNext());

            cur.close();
            return result;
        }
    }

    public static class TagsController {

        public TagsController(){

        }

        public void addTagsToImage(String tagName, String imageURI){
            ContentValues values = new ContentValues();
            values.put(TagsTable.COL_NAME, tagName);
            values.put(TagsTable.COL_IMAGE_URI, imageURI);
            long a = sqLiteDatabase.insert(TagsTable.TABLE_NAME, null, values);
            Log.i("INSERT CHECK",  a + "");
        }

        public void deleteTag(String tagName){
            sqLiteDatabase.delete(TagsTable.TABLE_NAME, TagsTable.COL_NAME + " =?", new String[]{tagName});
        }

        public void removeTag(String tagName, String imageURI){
            sqLiteDatabase.delete(TagsTable.TABLE_NAME, TagsTable.COL_NAME + " =? AND " + TagsTable.COL_IMAGE_URI + " =?", new String[]{tagName, imageURI});
        }

        public ArrayList<String> getAllTags(){
            Cursor cur = sqLiteDatabase.query(true, TagsTable.TABLE_NAME, new String[] {TagsTable.COL_NAME}, null, null, null, null, null, null);
            ArrayList<String> result = new ArrayList<>();

            if (cur.getCount() == 0)
                return result;

            cur.moveToFirst();
            do {
                int uriCol = cur.getColumnIndex(TagsTable.COL_NAME);
                String uri = null;
                if (uriCol != -1)
                    uri = cur.getString(uriCol);
                result.add(uri);
            } while (cur.moveToNext());

            cur.close();
            return result;
        }

        public ArrayList<String> getTagsOfImage(String imageURI){
            Cursor cur = sqLiteDatabase.query(TagsTable.TABLE_NAME, new String[] {TagsTable.COL_NAME}, TagsTable.COL_IMAGE_URI + " =?", new String[]{imageURI}, null, null, null);
            ArrayList<String> result = new ArrayList<>();

            if (cur.getCount() == 0)
                return result;

            cur.moveToFirst();
            do {
                int uriCol = cur.getColumnIndex(TagsTable.COL_NAME);
                String uri = null;
                if (uriCol != -1)
                    uri = cur.getString(uriCol);
                result.add(uri);
            } while (cur.moveToNext());

            cur.close();
            return result;
        }

        public ArrayList<String> getImagesOfTag(String tagName){
            Cursor cur = sqLiteDatabase.query(TagsTable.TABLE_NAME, new String[] {TagsTable.COL_IMAGE_URI}, TagsTable.COL_NAME + " =?", new String[]{tagName}, null, null, null);
            ArrayList<String> result = new ArrayList<>();

            if (cur.getCount() == 0)
                return result;

            cur.moveToFirst();

            do {
                int uriCol = cur.getColumnIndex(TagsTable.COL_IMAGE_URI);
                String uri = null;
                if (uriCol != -1)
                    uri = cur.getString(uriCol);
                result.add(uri);
            } while (cur.moveToNext());

            cur.close();
            return result;
        }

    }
}
