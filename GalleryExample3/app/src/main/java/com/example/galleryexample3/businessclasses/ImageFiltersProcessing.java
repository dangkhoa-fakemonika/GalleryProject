package com.example.galleryexample3.businessclasses;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class ImageFiltersProcessing {
    public static Bitmap applyGrayscale(Bitmap imageBitmap){
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

    public static Bitmap applySepia(Bitmap imageBitmap){
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

    public static Bitmap rotateRight(Bitmap imageBitmap){
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

    public static Bitmap rotateLeft(Bitmap imageBitmap){
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

    public static Bitmap flipHorizontal(Bitmap imageBitmap){
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

    public static Bitmap flipVertical(Bitmap imageBitmap){
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
    public static Bitmap blurImage(Bitmap imageBitmap){
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

    public static Bitmap sharpenImage(Bitmap imageBitmap){
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        Bitmap.Config cf = imageBitmap.getConfig();

        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap blurredBitmap = blurImage(imageBitmap);
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

    public static Bitmap adjustBrightness(Bitmap imageBitmap){
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

    public static Bitmap adjustContrast(Bitmap imageBitmap){
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

    public static Bitmap applyNegative(Bitmap imageBitmap){
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

    public static Bitmap adjustTemperature(Bitmap imageBitmap){
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
