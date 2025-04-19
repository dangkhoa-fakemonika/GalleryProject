package com.example.galleryexample3.imageediting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.example.galleryexample3.businessclasses.ClipBoardProcessing;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.Objects;

public class BarCodeScannerClass {
    static BarcodeScannerOptions options;

    public static void getBarCodeData(Context context, String uri){
        if (options == null)
            options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE,Barcode.FORMAT_AZTEC).build();

        BarcodeScanner scanner = BarcodeScanning.getClient();

        InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(uri), 0);
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        for (Barcode barcode: barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            int valueType = barcode.getValueType();
                            String temp = barcode.getRawValue();
                            ClipBoardProcessing.getTextToClipBoard(context, temp);

                            switch (valueType) {
                                case Barcode.TYPE_WIFI:
                                    String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();
                                    ClipBoardProcessing.getTextToClipBoard(context, ssid + "\n" + password);
                                    break;
                                case Barcode.TYPE_URL:
                                    String title = Objects.requireNonNull(barcode.getUrl()).getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    ClipBoardProcessing.getTextToClipBoard(context, url);
                                    break;
                                default:
                                    String text = barcode.getRawValue();
                                    ClipBoardProcessing.getTextToClipBoard(context, text);
                                    break;

                            }
                        }
                    }
                });
    }
}
