package com.skamenialo.fingerprint2home;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.util.Log;

public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = "Fingerprint.Helper";

    private final FingerprintManager mFingerprintManager;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;

    FingerprintHelper(FingerprintManager fingerprintManager, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mCallback = callback;
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null);
        Log.i(TAG, "startListening");
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            Log.i(TAG, "stopListening");
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.i(TAG, "onAuthenticationError");
        if(!mSelfCancelled && mCallback!=null)
            mCallback.onError();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Log.i(TAG, "onAuthenticationHelp");
        if(!mSelfCancelled && mCallback!=null)
            mCallback.onFailed();
    }

    @Override
    public void onAuthenticationFailed() {
        Log.i(TAG, "onAuthenticationFailed");
        if(!mSelfCancelled && mCallback!=null)
            mCallback.onFailed();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Log.i(TAG, "onAuthenticationSucceeded");
        if(!mSelfCancelled && mCallback!=null)
            mCallback.onAuthenticated();
    }

    public interface Callback {

        void onAuthenticated();

        void onError();

        void onFailed();
    }
}