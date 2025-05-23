package com.example.galleryexample3.imageediting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class PaintView extends androidx.appcompat.widget.AppCompatImageView {

    private Paint paint;
    private Path path;
    private ArrayList<Path> paths;
    private ArrayList<Paint> paints;
    private ArrayList<Path> undo_paths;
    private ArrayList<Paint> undo_paints;
    private Bitmap bitmap, temp;
    private Canvas canvas;
    private Context context;
    private Rect src, dst;
    private String imageURI;
    private int nw;
    private int nh;
    private PorterDuffXfermode pdxf;

    public void setScale (int nw, int nh) {
        this.nw = nw;
        this.nh = nh;
        dst = new Rect(0, 0, nw, nh);
        invalidate();
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Intent gotIntent = ((Activity) context).getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        imageURI = gotBundle.getString("imageURI");
        temp = BitmapFactory.decodeFile(imageURI);
        src = new Rect(0, 0, temp.getWidth(), temp.getHeight());
        dst = new Rect(0, 0, temp.getWidth(), temp.getHeight());

//        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
//        this.getLayoutParams().width = temp.getWidth();
//        this.getLayoutParams().height = temp.getHeight();
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        path = new Path();
        paths = new ArrayList<>();
        paints = new ArrayList<>();

        undo_paths = new ArrayList<>();
        undo_paints = new ArrayList<>();
        pdxf = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(temp, null, dst,null);

        for (int i = 0; i < paths.size(); i++){
            if (paints.get(i).getColor() != Color.TRANSPARENT){
                canvas.drawPath(paths.get(i), paints.get(i));
            }
            else {
                Paint temp = paints.get(i);
                temp.setXfermode(pdxf);
                canvas.drawPath(paths.get(i), temp);
            }

        }

        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                paths.add(new Path(path));
                paints.add(new Paint(paint));
                undo_paths.clear();
                undo_paints.clear();
                path.reset();
                invalidate();
                break;
        }

        return true;
    }

    public void setBrushColor(int color){
        paint.setColor(color);
    }

    public int getBrushColor() {
        return paint.getColor();
    }

    public void setBrushSize(float size){
        paint.setStrokeWidth(size);
    }

    public float getBrushSize(){
        return paint.getStrokeWidth();
    }

    public void setEraser(boolean isEraser){
        if (isEraser) paint.setColor(Color.TRANSPARENT);
    }

    public void clearCanvas(){
        paths.clear();
        paints.clear();
        invalidate();
    }

    public Bitmap getBitmap(){
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public void undo(){
        if (paths.isEmpty()) return;

        Path temp_path = paths.remove(paths.size() - 1);
        Paint temp_paint = paints.remove(paints.size() - 1);

        undo_paths.add(temp_path);
        undo_paints.add(temp_paint);
        path.reset();
        invalidate();
    }

    public void redo(){
        Log.i("debug", getX() + " " + getY() + " " + getWidth() + " " + getHeight());
        Log.i("debug", getLayoutParams().width + " " + getLayoutParams().height);
        if (undo_paths.isEmpty()) return;

        Path temp_path = undo_paths.remove(undo_paths.size() - 1);
        Paint temp_paint = undo_paints.remove(undo_paints.size() - 1);

        paths.add(temp_path);
        paints.add(temp_paint);
        path.reset();
        invalidate();
    }

}
