package com.example.galleryexample3;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    AppCompatActivity current;
    ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view);
        Button takeImageButton = (Button) findViewById(R.id.takeImage);
        ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
        previewImage.setImageResource(R.drawable.uoh);
        current = this;

        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{ Manifest.permission.CAMERA, Manifest.permission.CAMERA}, 101);
        }

        previewView = findViewById(R.id.previewView);

        imageCapture = new ImageCapture.Builder().setTargetRotation(Surface.ROTATION_0).build();

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        takeImageButton.setOnClickListener((l)->{
//            ImageCapture.OutputFileOptions outputFileOptions =
//                    new ImageCapture.OutputFileOptions.Builder(new File(...)).build();
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback(){
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    super.onCaptureSuccess(image);

                    Toast.makeText(current, "Picture taken!", Toast.LENGTH_SHORT).show();
                    Log.i("IMAGE", "GOT");
                    Bitmap result = image.toBitmap();
                    previewImage.setImageBitmap(result);
                }

//                @Override
//                public void onPostviewBitmapAvailable(@NonNull Bitmap bitmap) {
//                    super.onPostviewBitmapAvailable(bitmap);
//
//                    previewImage.setImageBitmap(bitmap);
//                }
            });
        });

    }

    void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Setting camera here
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, preview);
    }
}
