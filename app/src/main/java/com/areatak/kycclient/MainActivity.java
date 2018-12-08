package com.areatak.kycclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int RC_HANDLE_STORAGE_PERM = 3;
    private static final String LOG_TAG = "KYC.MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.text_configure).setOnClickListener(this);
        findViewById(R.id.button_profile).setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_STORAGE_PERM) {
            Log.d(LOG_TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Storage permission granted");
            Intent intent = new Intent(this, Register.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.e(LOG_TAG, "Storage permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("KYC Client")
                .setMessage(R.string.no_storage_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.text_configure) {
            // launch setting activity.
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
            finish();
        }
        if (v.getId() == R.id.button_register) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_STORAGE_PERM);
                return;
            } else {
                Intent intent = new Intent(this, Register.class);
                startActivity(intent);
                finish();
            }
        }
        if (v.getId() == R.id.button_profile) {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
            finish();
        }
    }
}
