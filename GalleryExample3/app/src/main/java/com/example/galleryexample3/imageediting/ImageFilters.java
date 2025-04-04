package com.example.galleryexample3.imageediting;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.galleryexample3.R;
import com.example.galleryexample3.SingleImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


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
        Button drawButton = (Button) findViewById(R.id.drawButton);

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
                displayBitmap = applyNegative();
                imageView.setImageBitmap(displayBitmap);
            }
        });

        sepiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBitmap = adjustTemperature();
                imageView.setImageBitmap(displayBitmap);
            }
        });

        saveButton.setOnClickListener((l) -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".png");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HeavensDoor");

            Uri result = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            if (result != null){
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(result, "w");
                    if (outputStream != null){
                        displayBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    Toast.makeText(this, "Can't save image.", Toast.LENGTH_SHORT).show();
                }
            }

//            String[] temp = imageURI.split("/");
//            String path = "";
//            for (int i = 0; i < temp.length - 1; i ++){
//                path += "/" + temp[i];
//            }
//
//
//                try {
//                    File tempFile = File.createTempFile("Copy_of_", ".png", new File(path));
//
//                    FileOutputStream out = new FileOutputStream(tempFile.getAbsolutePath());
//                    displayBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
//                    // PNG is a lossless format, the compression factor (100) is ignored
//                    out.close();
//                    Toast.makeText(this, "New file created: " + tempFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
            }
        );



        if (gotBundle == null)
            return;

        imageURI = gotBundle.getString("imageURI");

        drawButton.setOnClickListener((l) -> {
            Intent intent = new Intent(ImageFilters.this, PaintingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("imageURI", imageURI);
            intent.putExtras(bundle);
            startActivity(intent);
        });

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
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int kernel_size = 7;
        kernel_size = Math.max(Math.min(kernel_size, 31), 3);
        int[] new_pixels = new int[width * height];
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++) {
                double rSum = 0, gSum = 0, bSum = 0;
                for (int j = -kernel_size / 2; j <= kernel_size / 2; j++) {
                    for (int k = -kernel_size / 2; k <= kernel_size / 2; k++) {
                        int new_x = Math.min(Math.max(x + j, 0), width - 1);
                        int new_y = Math.min(Math.max(y + k, 0), height - 1);
                        int color = pixels[new_y * width + new_x];
                        rSum += Color.red(color);
                        gSum += Color.green(color);
                        bSum += Color.blue(color);
                    }
                }
                int r = (int) Math.min(255, Math.max(0, Math.round(rSum / (kernel_size * kernel_size))));
                int g = (int) Math.min(255, Math.max(0, Math.round(gSum / (kernel_size * kernel_size))));
                int b = (int) Math.min(255, Math.max(0, Math.round(bSum / (kernel_size * kernel_size))));
                new_pixels[y * width + x] = Color.rgb(r, g, b);
                //Log.i("debug", r + " " + g + " " + b);
            }
        }

        Log.i("debug", "blur done");
        return Bitmap.createBitmap(new_pixels, width, height, cf);
    }

    private Bitmap sharpenImage(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap blurredBitmap = blurImage();
        int[] blurred = new int[width * height];
        blurredBitmap.getPixels(blurred, 0, width, 0, 0, width, height);
        int sharpness = 1;
        for (int i = 0; i < width * height; i++){
            int r = Math.min(255, Math.max(0, (Color.red(pixels[i]) - Color.red(blurred[i])) * sharpness + Color.red(pixels[i])));
            int g = Math.min(255, Math.max(0, (Color.green(pixels[i]) - Color.green(blurred[i])) * sharpness + Color.green(pixels[i])));
            int b = Math.min(255, Math.max(0, (Color.blue(pixels[i]) - Color.blue(blurred[i])) * sharpness + Color.blue(pixels[i])));
            pixels[i] = Color.rgb(r, g, b);
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap adjustBrightness(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        double brightness = 1.5;
        for (int i = 0; i < width * height; i++){
            int r = (int) Math.min(255, Math.max(0, (Color.red(pixels[i]) * brightness)));
            int g = (int) Math.min(255, Math.max(0, (Color.green(pixels[i]) * brightness)));
            int b = (int) Math.min(255, Math.max(0, (Color.blue(pixels[i]) * brightness)));
            pixels[i] = Color.rgb(r, g, b);
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap adjustContrast(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        double contrast = 1.5;
        for (int i = 0; i < width * height; i++){
            int r = (int) Math.min(255, Math.max(0, ((Color.red(pixels[i]) - 128) * contrast + 128)));
            int g = (int) Math.min(255, Math.max(0, ((Color.green(pixels[i]) - 128) * contrast + 128)));
            int b = (int) Math.min(255, Math.max(0, ((Color.blue(pixels[i]) - 128) * contrast + 128)));
            pixels[i] = Color.rgb(r, g, b);
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap applyNegative(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width * height; i++){
            int r = 255 - Color.red(pixels[i]);
            int g = 255 - Color.green(pixels[i]);
            int b = 255 - Color.blue(pixels[i]);
            pixels[i] = Color.rgb(r, g, b);
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

    private Bitmap adjustTemperature(){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int value = -15;
        for (int i = 0; i < width * height; i++){
            int r = Math.min(255, Math.max(0, Color.red(pixels[i]) + value));
            int g = Color.green(pixels[i]);
            int b = Math.min(255, Math.max(0, Color.blue(pixels[i]) - value));
            pixels[i] = Color.rgb(r, g, b);
        }

        return Bitmap.createBitmap(pixels, width, height, cf);
    }

}
