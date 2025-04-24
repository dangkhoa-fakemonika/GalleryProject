package com.example.galleryexample3.businessclasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PinUtils {
    private final static String ALGORITHM_NAME = KeyProperties.KEY_ALGORITHM_RSA;
    public final static  String PREF_NAME = "appSettings";
    public final static  String PREF_KEY = "heaven_key";
    public final static  String PREF_LOCKER = "heaven_locker";
    public final static  String ALIAS_NAME = "heaven_gallery";
    public final static int KEY_SIZE = 2048;
    public void generateKeySet(Context context) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_NAME);
        keyPairGenerator.initialize(genKeyGenParameterSpec());
        keyPairGenerator.generateKeyPair();

    }
    public KeyGenParameterSpec genKeyGenParameterSpec(){
        return new KeyGenParameterSpec.Builder(ALIAS_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4))
                .setUserAuthenticationRequired(false)
                .build();
    }

    public PublicKey getPublicKey() throws Exception {
        KeyStore galleryKeyStore = KeyStore.getInstance("AndroidKeyStore");
        galleryKeyStore.load(null);
        if(!galleryKeyStore.containsAlias(ALIAS_NAME)){
            return null;
        }
        Certificate publicKey = galleryKeyStore.getCertificate(ALIAS_NAME);
        return publicKey.getPublicKey();

    }

    public PrivateKey getPrivateKey() throws Exception {
        KeyStore galleryKeyStore = KeyStore.getInstance("AndroidKeyStore");
        galleryKeyStore.load(null);
        if(!galleryKeyStore.containsAlias(ALIAS_NAME)){
            return null;
        }
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) galleryKeyStore.getEntry(ALIAS_NAME, null);
        return privateKeyEntry.getPrivateKey();
    }
    public String encryptPin(String pin) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PublicKey publicKey = getPublicKey();
        if (publicKey != null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeToString(cipher.doFinal(pin.getBytes(Charset.defaultCharset())), Base64.DEFAULT);
        }
        return null;
    }
    public String decryptString(String encoded) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PrivateKey publicKey = getPrivateKey();
        if (publicKey != null) {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(cipher.doFinal(Base64.decode(encoded, Base64.DEFAULT)), Charset.defaultCharset());
        }
        return null;
    }

    public static void savePinWithEncoded(Context context, String pinCode) throws Exception{
        byte[] salts = new byte[16];
        new Random().nextBytes(salts);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salts);
//        Convert Salts to string, hashed PinCode
        String storedSalt = Base64.encodeToString(salts, Base64.DEFAULT);
        String hashedPinCode = Base64.encodeToString(md.digest(pinCode.getBytes(Charset.defaultCharset())), Base64.DEFAULT);

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY, pinCode);
        editor.putString(PREF_LOCKER, storedSalt);
        editor.apply();

        Log.i("PRIVATE SET PREFERENCE", sharedPreferences.getString(PREF_KEY, "Not set yet"));
    }

    public static boolean validatePinCode(Context context, String pinCode) throws Exception {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String storedSalt = sharedPreferences.getString(PREF_LOCKER, "");
        String storedPin = sharedPreferences.getString(PREF_KEY, "");
        Log.i("PRIVATE SET", sharedPreferences.getString(PREF_KEY, "Not set yet"));

        if (storedSalt.isEmpty() || storedPin.isEmpty()) throw new Exception("Missing Salt or Pin");
        byte[] decodedSalt = Base64.decode(storedSalt, Base64.DEFAULT);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(decodedSalt);
        String encodedPinCode = Base64.encodeToString(md.digest(pinCode.getBytes(Charset.defaultCharset())), Base64.DEFAULT);
        Log.i("PRIVATE SET - PINCODE", (encodedPinCode.contains(storedPin) ?"Forreal":"nah"));

        return pinCode.equals(storedPin);
    }

}
