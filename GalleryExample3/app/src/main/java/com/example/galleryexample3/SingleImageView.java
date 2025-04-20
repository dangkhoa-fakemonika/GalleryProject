package com.example.galleryexample3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.galleryexample3.businessclasses.ClipBoardProcessing;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.businessclasses.ImageWallpaperManager;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.imageediting.BarCodeScannerClass;
import com.example.galleryexample3.imageediting.EditView;
import com.example.galleryexample3.imageediting.ImageClipboard;
import com.example.galleryexample3.imageediting.PaintingActivity;
import com.example.galleryexample3.imageediting.TagAnalyzerClass;
import com.example.galleryexample3.imageediting.TextRecognitionClass;
import com.example.galleryexample3.userinterface.SwipeImageAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SingleImageView extends Activity implements PopupMenu.OnMenuItemClickListener {
    public final static String FLAG_ALBUM = "albumName";
    public final static String FLAG_TAG = "tagName";
    public final static String FLAG_SEARCH_NAME = "fileName";

    private String imageURI;
    private int position;
    private String dateAdded;
    private int shortAnimationDuration;
    private DatabaseHandler databaseHandler;
    private Context context;
    private MediaStoreObserver mediaStoreObserver;
    private AlertDialog alertDialog;
    private ArrayList<String> imagesList;
    private ViewPager2 viewPager;
    private View.OnClickListener toggleUtility;
    private boolean osv = false;
    private String matchName;
    private String albumName;
    private String tagName;
    public class MediaStoreObserver extends ContentObserver {
        public MediaStoreObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (alertDialog != null) {
                alertDialog.dismiss();
            }

            imagesList = ImageGalleryProcessing.getImages(SingleImageView.this, "DATE_ADDED", " DESC");
            SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(SingleImageView.this, imagesList);
            viewPager.setAdapter(swipeImageAdapter);
            if (!imagesList.contains(imageURI)) {
                position = Math.min(imagesList.size() - 1, Math.max(0, position - 1));
                viewPager.setCurrentItem(position, true);
            } else {
                position = imagesList.indexOf(imageURI);
                viewPager.setCurrentItem(position, false);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.screenLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        RelativeLayout screenLayout = (RelativeLayout) findViewById(R.id.screenLayout);
        viewPager = (ViewPager2) findViewById(R.id.imageViewPager);

        RelativeLayout utilityLayout = (RelativeLayout) findViewById(R.id.utilityLayout);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        TextView dateAddedText = (TextView) findViewById(R.id.dateAddedText);

        ImageButton editModeButton = (ImageButton) findViewById(R.id.editModeButton);
        ImageButton drawModeButton = (ImageButton) findViewById(R.id.drawModeButton);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        ImageButton moreOptionButton = (ImageButton) findViewById(R.id.moreOptionButton);
        ImageButton shareButton = (ImageButton) findViewById(R.id.shareButton);

        // View/hide utility buttons
        toggleUtility = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (utilityLayout.getVisibility() == View.VISIBLE)
                    utilityLayout.animate()
                            .alpha(0f)
                            .setDuration(shortAnimationDuration)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    utilityLayout.setVisibility(View.GONE);
                                }
                            });
                else {
                    utilityLayout.setAlpha(0f);
                    utilityLayout.setVisibility(View.VISIBLE);
                    utilityLayout.animate()
                            .alpha(1f)
                            .setDuration(shortAnimationDuration)
                            .setListener(null);
                }
            }
        };

//        TextRecognitionClass textRecognitionClass = new TextRecognitionClass();
//        TagAnalyzerClass tagAnalyzerClass = new TagAnalyzerClass();
//        BarCodeScannerClass barCodeScannerClass = new BarCodeScannerClass();

        // Get bundle from previous screen
        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        databaseHandler = new DatabaseHandler(this);
        context = this;
        if (gotBundle == null)
            return;
        matchName = gotBundle.getString(FLAG_SEARCH_NAME);
        albumName = gotBundle.getString(FLAG_ALBUM);
        tagName = gotBundle.getString(FLAG_TAG);
        imageURI = gotBundle.getString("imageURI");
        dateAdded = gotBundle.getString("dateAdded");
        position = gotBundle.getInt("position");
        dateAddedText.setText(dateAdded);
        
        if (matchName != null){
            imagesList = ImageGalleryProcessing.getImagesByName(getApplicationContext(), matchName, "DATE_ADDED", " DESC");
            Log.v("matchName", matchName);
        } else if (albumName != null) {
            imagesList = databaseHandler.albums().getImagesOfAlbum(albumName);
            Log.v("albumName", albumName);
        } else if (tagName != null) {
//            Currently has no Tag retrieve Logic
            imagesList = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " DESC");
            Log.v("tagName", tagName);

        }else {
            imagesList = ImageGalleryProcessing.getImages(this, "DATE_ADDED", " DESC");
        }

        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setOnClickListener(toggleUtility);
            }
        });

        // Set up swiping between images
        Log.e("Received Position", String.valueOf(position) + " " + String.valueOf(imagesList.size()));

        SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(this, imagesList);
        viewPager.setAdapter(swipeImageAdapter);
