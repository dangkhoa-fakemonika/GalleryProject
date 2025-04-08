package com.example.galleryexample3.imageediting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class RectangleView extends View {
    private Paint paint;

    public RectangleView(Context context) {
        super(context);
        init();
    }

    public RectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RectangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    public void setRect (float left, float top, float right, float bottom) {
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = (int)(right - left);
        params.height = (int)(bottom - top);
        setLayoutParams(params);
        setX(left);
        setY(top);
        layout(getLeft(), getTop(), getLeft() + (int)(right - left), getTop() + (int)(bottom - top));
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    @Override
    public boolean performClick() {
        super.performClick();

        return true;
    }
}