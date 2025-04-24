package com.example.galleryexample3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chaos.view.PinView;
import com.example.galleryexample3.businessclasses.PinUtils;

import java.util.concurrent.Executor;

public class PrivateVaultLockScreen extends AppCompatActivity {
    boolean userFingerprint;
    private Executor executor;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Button goToPrivateVault;
    private Button biometricLoginButton;
    private PinView myPinView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_screen_pin_view);

        biometricLoginButton = findViewById(R.id.biometric_button);
        myPinView = findViewById(R.id.firstPinView);
        myPinView.setAnimationEnable(true);
        SharedPreferences myPrefs = this.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        userFingerprint = myPrefs.getBoolean("heaven_use_fingerprint", false);
        if (userFingerprint){
            executor = ContextCompat.getMainExecutor(this);
            BiometricManager biometricManager = BiometricManager.from(this);

            biometricPrompt = new BiometricPrompt(PrivateVaultLockScreen.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(),
                                    "Authentication error: " + errString, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(PrivateVaultLockScreen.this, "Nice to see you again", Toast.LENGTH_SHORT).show();
                    Intent myIntent =  new Intent();
                    setResult(RESULT_OK, myIntent);
                    finish();

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(PrivateVaultLockScreen.this, "See you soon", Toast.LENGTH_SHORT).show();
                    Intent myIntent =  new Intent();
                    setResult(RESULT_CANCELED, myIntent);
                    finish();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for my app")
                    .setSubtitle("Log in using your biometric credential")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
            biometricLoginButton.setOnClickListener(view -> {
                biometricPrompt.authenticate(promptInfo);
            });
            biometricLoginButton.setVisibility(Button.VISIBLE);
        }else{
            biometricLoginButton.setVisibility(Button.GONE);
        }

        goToPrivateVault = findViewById(R.id.open_private_vault);
        goToPrivateVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myPinView.getText() == null){
                    Toast.makeText(PrivateVaultLockScreen.this, "Please fill in your PIN code", Toast.LENGTH_SHORT).show();
                }else{
                    String userPin = myPinView.getText().toString();
                    try {
                        if(PinUtils.validatePinCode(PrivateVaultLockScreen.this, userPin)){
                            Toast.makeText(PrivateVaultLockScreen.this, "Nice to see you again", Toast.LENGTH_SHORT).show();
                            Intent myIntent =  new Intent();
                            setResult(RESULT_OK, myIntent);
                            finish();
                        }else{
                            Toast.makeText(PrivateVaultLockScreen.this, "Wrong PIN, please try again", Toast.LENGTH_SHORT).show();

                        }
                    } catch (Exception e) {
                        Toast.makeText(PrivateVaultLockScreen.this, "Something bad happen", Toast.LENGTH_SHORT).show();
                        Log.e("PrivateVault", e.toString());
                    }
                }
            }
        });
    }
}
