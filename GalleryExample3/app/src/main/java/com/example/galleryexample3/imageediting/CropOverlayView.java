package com.example.galleryexample3.imageediting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class CropOverlayView extends View {
    private static final int HANDLE_RADIUS = 30;
    private static final int MIN_CROP_SIZE = 200;

    private enum HandleType {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, NONE
    }

    private HandleType activeHandle = HandleType.NONE;
    private Paint outsidePaint, borderPaint, handlePaint;
    private RectF boundRect, cropRect;

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8f);

        handlePaint = new Paint();
        handlePaint.setColor(Color.TRANSPARENT);
        handlePaint.setStyle(Paint.Style.FILL);

        outsidePaint = new Paint();
        outsidePaint.setColor(Color.parseColor("#A0000000"));
    }

    public void setBound(ImageView imageView, float rotation) {
        Drawable imageAsDrawable = imageView.getDrawable();
        if (imageAsDrawable == null) return;

        Matrix imageMatrix = imageView.getImageMatrix();
        RectF bound = new RectF(0, 0, imageAsDrawable.getIntrinsicWidth(), imageAsDrawable.getIntrinsicHeight());
        imageMatrix.mapRect(bound);

        rotation = (rotation % 360 + 360) % 360;
        if (rotation == 90f || rotation == 270f) {
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.setRotate(rotation, imageView.getWidth() / 2f, imageView.getHeight() / 2f);
            rotationMatrix.mapRect(bound);
        }

        cropRect = new RectF(bound);
        boundRect = new RectF(bound);

//        Log.d("debug", "Rect - left: " + cropRect.left +
//        ", top: " + cropRect.top +
//        ", right: " + cropRect.right +
//        ", bottom: " + cropRect.bottom +
//        ", width: " + cropRect.width() +
//        ", height: " + cropRect.height());

        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (cropRect == null || cropRect.width() == 0 || cropRect.height() == 0)
            return;

        canvas.drawRect(cropRect, borderPaint);

        canvas.clipRect(boundRect);
        canvas.clipOutRect(cropRect.left - 4f, cropRect.top - 4f, cropRect.right + 4f, cropRect.bottom + 4f);
        canvas.drawColor(Color.parseColor("#B0000000"));

        canvas.drawCircle(cropRect.left, cropRect.top, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.top, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, HANDLE_RADIUS, handlePaint);
    }

    public RectF getCropRect() {
        return cropRect;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = getTouchedHandle(x, y);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (activeHandle != HandleType.NONE) {
                    resizeCropRect((int) x,(int) y);
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeHandle = HandleType.NONE;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private HandleType getTouchedHandle(float x, float y) {
        if (isInside(x, y, cropRect.left, cropRect.top)) return HandleType.TOP_LEFT;
        if (isInside(x, y, cropRect.right, cropRect.top)) return HandleType.TOP_RIGHT;
        if (isInside(x, y, cropRect.left, cropRect.bottom)) return HandleType.BOTTOM_LEFT;
        if (isInside(x, y, cropRect.right, cropRect.bottom)) return HandleType.BOTTOM_RIGHT;
        return HandleType.NONE;
    }

    private boolean isInside(float touchX, float touchY, float handleX, float handleY) {
        return Math.hypot(touchX - handleX, touchY - handleY) <= HANDLE_RADIUS * 1.5;
    }

    private void resizeCropRect(int x, int y) {
        switch (activeHandle) {
            case TOP_LEFT:
                cropRect.left = Math.min(x, cropRect.right - MIN_CROP_SIZE);
                cropRect.top = Math.min(y, cropRect.bottom - MIN_CROP_SIZE);
                break;
            case TOP_RIGHT:
                cropRect.right = Math.max(x, cropRect.left + MIN_CROP_SIZE);
                cropRect.top = Math.min(y, cropRect.bottom - MIN_CROP_SIZE);
                break;
            case BOTTOM_LEFT:
                cropRect.left = Math.min(x, cropRect.right - MIN_CROP_SIZE);
                cropRect.bottom = Math.max(y, cropRect.top + MIN_CROP_SIZE);
                break;
            case BOTTOM_RIGHT:
                cropRect.right = Math.max(x, cropRect.left + MIN_CROP_SIZE);
                cropRect.bottom = Math.max(y, cropRect.top + MIN_CROP_SIZE);
                break;
        }

        constrainRect();
    }

    private void constrainRect() {
        float left = boundRect.left;
        float top = boundRect.top;
        float width = boundRect.width();
        float height = boundRect.height();

        cropRect.left = Math.max(left, cropRect.left);
        cropRect.top = Math.max(top, cropRect.top);
        cropRect.right = Math.min(left + width, cropRect.right);
        cropRect.bottom = Math.min(top + height, cropRect.bottom);
    }
}