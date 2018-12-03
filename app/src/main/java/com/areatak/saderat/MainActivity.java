package com.areatak.kycclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {

    private Timer timer;
    private static final int RC_HANDLE_STORAGE_PERM = 3;
    private static final String LOG_TAG = "KYC.MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.text_configure).setOnClickListener(this);

        final Handler handler = new Handler();
        timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            checkStatus();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000); //execute in every 1 sec
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
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            return;
        }

        Log.e(LOG_TAG, "Storage permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name))
                .setMessage(R.string.no_storage_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void checkStatus() {
        Button loginButton = findViewById(R.id.button_login);
        if (checkRegisterFileExists()) {
            loginButton.setTextColor(getApplication().getResources().getColor(R.color.purpleSolid));
            timer.cancel();
        } else {
            loginButton.setTextColor(getApplication().getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_login) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_STORAGE_PERM);
                return;
            }else {
                // Check if Registered And Approved
                if (checkRegisterFileExists()){
                    Intent intent = new Intent(this, Login.class);
                    startActivity(intent);
                }else{
                    Snackbar.make(findViewById(R.id.main_activity), getString(R.string.no_registered_message), Snackbar.LENGTH_LONG).show();
                }

            }
        }
        if (v.getId() == R.id.text_configure) {
            // launch setting activity.
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
        }
    }

    public boolean checkRegisterFileExists() {
        File folderKYC = new File(Environment.getExternalStorageDirectory(), getString(R.string.KYC_folder_name));
        if (!folderKYC.exists()) {
            return false;
        }
        File registerFile = new File(folderKYC, getString(R.string.register_file));
        if (!registerFile.exists()) {
            return false;
        }
        return true;
    }

    public void readRegisterXML() {

    }
}
