package com.example.galleryexample3.dataclasses;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.galleryexample3.userinterface.SearchItemListAdapter;

import java.io.File;
import java.lang.reflect.Array;
import java.security.AllPermission;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

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
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TIME_CREATE = "time_create";
    public static final String THUMBNAIL_URI = "thumbnail_uri";
    public static String createTableQuery(){
        return "CREATE TABLE " + TABLE_NAME + "(" +
                COL_NAME + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_TIME_CREATE + " TEXT, " +
                THUMBNAIL_URI + " TEXT)";
    }
}

class TagsTable{
    public static final String TABLE_NAME = "tags";
    public static final String COL_NAME = "name";
    public static final String COL_IMAGE_URI = "imageUri";

    public static String createTableQuery(){
        return "CREATE TABLE " + TABLE_NAME + "(" +
                COL_NAME + " TEXT, " +
                COL_IMAGE_URI + " TEXT) ";
    }
}

class TagsInfoTable{
    public static final String TABLE_NAME = "tags_info";
    public static final String COL_NAME = "name";

    public static String createTableQuery(){
        return "CREATE TABLE " + TABLE_NAME + "(" +
                COL_NAME + " TEXT) ";
    }
}

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "hddb";
    private static final int DB_VERSION = 6;
    private static volatile DatabaseHandler instance = null;
    private static SQLiteDatabase sqLiteDatabase;

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null)
            synchronized (DatabaseHandler.class) {
                if (instance == null)
                    instance = new DatabaseHandler(context);
            }
        return instance;
    }

    private DatabaseHandler(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        if (sqLiteDatabase == null)
            sqLiteDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(AlbumsTable.createTableQuery());
        sqLiteDatabase.execSQL(AlbumsInfoTable.createTableQuery());
        sqLiteDatabase.execSQL(TagsTable.createTableQuery());
        sqLiteDatabase.execSQL(TagsInfoTable.createTableQuery());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumsTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlbumsInfoTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TagsTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TagsInfoTable.TABLE_NAME);
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
            try (Cursor cursor = sqLiteDatabase.query(AlbumsTable.TABLE_NAME, new String[] {AlbumsTable.COL_NAME}, AlbumsTable.COL_NAME + " =? AND " + AlbumsTable.COL_IMAGE_URI + " =?", new String[]{albumName, imageURI}, null, null, null)) {
                if (cursor.getCount() > 0) return;
            }
            ContentValues values = new ContentValues();
            values.put(AlbumsTable.COL_NAME, albumName);
            values.put(AlbumsTable.COL_IMAGE_URI, imageURI);
            sqLiteDatabase.insert(AlbumsTable.TABLE_NAME, null, values);
            if (!checkAlbumExisted(albumName)) {
                setAlbumThumbnail(albumName, imageURI);
                createAlbum(albumName);
            }
            setAlbumThumbnail(albumName, imageURI);
        }

        public boolean checkAlbumExisted(String albumName){
            Cursor cur = sqLiteDatabase.query(AlbumsInfoTable.TABLE_NAME, new String[] {AlbumsInfoTable.COL_NAME}, AlbumsInfoTable.COL_NAME + " =?", new String[]{albumName}, null, null, null);
            int count = cur.getCount();
            cur.close();
            return count != 0;
        }

        public void setAlbumThumbnail(String albumName, String imageURI){
            ContentValues values = new ContentValues();
            values.put(AlbumsInfoTable.THUMBNAIL_URI, imageURI);
            sqLiteDatabase.update(AlbumsInfoTable.TABLE_NAME, values,AlbumsInfoTable.COL_NAME + " = ?", new String[]{albumName});
        }
        public void createAlbum(String albumName){
            if (checkAlbumExisted(albumName))
                return;

            Cursor cur = sqLiteDatabase.query(AlbumsInfoTable.TABLE_NAME, new String[] {AlbumsInfoTable.COL_NAME}, AlbumsInfoTable.COL_NAME + "=?", new String[]{albumName}, null, null, null);

            if (cur.getCount() == 0){
                ContentValues values = new ContentValues();
                values.put(AlbumsInfoTable.COL_NAME, albumName);
                values.put(AlbumsInfoTable.COL_DESCRIPTION, "something");
                values.put(AlbumsInfoTable.COL_TIME_CREATE, new Date().toString());
                values.put(AlbumsInfoTable.THUMBNAIL_URI, "no_thumbnail");
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
                if (uriCol != -1) {
                    uri = cur.getString(uriCol);
                    if (new File(uri).exists())
                        result.add(uri);
                    else deleteImage(uri);
                }
            } while (cur.moveToNext());

            cur.close();
            return result;
        }

        public void removeImageFromAlbum(String albumName, String imageURI){
            sqLiteDatabase.delete(AlbumsTable.TABLE_NAME, AlbumsTable.COL_NAME + " =? AND " + AlbumsTable.COL_IMAGE_URI + " =?", new String[]{albumName, imageURI});
            if (imageURI.equals(getAlbumThumbnail(albumName))) {
                changeAlbumThumbnail(albumName);
            }
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

        public String getAlbumThumbnail(String albumName) {
            try (Cursor cursor = sqLiteDatabase.query(AlbumsInfoTable.TABLE_NAME, new String[] {AlbumsInfoTable.COL_NAME, AlbumsInfoTable.THUMBNAIL_URI}, AlbumsTable.COL_NAME + " = ?", new String[]{albumName}, null, null, null)) {
                if (cursor.moveToFirst()) {
                    int uriCol = cursor.getColumnIndex(AlbumsInfoTable.THUMBNAIL_URI);
                    String uri = cursor.getString(uriCol);
                    if (new File(uri).exists())
                        return uri;
                    else {
                        deleteImage(uri);
                        return getAlbumThumbnail(albumName);
                    }
                }
            } catch (Exception e) {
                Log.e("Error", "Can not get album thumbnail", e);
            }
            return null;
        }

        public ArrayList<SearchItemListAdapter.MatchItem> getMatchAlbumItems(String name) {
            ArrayList<SearchItemListAdapter.MatchItem> l = new ArrayList<>();
            String[] args = new String[]{"%"+ name + "%"};
            String statement = "SELECT albums.name, count(albums.imageURI) as totals, albums_info.thumbnail_uri " +
                    "FROM albums " +
                    "JOIN albums_info on albums_info.name = albums.name " +
                    "WHERE albums.name LIKE ? " +
                    "GROUP BY albums.name, albums_info.name";

            try(Cursor cursor = sqLiteDatabase.rawQuery(statement, args)){
                if (cursor.moveToFirst()){
                    Log.e("Something here", String.valueOf(cursor.getCount()));
                    int columnName = cursor.getColumnIndex("name");
                    int countIndex = cursor.getColumnIndex("totals");
                    int thumbnailUri = cursor.getColumnIndex("thumbnail_uri");
                    int matchType = SearchItemListAdapter.MATCH_ALBUM;
                    do {
                        String matchName = cursor.getString(columnName);
                        String albumsTotals = String.valueOf(cursor.getLong(countIndex));
                        String uri = cursor.getString(thumbnailUri);
                        if (new File(uri).exists())
                            l.add(new SearchItemListAdapter.MatchItem(matchName, albumsTotals, uri, matchType));
                        else {
                            deleteImage(uri);
                            return getMatchAlbumItems(name);
                        }
                    } while (cursor.moveToNext());
                }
            }catch (Exception e){
                Log.e("DatabaseHandler", e.toString());
            }
            return l;
        }

        public ArrayList<String> getAllAlbumsWithFilters(String field, String order) {
            ArrayList<String> l = new ArrayList<>();
            String[] args = new String[]{};
            String statement = "SELECT albums.name, count(albums.imageURI) as totals, albums_info.thumbnail_uri, albums_info.time_create " +
                    "FROM albums " +
                    "JOIN albums_info on albums_info.name = albums.name " +
                    "GROUP BY albums.name, albums_info.name " +
                    "ORDER BY " + field + order;

            try(Cursor cursor = sqLiteDatabase.rawQuery(statement, args)){
                if (cursor.moveToFirst()){
                    Log.e("Something here", String.valueOf(cursor.getCount()));
                    int columnName = cursor.getColumnIndex("name");
                    int thumbnailUri = cursor.getColumnIndex("thumbnail_uri");
                    do {
                        String matchName = cursor.getString(columnName);
                        String uri = cursor.getString(thumbnailUri);
                        if (new File(uri).exists())
                            l.add(matchName);
                        else {
                            deleteImage(uri);
                            return getAllAlbumsWithFilters(field, order);
                        }
                    } while (cursor.moveToNext());
                }
            }catch (Exception e){
                Log.e("DatabaseHandler", e.toString());
            }
            return l;
        }

//        public ArrayList<String> getAllAlbumsTemp(){
//            Cursor cur = sqLiteDatabase.query(true, AlbumsTable.TABLE_NAME, new String[] {AlbumsTable.COL_NAME},  null,null, null, null, null, null);
//            ArrayList<String> result = new ArrayList<>();
//
//            if (cur.getCount() == 0)
//                return result;
//
//            cur.moveToFirst();
//            do {
//                int uriCol = cur.getColumnIndex(AlbumsTable.COL_NAME);
//                String uri = null;
//                if (uriCol != -1)
//                    uri = cur.getString(uriCol);
//                result.add(uri);
//            } while (cur.moveToNext());
//
//            cur.close();
//            return result;
//        }

        public void updateAlbum(String albumName, String newAlbumName, String newDescription){
            ContentValues infoValues = new ContentValues();
            infoValues.put(AlbumsInfoTable.COL_NAME, newAlbumName);
            infoValues.put(AlbumsInfoTable.COL_DESCRIPTION, newDescription);
            infoValues.put(AlbumsInfoTable.COL_TIME_CREATE, new Date().toString());

            sqLiteDatabase.update(AlbumsInfoTable.TABLE_NAME, infoValues, AlbumsInfoTable.COL_NAME + " =?", new String[]{albumName});

            ContentValues albumValues = new ContentValues();
            albumValues.put(AlbumsTable.COL_NAME, newAlbumName);
            sqLiteDatabase.update(AlbumsTable.TABLE_NAME, albumValues, AlbumsTable.COL_NAME + " =?", new String[]{albumName});
        }

        public void changeAlbumThumbnail (String albumName) {
            try (Cursor cursor = sqLiteDatabase.query(AlbumsTable.TABLE_NAME, new String[]{AlbumsTable.COL_IMAGE_URI}, AlbumsTable.COL_NAME + " = ?", new String[]{albumName}, null, null, null)) {
                if (cursor.moveToLast()) {
                    @SuppressLint("Range") String newThumb = cursor.getString(cursor.getColumnIndex(AlbumsTable.COL_IMAGE_URI));
                    setAlbumThumbnail(albumName, newThumb);
                }
            }
        }
        public void deleteImage(String imageURI) {
            sqLiteDatabase.delete(AlbumsTable.TABLE_NAME, AlbumsTable.COL_IMAGE_URI + " = ?", new String[]{imageURI});
            try (Cursor cursor = sqLiteDatabase.query(AlbumsInfoTable.TABLE_NAME, new String[]{AlbumsInfoTable.COL_NAME}, AlbumsInfoTable.THUMBNAIL_URI + " = ?", new String[]{imageURI}, null, null, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String albumName = cursor.getString(cursor.getColumnIndex("name"));
                        changeAlbumThumbnail(albumName);
                    } while (cursor.moveToNext());
                }
            }
        }

        public boolean checkAlbumHaveImage(String albumName, String imageURI){
            Cursor cur = sqLiteDatabase.query(AlbumsTable.TABLE_NAME, new String[] {AlbumsTable.COL_NAME}, AlbumsTable.COL_NAME + " =? AND " + AlbumsTable.COL_IMAGE_URI + " =?", new String[]{albumName, imageURI}, null, null, null);
            int count = cur.getCount();
            cur.close();
            return count != 0;
        }

        public void changeName(String oldAlbumName, String newAlbumName){
            if (checkAlbumExisted(newAlbumName))
                return;

            ContentValues value = new ContentValues();
            value.put(AlbumsTable.COL_NAME, newAlbumName);
            sqLiteDatabase.update(AlbumsTable.TABLE_NAME, value, AlbumsTable.COL_NAME + " =?", new String[]{oldAlbumName});
            ContentValues valueInfo = new ContentValues();
            valueInfo.put(AlbumsInfoTable.COL_NAME, newAlbumName);
            sqLiteDatabase.update(AlbumsInfoTable.TABLE_NAME, valueInfo, AlbumsInfoTable.COL_NAME + " =?", new String[]{oldAlbumName});
        }

        public void changeImageName(String oldImageName, String newImageName){
            ContentValues value = new ContentValues();
            value.put(AlbumsTable.COL_IMAGE_URI, newImageName);
            sqLiteDatabase.update(AlbumsTable.TABLE_NAME, value, AlbumsTable.COL_IMAGE_URI + " = ?", new String[]{oldImageName});
            value = new ContentValues();
            value.put(AlbumsInfoTable.THUMBNAIL_URI, newImageName);
            sqLiteDatabase.update(AlbumsInfoTable.TABLE_NAME, value, AlbumsInfoTable.THUMBNAIL_URI + " = ?", new String[]{oldImageName});
        }
    }

    public static class TagsController {

        public TagsController(){

        }

        public void createNewTag(String tagName){
            if (!checkTagExisted(tagName)){
                return;
            }
            ContentValues values = new ContentValues();
            values.put(TagsInfoTable.COL_NAME, tagName);
            sqLiteDatabase.insert(TagsInfoTable.TABLE_NAME, null, values);
        }

        public boolean checkTagExisted(String tagName){
            try (Cursor cur = sqLiteDatabase.query(TagsInfoTable.TABLE_NAME, new String[] {TagsInfoTable.COL_NAME}, TagsTable.COL_NAME + " =?", new String[]{tagName}, null, null, null, null)) {
                int count = cur.getCount();
                return count != 0;
            }
        }

        public void addTagsToImage(String tagName, String imageURI){
            try (Cursor cursor = sqLiteDatabase.query(TagsTable.TABLE_NAME, new String[] {TagsTable.COL_NAME}, TagsTable.COL_NAME + " =? AND " + AlbumsTable.COL_IMAGE_URI + " =?", new String[]{tagName, imageURI}, null, null, null)) {
                if (cursor.getCount() > 0) return;
            }
            createNewTag(tagName);
            ContentValues values = new ContentValues();
            values.put(TagsTable.COL_NAME, tagName);
            values.put(TagsTable.COL_IMAGE_URI, imageURI);
            sqLiteDatabase.insert(TagsTable.TABLE_NAME, null, values);
        }

        public void deleteTag(String tagName){
            sqLiteDatabase.delete(TagsTable.TABLE_NAME, TagsTable.COL_NAME + " =?", new String[]{tagName});
            sqLiteDatabase.delete(TagsInfoTable.TABLE_NAME, TagsInfoTable.COL_NAME + " =?", new String[]{tagName});
        }

        public void removeTag(String tagName, String imageURI){
            sqLiteDatabase.delete(TagsTable.TABLE_NAME, TagsTable.COL_NAME + " =? AND " + TagsTable.COL_IMAGE_URI + " =?", new String[]{tagName, imageURI});
        }

        public ArrayList<String> getAllTags(){
            try (Cursor cur = sqLiteDatabase.query(TagsInfoTable.TABLE_NAME, new String[] {TagsInfoTable.COL_NAME}, null, null, null, null, null, null)) {
                ArrayList<String> result = new ArrayList<>();

                if (cur.getCount() == 0)
                    return result;

                cur.moveToFirst();
                do {
                    int uriCol = cur.getColumnIndex(TagsInfoTable.COL_NAME);
                    String uri = null;
                    if (uriCol != -1) {
                        uri = cur.getString(uriCol);
                        result.add(uri);
                    } else {
                        deleteImage(uri);
                        return getAllTags();
                    }
                } while (cur.moveToNext());

                return result;
            }
        }

        public ArrayList<String> getTagsOfImage(String imageURI){
            try (Cursor cur = sqLiteDatabase.query(TagsTable.TABLE_NAME, new String[] {TagsTable.COL_NAME}, TagsTable.COL_IMAGE_URI + " =?", new String[]{imageURI}, null, null, null)) {
                ArrayList<String> result = new ArrayList<>();

                if (cur.getCount() == 0) {
                    return result;
                }
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
        }
        public ArrayList<SearchItemListAdapter.MatchItem> getMatchTagsItem(String name){
            ArrayList<SearchItemListAdapter.MatchItem> l = new ArrayList<>();
            String[] args = new String[]{"%"+ name + "%"};
            String statement = "select tags.name, count(tags.imageUri) as totals, tags.imageUri " +
                    " from tags " +
                    " where tags.name like ? " +
                    " group by tags.name";

            Log.v("Statement", statement);

            try(Cursor cursor = sqLiteDatabase.rawQuery(statement, args)){
                Log.v("After Query:", statement);
                if (cursor.moveToFirst()){
                    int columnName = cursor.getColumnIndex("name");
                    int countIndex = cursor.getColumnIndex("totals");
                    int thumbnailUri = cursor.getColumnIndex("imageUri");
                    int matchType = SearchItemListAdapter.MATCH_TAG;
                    do {
                        String matchName = cursor.getString(columnName);
                        String albumsTotals = String.valueOf(cursor.getLong(countIndex));
                        String uri = cursor.getString(thumbnailUri);
                        if (new File(uri).exists()) {
                            l.add(new SearchItemListAdapter.MatchItem(matchName, albumsTotals, uri, matchType));
                        } else {
                            deleteImage(uri);
                            return getMatchTagsItem(name);
                        }
                    } while (cursor.moveToNext());
                }
            }catch (Exception e){
                Log.e("DatabaseHandler", e.toString());
            }
            return l;
        }
        public ArrayList<String> getImagesOfTag(String tagName){
            try (Cursor cur = sqLiteDatabase.query(TagsTable.TABLE_NAME, new String[] {TagsTable.COL_IMAGE_URI}, TagsTable.COL_NAME + " =?", new String[]{tagName}, null, null, null)) {
                ArrayList<String> result = new ArrayList<>();

                if (cur.getCount() == 0)
                    return result;

                cur.moveToFirst();

                do {
                    int uriCol = cur.getColumnIndex(TagsTable.COL_IMAGE_URI);
                    String uri = null;
                    if (uriCol != -1) {
                        uri = cur.getString(uriCol);
                        if (new File(uri).exists()) {
                            result.add(uri);
                        } else {
                            deleteImage(uri);
                            return getImagesOfTag(tagName);
                        }
                    }
                } while (cur.moveToNext());

                return result;
            }
        }

        public void deleteImage(String imageURI) {
            sqLiteDatabase.delete(TagsTable.TABLE_NAME, TagsTable.COL_IMAGE_URI + " = ?", new String[]{imageURI});
        }

        public void changeImageName(String oldImageName, String newImageName){
            ContentValues value = new ContentValues();
            value.put(TagsTable.COL_IMAGE_URI, newImageName);
            sqLiteDatabase.update(TagsTable.TABLE_NAME, value, TagsTable.COL_IMAGE_URI + " = ?", new String[]{oldImageName});
        }
    }
}
