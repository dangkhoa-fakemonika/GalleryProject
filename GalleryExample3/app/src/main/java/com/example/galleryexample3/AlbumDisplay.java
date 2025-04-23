package com.example.galleryexample3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.ImageBaseAdapter;
import com.example.galleryexample3.userinterface.ScaleListener;

import java.util.ArrayList;
@Deprecated
public class AlbumDisplay extends Activity {
    /** The images. */

    private EditText rowNum;
    private Button changeColumns;
    private Button previousPage;
    private Button nextPage;
    private TextView pageNumber;

    private ScaleGestureDetector mScaleDetector;

    final int PICK_FROM_GALLERY = 101; // This could be any non-0 number lol
    final int NUM_IMAGE_LOAD_LIMIT = 20;

    private float mScaleFactor = 1.f;
    private int pageNum = 0;

    private ImageBaseAdapter imageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_view_old);

//        if (ActivityCompat.checkSelfPermission(AlbumDisplay.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(AlbumDisplay.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
//        }

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        rowNum = (EditText) findViewById(R.id.rowNum);
        changeColumns = (Button) findViewById(R.id.changeRowButton);
        previousPage = (Button) findViewById(R.id.previousPage);
        nextPage = (Button) findViewById(R.id.nextPage);
        pageNumber = (TextView) findViewById(R.id.pageNumber);

        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(this);

        ArrayList<String> albumImages = new ArrayList<>();
        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();


        if (gotBundle != null){
            String getAlbumName = gotBundle.getString("albumSavedName");
            albumImages.addAll(databaseHandler.albums().getImagesOfAlbum(getAlbumName));
        }

        imageAdapter = new ImageBaseAdapter(this, 20, albumImages);
        gallery.setAdapter(imageAdapter);
        mScaleDetector = new ScaleGestureDetector(gallery.getContext(), new ScaleListener(imageAdapter, gallery));

        // Change columns of the thing (not restricted yet)
        changeColumns.setOnClickListener((l) -> {
            String val = String.valueOf(rowNum.getText());
            int numVal = Integer.parseInt(val);

            gallery.setNumColumns(numVal);
        });

        previousPage.setOnClickListener((l) -> {
            pageNum = (pageNum > 0) ? pageNum - 1 : pageNum;
            pageNumber.setText("Page " + (pageNum + 1));
            imageAdapter.setPage(pageNum);
            imageAdapter.notifyDataSetChanged();
        });

        nextPage.setOnClickListener((l) -> {
            pageNum = (pageNum < imageAdapter.getMaxPage()) ? pageNum + 1 : pageNum;
            pageNumber.setText("Page " + (pageNum + 1));
            imageAdapter.setPage(pageNum);
            imageAdapter.notifyDataSetChanged();
        });

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (!albumImages.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "position " + position + " " + albumImages.get(position), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AlbumDisplay.this, SingleImageView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURI", albumImages.get(position + pageNum * NUM_IMAGE_LOAD_LIMIT));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });


    }
}