//        viewPager.setCurrentItem(imagesList.size()-1, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                super.onPageSelected(pos);
                imageURI = imagesList.get(pos);
                position = pos;
            }
        });

        backButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, MainActivityNew.class);
            startActivity(intent);
        });

        editModeButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, EditView.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        drawModeButton.setOnClickListener(listener -> {
            Intent intent = new Intent(SingleImageView.this, PaintingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        deleteButton.setOnClickListener((l) -> {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Image?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean r = ImageGalleryProcessing.deleteImage(context, imageURI);
                            //boolean r = ImageGalleryProcessing.changeNameImage(this, imageURI, "newtest1.png");
                            if (r){
                                Toast.makeText(context, "Image deleted.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SingleImageView.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(context, "Image can't be deleted.", Toast.LENGTH_LONG).show();
                            }
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
        });

        shareButton.setOnClickListener((l) -> {
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),this.getPackageName() + ".provider", new File(imageURI));
            Intent share = ShareCompat.IntentBuilder.from(this)
                    .setStream(uri)
                    .setType("text/html")
                    .getIntent()
                    .setAction(Intent.ACTION_SEND)
                    .setDataAndType(uri, "image/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(share, "Share File"));
        });

        // Show menu
        moreOptionButton.setOnClickListener(this::showMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        imagesList = ImageGalleryProcessing.getImages(SingleImageView.this, "DATE_ADDED", " DESC");
        SwipeImageAdapter swipeImageAdapter = new SwipeImageAdapter(SingleImageView.this, imagesList);
        viewPager.setAdapter(swipeImageAdapter);
        if (!imagesList.contains(imageURI)) {
            position = Math.min(imagesList.size() - 1, Math.max(0, position - 1));
            viewPager.setCurrentItem(position, true);
        } else {
            position = imagesList.indexOf(imageURI);
            viewPager.setCurrentItem(position, false);
        }

        if (!osv) {
            Handler handler = new Handler();
            mediaStoreObserver = new MediaStoreObserver(handler);

            ContentResolver contentResolver = this.getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mediaStoreObserver);
            osv = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.getContentResolver().unregisterContentObserver(mediaStoreObserver);
        osv = false;
    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.single_image_view_menu);
        popup.show();
    }

    // Handle menu item click
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.addAlbum){
            View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Album Name");
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Album")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String albumName = editText.getText().toString();
//                            Log.i("ALBUM", albumName);
                            databaseHandler.albums().addImageToAlbum(albumName, imageURI);
                            Toast.makeText(context, "Added to " + albumName, Toast.LENGTH_LONG).show();

                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("ALBUM", "Cancel");
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return true;
        }
        else if (id == R.id.addTag) {
            View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Tag Name");
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tagName = editText.getText().toString();
                            Log.i("TAG", tagName);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("TAG", "Cancel");
                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return true;
        }
        else if (id == R.id.analyzeText) {
//            TextRecognitionClass.getTextFromImage(this, imageURI);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(imageURI), 0);

            recognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text visionText) {
                            String resultText = visionText.getText();

                            if (resultText.trim().isEmpty())
                                Toast.makeText(context,"No words scanned", Toast.LENGTH_SHORT).show();

                            View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.text_extraction_dialog_layout, null);

                            AlertDialog alertDialog = new AlertDialog.Builder(context)
                                    .setTitle("Text Scanned")
                                    .setView(dialogView)
                                    .setPositiveButton("Copy Text", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ClipBoardProcessing.getTextToClipBoard(context, resultText);
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).create();
                            TextView title = dialogView.findViewById(R.id.textExtractionTextView);
                            title.setText(resultText);
                            title.setMovementMethod(new ScrollingMovementMethod());
                            alertDialog.show();
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(context,"Words scanned failed", Toast.LENGTH_SHORT).show();
                                }
                            });
            return true;
        }
        else if (id == R.id.moreInfo) {
            Intent intent = new Intent(SingleImageView.this, MoreInformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            bundle.putString("dateAdded", dateAdded);
            bundle.putInt("position", position);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.qrread){
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE,Barcode.FORMAT_AZTEC).build();

            BarcodeScanner scanner = BarcodeScanning.getClient();

            InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(imageURI), 0);
            scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            for (Barcode barcode: barcodes) {
                                int valueType = barcode.getValueType();
                                switch (valueType) {
                                    case Barcode.TYPE_WIFI:
                                        String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                                        String password = barcode.getWifi().getPassword();

//                                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                                        boolean isConnectable = wifiManager.isEasyConnectSupported();
//                                        if (isConnectable){
//                                            wifiManager.
//                                        }
//                                        else {
//
//                                        }

                                        View wifiDialog = LayoutInflater.from(SingleImageView.this).inflate(R.layout.selection_dialog_layout, null);
                                        TextView wifiName = wifiDialog.findViewById(R.id.setBackScreen);
                                        wifiName.setText("Wifi Name: " + ssid);
                                        TextView wifiPassword = wifiDialog.findViewById(R.id.setLockScreen);
                                        wifiPassword.setText("Password: " + password);
                                        AlertDialog alertDialogWifi = new AlertDialog.Builder(context)
                                                .setTitle("Wifi Information")
                                                .setView(wifiDialog)
                                                .setPositiveButton("Copy Password", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        ClipBoardProcessing.getTextToClipBoard(context, password);
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).create();
                                        alertDialogWifi.show();
                                        break;

                                    case Barcode.TYPE_URL:
                                        String title = Objects.requireNonNull(barcode.getUrl()).getTitle();
                                        String url = barcode.getUrl().getUrl();

                                        View urlDialog = LayoutInflater.from(SingleImageView.this).inflate(R.layout.selection_dialog_layout, null);
                                        TextView urlTitle = urlDialog.findViewById(R.id.setBackScreen);
                                        urlTitle.setText((title == null || !title.isEmpty()) ? title : "No Title");
                                        EditText urlGenerated = urlDialog.findViewById(R.id.setLockScreen);
                                        urlGenerated.setText(url);
                                        AlertDialog alertDialogUrl = new AlertDialog.Builder(context)
                                                .setTitle("URL Information")
                                                .setView(urlDialog)
                                                .setPositiveButton("Go To URL", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse(url));
                                                        startActivity(browse);
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setNeutralButton("Copy URL", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        ClipBoardProcessing.getTextToClipBoard(context, url);
                                                        dialogInterface.dismiss();
                                                    }
                                                }).create();
                                        alertDialogUrl.show();
