package com.example.galleryexample3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.galleryexample3.businessclasses.ImageFiltersProcessing;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    AppCompatActivity current;
    ImageCapture imageCapture;
    int cameraSelectorFacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view);
        cameraSelectorFacing = CameraSelector.LENS_FACING_BACK;

        CardView takeImageButton = (CardView) findViewById(R.id.takePicture);
        ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
        previewImage.setImageResource(R.drawable.uoh);
        ImageButton switchCamera = (ImageButton) findViewById(R.id.switchCamera);
        current = this;

        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{ Manifest.permission.CAMERA, Manifest.permission.CAMERA}, 101);
        }

        previewView = findViewById(R.id.previewView);

        imageCapture = new ImageCapture.Builder().build();

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
                    Bitmap result = image.toBitmap();
                    previewImage.setImageBitmap(result);
                    ImageGalleryProcessing.saveImage(current, result);
                }
            });
        });

        switchCamera.setOnClickListener((l) -> {
            if (cameraSelectorFacing == CameraSelector.LENS_FACING_BACK)
                cameraSelectorFacing = CameraSelector.LENS_FACING_FRONT;
            else if (cameraSelectorFacing == CameraSelector.LENS_FACING_FRONT)
                cameraSelectorFacing = CameraSelector.LENS_FACING_BACK;

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
        });

    }

    void bindPreview(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder().build();
        cameraProvider.unbind();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraSelectorFacing).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//        imageCapture.setTargetRotation(previewView.getDisplay().getRotation());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, preview);
    }
}
