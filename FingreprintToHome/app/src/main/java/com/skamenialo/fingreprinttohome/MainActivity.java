package com.skamenialo.fingreprinttohome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "Fingerprint.MainAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        TextView textView = (TextView) findViewById(R.id.textView);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Fingerprint permission not granted!");
            textView.setText(R.string.permission_needed);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else if (!fingerprintManager.isHardwareDetected()) {
            Log.w(TAG, "Fingerprint not supported!");
            textView.setText(R.string.device_not_supports);
        } else {
            toggleButton.setEnabled(true);
            toggleButton.setOnCheckedChangeListener(this);
            if (MainService.getInstance() != null)
                toggleButton.setChecked(true);
            Log.i(TAG, "Fingerprint supported!");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        Intent service = new Intent(this, MainService.class);
        if(checked){
            startService(service);
            Log.i(TAG, "Service started");
        }else{
            stopService(service);
            Log.i(TAG, "Service stopped");
        }
    }
}
