package com.example.galleryexample3.businessclasses;


import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

public class ClipBoardProcessing {
    public static void getTextToClipBoard(Context context, String text){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("generated text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "Saved to clipboard!", Toast.LENGTH_SHORT).show();
    }
}
