package com.skamenialo.fingreprinttohome;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainService extends Service implements FingerprintHelper.Callback{
    private static final String TAG = "Fingerprint.Service";
    private FingerprintHelper mFingerprintHelper;
    private FingerprintManager.CryptoObject mCryptoObject;
    private Signature mSignature;
    private KeyStore mKeyStore;
    private KeyPairGenerator mKeyPairGenerator;
    private boolean mRegistered;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFingerprintHelper = new FingerprintHelper(getSystemService(FingerprintManager.class), this);
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            Log.i(TAG, "KeyStore.getInstance");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        try {
            mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            Log.i(TAG, "mKeyPairGenerator.getInstance");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            mSignature = Signature.getInstance("SHA256withECDSA");
            Log.i(TAG, "Signature.getInstance");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        createKeyPair();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if(!mRegistered)
            register(true);
        return Service.START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if(mRegistered)
            register(false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onAuthenticated() {
        Log.i(TAG, "onAuthenticated");
        goHome();
    }

    @Override
    public void onError() {
        Log.i(TAG, "onError");
        goHome();
    }

    private void goHome(){
        Log.i(TAG, "goHome");
    }

    private void register(boolean register) {
        if(register){
            if(initSignature()) {
                mCryptoObject = new FingerprintManager.CryptoObject(mSignature);
                mFingerprintHelper.startListening(mCryptoObject);
                mRegistered = true;
                Log.i(TAG, "Registered");
            }else
                Log.w(TAG, "Not registered");
        }else {
            mFingerprintHelper.stopListening();
            mRegistered = false;
            Log.i(TAG, "Unregistered");
        }
    }

    private boolean initSignature() {
        try {
            mKeyStore.load(null);
            PrivateKey key = (PrivateKey) mKeyStore.getKey(Utils.KEY_NAME, null);
            mSignature.initSign(key);
            Log.i(TAG, "Signature initialized");
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    private void createKeyPair() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            mKeyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(Utils.KEY_NAME,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            .setUserAuthenticationRequired(false)
                            .build());
            mKeyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

}
