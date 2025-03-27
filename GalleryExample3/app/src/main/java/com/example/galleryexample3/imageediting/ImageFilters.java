package com.example.galleryexample3.imageediting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.galleryexample3.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageFilters extends Activity {

    ImageView imageView;
    Bitmap imageBitmap;
    Bitmap displayBitmap;

    String imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image_edit);

        imageView = (ImageView) findViewById(R.id.imageView);
        Button normalButton = (Button) findViewById(R.id.normalButton);
        Button grayscaleButton = (Button) findViewById(R.id.grayscaleButton);
        Button sepiaButton = (Button) findViewById(R.id.sepiaButton);
        Button saveButton = (Button) findViewById(R.id.saveButton);

        Intent gotIntent = getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        normalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = imageBitmap;
                imageView.setImageBitmap(displayBitmap);
            }
        });

        grayscaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = applyGrayscale();
                imageView.setImageBitmap(displayBitmap);
            }
        });

        sepiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = applySepia();
                imageView.setImageBitmap(displayBitmap);
            }
        });

        saveButton.setOnClickListener((l) -> {

            String[] temp = imageURI.split("/");
            String path = "";
            for (int i = 0; i < temp.length - 1; i ++){
                path += "/" + temp[i];
            }


                try {
                    File tempFile = File.createTempFile("Copy_of_", ".png", new File(path));

                    FileOutputStream out = new FileOutputStream(tempFile.getAbsolutePath());
                    displayBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                    out.close();
                    Toast.makeText(this, "New file created: " + tempFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        );

        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");

//        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        imageBitmap = BitmapFactory.decodeFile(imageURI);
        displayBitmap = imageBitmap;

        imageView.setImageBitmap(displayBitmap);
    }

    private Bitmap applyGrayscale(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++){
            int pixel = pixels[i];
            int average =  (int) (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114) ;

            pixels[i] = Color.rgb(average, average, average);
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap applySepia(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++){
            int pixel = pixels[i];
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            double tr = 0.393 * red + 0.769 * green + 0.189 * blue;
            double tg = 0.349 * red + 0.686 * green + 0.168 * blue;
            double tb = 0.272 * red + 0.534 * green + 0.131 * blue;

            tr = tr >= 255 ? 255 : tr;
            tg = tg >= 255 ? 255 : tg;
            tb = tb >= 255 ? 255 : tb;

            pixels[i] = Color.rgb((int) tr, (int) tg, (int) tb);
        }


        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap rotateRight(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] newPixels = new int[width * height];

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++)
                newPixels[j * height + (height - i - 1)] = pixels[i * width + j];
        }

        return Bitmap.createBitmap(newPixels, height, width, cf);
    }

    private Bitmap rotateLeft(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] newPixels = new int[width * height];

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++)
//                newPixels[j * height + (height - i - 1)] = pixels[i * width + j];
                newPixels[j * height + i] = pixels[i * width + (width - j - 1)];
        }

        return Bitmap.createBitmap(newPixels, height, width, cf);
    }

    private Bitmap flipHorizontal(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width / 2; j++){
                int temp = pixels[i * width + j];
                pixels[i * width + j] = pixels[i * width + (width - j - 1)];
                pixels[i * width + (width - j - 1)] = temp;
            }
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap flipVertical(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];

        for (int i = 0; i < height / 2; i++){
            for (int j = 0; j < width; j++){
                int temp = pixels[i * width + j];
                pixels[i * width + j] = pixels[(height - i - 1) * width + j];
                pixels[(height - i - 1) * width + j] = temp;
            }
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    // TODO : Implement these methods
    private Bitmap blurImage(){

        return null;
    }

    private Bitmap sharpenImage(){

        return null;
    }

    private Bitmap adjustBrightness(){

        return null;
    }

    private Bitmap adjustContrast(){

        return null;
    }

}
