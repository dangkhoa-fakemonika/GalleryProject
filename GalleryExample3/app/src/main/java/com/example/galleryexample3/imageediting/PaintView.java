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
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class PaintView extends View {

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

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Intent gotIntent = ((Activity) context).getIntent();
        Bundle gotBundle = gotIntent.getExtras();

        imageURI = gotBundle.getString("imageURI");
        temp = BitmapFactory.decodeFile(imageURI);
        src = new Rect(0, 0, temp.getWidth(), temp.getHeight()); dst = new Rect(0, 0, temp.getWidth(), temp.getHeight());

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
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(temp, null, dst,null);

        for (int i = 0; i < paths.size(); i++){
            canvas.drawPath(paths.get(i), paints.get(i));
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

    public void setBrushSize(float size){
        paint.setStrokeWidth(size);
    }

    public void setEraser(boolean isEraser){
        paint.setColor(isEraser ? Color.WHITE : Color.BLACK);
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
        if (undo_paths.isEmpty()) return;

        Path temp_path = undo_paths.remove(undo_paths.size() - 1);
        Paint temp_paint = undo_paints.remove(undo_paints.size() - 1);

        paths.add(temp_path);
        paints.add(temp_paint);
        path.reset();
        invalidate();
    }

}
