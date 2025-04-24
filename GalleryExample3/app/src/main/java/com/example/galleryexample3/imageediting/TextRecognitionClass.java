package com.example.galleryexample3.imageediting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
    
import androidx.annotation.NonNull;

import com.example.galleryexample3.businessclasses.ClipBoardProcessing;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
@Deprecated
public class TextRecognitionClass {
    static TextRecognizer recognizer;

    public static void getTextFromImage(Context context, String uri){
        if (recognizer == null)
            recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(uri), 0);

                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully

                                String resultText = visionText.getText();
//                                for (Text.TextBlock block : visionText.getTextBlocks()) {
//                                    String blockText = block.getText();
//                                    Point[] blockCornerPoints = block.getCornerPoints();
//                                    Rect blockFrame = block.getBoundingBox();
//                                    for (Text.Line line : block.getLines()) {
//                                        String lineText = line.getText();
//                                        Point[] lineCornerPoints = line.getCornerPoints();
//                                        Rect lineFrame = line.getBoundingBox();
//                                        for (Text.Element element : line.getElements()) {
//                                            String elementText = element.getText();
//                                            Point[] elementCornerPoints = element.getCornerPoints();
//                                            Rect elementFrame = element.getBoundingBox();
//                                            for (Text.Symbol symbol : element.getSymbols()) {
//                                                String symbolText = symbol.getText();
//                                                Point[] symbolCornerPoints = symbol.getCornerPoints();
//                                                Rect symbolFrame = symbol.getBoundingBox();
//                                            }
//                                        }
//                                    }
//                                }


//                                Log.i("SCANNED TEXT", resultText);
                                ClipBoardProcessing.getTextToClipBoard(context, resultText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(context,"Words scanned failed", Toast.LENGTH_SHORT).show();
                                    }
                                });

    }

}