//                                        ClipBoardProcessing.getTextToClipBoard(context, url);
                                        break;

                                    default:
                                        String text = barcode.getRawValue();

                                        View normalDialog = LayoutInflater.from(SingleImageView.this).inflate(R.layout.displaying_data_dialog_layout, null);
                                        TextView normalTitle = normalDialog.findViewById(R.id.extraField);
                                        normalTitle.setText("Plain text scanned");
                                        EditText textGenerated = normalDialog.findViewById(R.id.editText);
                                        textGenerated.setText(text);
                                        AlertDialog textDialogUrl = new AlertDialog.Builder(context)
                                                .setTitle("Plain text")
                                                .setView(normalDialog)
                                                .setPositiveButton("Copy text", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        ClipBoardProcessing.getTextToClipBoard(context, text);
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).create();
                                        textDialogUrl.show();

                                        ClipBoardProcessing.getTextToClipBoard(context, text);
                                        break;

                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "No QR available.", Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        else if (id == R.id.setWallpaper){
            View dialogView = LayoutInflater.from(SingleImageView.this).inflate(R.layout.selection_dialog_layout, null);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Set Background For")
                    .setView(dialogView)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            dialogView.findViewById(R.id.setBackScreen).setOnClickListener((l) -> {
                ImageWallpaperManager.setWallpaper(context, imageURI, 0);
                alertDialog.dismiss();
            });
            dialogView.findViewById(R.id.setLockScreen).setOnClickListener((l) -> {
                ImageWallpaperManager.setWallpaper(context, imageURI, 1);
                alertDialog.dismiss();
            });
            alertDialog.show();
            return true;
        }
        else if (id == R.id.copyImageClipboard){
            ImageClipboard.getImageToClipBoard(context, imageURI);
            Toast.makeText(context, "Image copied to clipboard.", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
            return false;
    }
}
