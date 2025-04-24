package com.example.galleryexample3.businessclasses;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import com.example.galleryexample3.dataclasses.EditParams;
import com.example.galleryexample3.dataclasses.FilterPreview;

import java.util.ArrayList;
import java.util.Stack;

public class ImageEditingController {
    private Stack<EditParams> undoStack = new Stack<>();
    private Stack<EditParams> redoStack = new Stack<>();
    private EditParams currentParams;
    private Bitmap processed = null;
    private int last_radius;

    public ImageEditingController(EditParams initialParams) {
        currentParams = initialParams.copy();
        last_radius = currentParams.radius;
    }

    public EditParams getCurrentParams() {
        return currentParams.copy();
    }

    public void addNewParams(EditParams newParams) {
        undoStack.push(currentParams.copy());
        redoStack.clear();
        currentParams = newParams.copy();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public EditParams undo() {
        if (!canUndo()) return currentParams.copy();
        last_radius = currentParams.radius;
        redoStack.push(currentParams.copy());
        currentParams = undoStack.pop();
        return currentParams.copy();
    }

    public EditParams redo() {
        if (!canRedo()) return currentParams.copy();
        last_radius = currentParams.radius;
        undoStack.push(currentParams.copy());
        currentParams = redoStack.pop();
        return currentParams.copy();
    }

    public EditParams reset() {
        if (!canUndo()) return currentParams.copy();
        redoStack.clear();
        undoStack.clear();
        processed = null;
        currentParams = new EditParams(0, 1, 1, "normal", 0);
        return currentParams.copy();
    }

    public Bitmap applyEdit(Bitmap source) {
        ColorMatrix resultMatrix = new ColorMatrix();

        boolean isModified = false;

        if (currentParams.radius != last_radius && currentParams.radius != 0) {
            int radius = currentParams.radius;

            if (radius < 0)
                processed = applyGaussianBlur(source, -radius);
            else
                processed = applyUnsharpMask(source, radius);

            last_radius = currentParams.radius;
        } else if (processed == null)
            processed = source.copy(source.getConfig() != null
                    ? source.getConfig()
                    : Bitmap.Config.ARGB_8888,
                    true
            );

        if (currentParams.brightness != 100f) {
            float b = currentParams.brightness;
            ColorMatrix brightnessMatrix = new ColorMatrix(new float[]{
                    1, 0, 0, 0, b,
                    0, 1, 0, 0, b,
                    0, 0, 1, 0, b,
                    0, 0, 0, 1, 0
            });
            resultMatrix.postConcat(brightnessMatrix);
            isModified = true;
        }

        if (currentParams.contrast != 1f) {
            float scale = currentParams.contrast;
            float translate = (-0.5f * scale + 0.5f) * 255f;
            ColorMatrix contrastMatrix = new ColorMatrix(new float[]{
                    scale, 0,     0,     0, translate,
                    0,     scale, 0,     0, translate,
                    0,     0,     scale, 0, translate,
                    0,     0,     0,     1, 0
            });
            resultMatrix.postConcat(contrastMatrix);
            isModified = true;
        }

        if (currentParams.saturation != 1f) {
            float s = currentParams.saturation;
            float R = 0.213f, G = 0.715f, B = 0.072f;
            ColorMatrix saturationMatrix = new ColorMatrix(new float[] {
                    R + (1 - R) * s, G - G * s,       B - B * s,       0, 0,
                    R - R * s,       G + (1 - G) * s, B - B * s,       0, 0,
                    R - R * s,       G - G * s,       B + (1 - B) * s, 0, 0,
                    0,               0,               0,               1, 0
            });
            resultMatrix.postConcat(saturationMatrix);
            isModified = true;
        }

        if (!currentParams.filter.equals("normal")) {
            resultMatrix.postConcat(getFilterMatrix(currentParams.filter));
            isModified = true;
        }

        if (!isModified) {
            return processed;
        }

        Bitmap modified = Bitmap.createBitmap(
                processed.getWidth(),
                processed.getHeight(),
                processed.getConfig() != null
                        ? processed.getConfig()
                        : Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(modified);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(resultMatrix));
        canvas.drawBitmap(processed, 0, 0, paint);

        return modified;
    }

    private static ColorMatrix getFilterMatrix(String filterName) {
        switch (filterName.toLowerCase()) {
            case "bright":
                return new ColorMatrix(new float[]{
                        1, 0, 0, 0, 30,
                        0, 1, 0, 0, 30,
                        0, 0, 1, 0, 30,
                        0, 0, 0, 1, 0
                });

            case "warm bright":
                return new ColorMatrix(new float[]{
                        1.2f, 0,    0,    0, 20,
                        0,    0.9f, 0,    0, 10,
                        0,    0,    0.8f, 0, 10,
                        0,    0,    0,    1, 0
                });

            case "cool bright":
                return new ColorMatrix(new float[]{
                        0.8f, 0,    0,    0, 10,
                        0,    0.9f, 0,    0, 10,
                        0,    0,    1.2f, 0, 20,
                        0,    0,    0,    1, 0
                });

            case "warm dark":
                return new ColorMatrix(new float[]{
                        1.2f, 0,    0,    0, -10,
                        0,    0.9f, 0,    0, -5,
                        0,    0,    0.8f, 0, -20,
                        0,    0,    0,    1,  0
                });

            case "cool dark":
                return new ColorMatrix(new float[]{
                        0.8f, 0,    0,    0, -20,
                        0,    0.9f, 0,    0, -5,
                        0,    0,    1.2f, 0, -10,
                        0,    0,    0,    1,  0
                });

            case "grayscale":
                return new ColorMatrix(new float[]{
                        0.213f, 0.715f, 0.072f, 0, 0,
                        0.213f, 0.715f, 0.072f, 0, 0,
                        0.213f, 0.715f, 0.072f, 0, 0,
                        0,      0,      0,      1, 0
                });

            case "sepia":
                return new ColorMatrix(new float[]{
                        0.213f, 0.715f, 0.072f, 0,  40,
                        0.213f, 0.715f, 0.072f, 0,  20,
                        0.213f, 0.715f, 0.072f, 0, -30,
                        0,      0,      0,      1,  0
                });

            case "cyanotype":
                return new ColorMatrix(new float[]{
                        0.213f, 0.715f, 0.072f, 0, -60,
                        0.213f, 0.715f, 0.072f, 0, -30,
                        0.213f, 0.715f, 0.072f, 0,  40,
                        0,      0,      0,      1,  0
                });

            default:
                return null;
        }
    }

    private void logColorMatrix(ColorMatrix matrix) {
        float[] values = matrix.getArray();
        StringBuilder sb = new StringBuilder("ColorMatrix:\n");

        for (int i = 0; i < 4; i++) {
            sb.append("[ ");
            for (int j = 0; j < 5; j++) {
                sb.append(String.format("%6.2f ", values[i * 5 + j]));
            }
            sb.append("]\n");
        }

        Log.d("ColorMatrixDebug", sb.toString());
    }

    public ArrayList<FilterPreview> generateFilterPreviews(Bitmap sourceBitmap) {
        Bitmap adjustedBitmap = applyEdit(sourceBitmap);
        ArrayList<FilterPreview> previewList = new ArrayList<>();

        int targetSize = 200;
        int sourceWidth = adjustedBitmap.getWidth();
        int sourceHeight = adjustedBitmap.getHeight();
        float ratio = Math.min(
                (float) targetSize / sourceWidth,
                (float) targetSize / sourceHeight
        );
        int targetWidth = Math.round(ratio * sourceWidth);
        int targetHeight = Math.round(ratio * sourceHeight);

        Bitmap previewSource = Bitmap.createScaledBitmap(
                adjustedBitmap,
                targetWidth,
                targetHeight,
                true
        );
        previewList.add(new FilterPreview("Normal", previewSource));

        String[] filterNames = new String[]{
                "Bright",
                "Warm Bright",
                "Cool Bright",
                "Warm Dark",
                "Cool Dark",
                "Grayscale",
                "Sepia",
                "Cyanotype"
        };

        for (String filterName : filterNames) {
            ColorMatrix filterMatrix = getFilterMatrix(filterName);
            Bitmap filtered = applyColorMatrix(previewSource, filterMatrix);
            previewList.add(new FilterPreview(filterName, filtered));
        }

        return previewList;
    }

    private Bitmap applyColorMatrix(Bitmap sourceBitmap, ColorMatrix matrix) {
        Bitmap output = Bitmap.createBitmap(
                sourceBitmap.getWidth(),
                sourceBitmap.getHeight(),
                sourceBitmap.getConfig() != null
                        ? sourceBitmap.getConfig()
                        : Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(sourceBitmap, 0, 0, paint);
        return output;
    }

    public static Bitmap applyGaussianBlur(Bitmap sourceBitmap, int radius) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        Bitmap blurred = Bitmap.createBitmap(
                width,
                height,
                sourceBitmap.getConfig() != null
                        ? sourceBitmap.getConfig()
                        : Bitmap.Config.ARGB_8888
        );

        float[] kernel = generateGaussianKernel(radius * 2 + 1);

        int[] pixels = new int[width * height];
        sourceBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] temp = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, sum = 0;
                for (int i = -radius; i <= radius; i++) {
                    int xIndex = x + i;
                    if (xIndex < 0) xIndex = -xIndex;
                    if (xIndex >= width) xIndex = 2 * width - xIndex - 1;
                    int color = pixels[y * width + xIndex];
                    float weight = kernel[i + radius];

                    r += ((color >> 16) & 0xFF) * weight;
                    g += ((color >> 8) & 0xFF) * weight;
                    b += (color & 0xFF) * weight;
                    sum += weight;
                }

                int ir = (int)(r / sum);
                int ig = (int)(g / sum);
                int ib = (int)(b / sum);
                temp[y * width + x] = (0xFF << 24) | (ir << 16) | (ig << 8) | ib;
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float r = 0, g = 0, b = 0, sum = 0;
                for (int i = -radius; i <= radius; i++) {
                    int yIndex = y + i;
                    if (yIndex < 0) yIndex = -yIndex;
                    if (yIndex >= height) yIndex = 2 * height - yIndex - 1;
                    int color = temp[yIndex * width + x];
                    float weight = kernel[i + radius];

                    r += ((color >> 16) & 0xFF) * weight;
                    g += ((color >> 8) & 0xFF) * weight;
                    b += (color & 0xFF) * weight;
                    sum += weight;
                }

                int ir = (int) (r / sum);
                int ig = (int) (g / sum);
                int ib = (int) (b / sum);
                blurred.setPixel(x, y, (0xFF << 24) | (ir << 16) | (ig << 8) | ib);
            }
        }

        Log.i("debug", "blur normal " + blurred.getPixel(0, 0) + " " + pixels[0]);

        return blurred;
    }

    public static Bitmap applyUnsharpMask(Bitmap sourceBitmap, int radius) {
        Log.d("debug", "sharp");

        Bitmap blurred = applyGaussianBlur(sourceBitmap, radius);
        Bitmap sharpened = Bitmap.createBitmap(
                sourceBitmap.getWidth(),
                sourceBitmap.getHeight(),
                sourceBitmap.getConfig() != null
                        ? sourceBitmap.getConfig()
                        : Bitmap.Config.ARGB_8888
        );

        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int origColor = sourceBitmap.getPixel(x, y);
                int blurColor = blurred.getPixel(x, y);

                int rOrig = (origColor >> 16) & 0xFF;
                int gOrig = (origColor >> 8) & 0xFF;
                int bOrig = origColor & 0xFF;

                int rBlur = (blurColor >> 16) & 0xFF;
                int gBlur = (blurColor >> 8) & 0xFF;
                int bBlur = blurColor & 0xFF;

                int r = clampColor(rOrig + (int)(rOrig - rBlur));
                int g = clampColor(gOrig + (int)(gOrig - gBlur));
                int b = clampColor(bOrig + (int)(bOrig - bBlur));

                int sharpColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
                sharpened.setPixel(x, y, sharpColor);
            }
        }

        Log.i("debug", "blur sharp " + blurred.getPixel(0, 0) + " " + sharpened.getPixel(0, 0));


        return sharpened;
    }

    private static float[] generateGaussianKernel(int size) {
        float[] kernel = new float[size];
        double sigma = size / 1.5;
        float sum = 0;
        int mid = size / 2;
        for (int i = 0; i < size; i++) {
            kernel[i] = (float) Math.exp(-((i - mid) * (i - mid)) / (2 * sigma * sigma));
            sum += kernel[i];
        }

        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }
        return kernel;
    }

    private static int clampColor(int val) {
        return Math.max(0, Math.min(255, val));
    }
}