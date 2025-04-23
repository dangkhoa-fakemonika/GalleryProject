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

    public ImageEditingController(EditParams initialParams) {
        currentParams = initialParams.copy();
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
        redoStack.push(currentParams.copy());
        currentParams = undoStack.pop();
        return currentParams.copy();
    }

    public EditParams redo() {
        if (!canRedo()) return currentParams.copy();
        undoStack.push(currentParams.copy());
        currentParams = redoStack.pop();
        return currentParams.copy();
    }

    public Bitmap applyEdit(Bitmap original) {
        ColorMatrix resultMatrix = new ColorMatrix();

        boolean modified = false;

        if (currentParams.brightness != 100f) {
            float b = currentParams.brightness;
            ColorMatrix brightnessMatrix = new ColorMatrix(new float[]{
                    1, 0, 0, 0, b,
                    0, 1, 0, 0, b,
                    0, 0, 1, 0, b,
                    0, 0, 0, 1, 0
            });
            resultMatrix.postConcat(brightnessMatrix);
            modified = true;
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
            modified = true;
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
            modified = true;
        }

        if (!currentParams.filter.equals("normal")) {
            resultMatrix.postConcat(getFilterMatrix(currentParams.filter));
            modified = true;
        }

        if (!modified) {
            Log.i("debug", "real");
            return original.copy(original.getConfig(), true);
        }

        Bitmap output = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(resultMatrix));
        canvas.drawBitmap(original, 0, 0, paint);

        return output;
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

    public ArrayList<FilterPreview> generateFilterPreviews(Bitmap originalBitmap) {
        Bitmap adjustedBitmap = applyEdit(originalBitmap);
        ArrayList<FilterPreview> previewList = new ArrayList<>();

        int targetSize = 200;
        int originalWidth = adjustedBitmap.getWidth();
        int originalHeight = adjustedBitmap.getHeight();
        float ratio = Math.min(
                (float) targetSize / originalWidth,
                (float) targetSize / originalHeight
        );
        int targetWidth = Math.round(ratio * originalWidth);
        int targetHeight = Math.round(ratio * originalHeight);

        Bitmap previewSource = Bitmap.createScaledBitmap(adjustedBitmap, targetWidth, targetHeight, true);
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

    private Bitmap applyColorMatrix(Bitmap source, ColorMatrix matrix) {
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(source, 0, 0, paint);
        return output;
    }
}