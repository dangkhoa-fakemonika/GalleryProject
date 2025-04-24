package com.example.galleryexample3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.chaos.view.PinView;
import com.example.galleryexample3.businessclasses.PinUtils;

import java.util.concurrent.Executor;

public class PrivateVaultCodeSettings extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    boolean settingMode;
    boolean useFingerprint;
    Toolbar myToolBar;
    PinView pinView;
    PinView confirmPinView;
    Button startVaultButton;
    String pin;
    SwitchCompat mySwitch;
    LinearLayout fingerPrintOption;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_screen_pin_setting_view);
        myToolBar = findViewById(R.id.myToolBar);
        pinView = findViewById(R.id.firstPinView);
        confirmPinView = findViewById(R.id.secondPinView);
        pinView.setAnimationEnable(true);
        confirmPinView.setAnimationEnable(true);
        startVaultButton = findViewById(R.id.set_pin);
        mySwitch = findViewById(R.id.fingerprintSwitch);
        fingerPrintOption = findViewById(R.id.fingerPrintOptions);

        Intent intent = getIntent();
        settingMode = intent.getBooleanExtra("settingMode", false);
        mySwitch.setChecked(settingMode);
        if (!settingMode){
            fingerPrintOption.setVisibility(LinearLayout.GONE);
        }
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    executor = ContextCompat.getMainExecutor(PrivateVaultCodeSettings.this);
                    biometricPrompt = new BiometricPrompt(PrivateVaultCodeSettings.this,
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
                            Toast.makeText(PrivateVaultCodeSettings.this, "Turn on Fingerprint Credential", Toast.LENGTH_SHORT).show();
                            mySwitch.setChecked(true);

                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(PrivateVaultCodeSettings.this, "Cannot get your fingerprint", Toast.LENGTH_SHORT).show();
                            mySwitch.setChecked(false);
                        }
                    });

                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Please confirm your fingerprint")
                            .setSubtitle("By confirming your finger print, you turn on this feature")
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                            .setNegativeButtonText("User App Pin")
                            .build();
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolBar.setTitle("Setup Private Album");
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("cancel", "user cancel");
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
        myToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        startVaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pinView.getText() == null){
                    return;
                }
                String pinCode = pinView.getText().toString();
                if (confirmPinView.getText() == null){
                    Toast.makeText(PrivateVaultCodeSettings.this, "Please confirm your PIN Code", Toast.LENGTH_SHORT).show();
                    return;
                }
                String confirmPinCode = confirmPinView.getText().toString();
                if (!pinCode.equals(confirmPinCode)){
                    Toast.makeText(PrivateVaultCodeSettings.this, "Confirmed PIN not match!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {

                    PinUtils.savePinWithEncoded(PrivateVaultCodeSettings.this,  pinView.getText().toString());
                    Toast.makeText(PrivateVaultCodeSettings.this, "Your settings is updated", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("pin_code", pinView.getText().toString());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } catch (Exception e) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Exception", e.getMessage());
                    setResult(RESULT_CANCELED, resultIntent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("heaven_use_fingerprint", mySwitch.isChecked());
        editor.apply();
        editor.commit();
    }
}
