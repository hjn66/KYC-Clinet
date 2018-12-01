package com.areatak.kycclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements View.OnClickListener {

    private String registerStatus;
    private Timer timer;
    private static final int RC_HANDLE_STORAGE_PERM = 3;
    private static final String LOG_TAG = "KYC.MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isExternalStorageWritable()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_STORAGE_PERM);

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }else{
                createDirectory();
            }
//            getPublicAlbumStorageDir("KYC");

        }
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
        registerStatus = sharedPref.getString(getString(R.string.register_status), "Pending");
        applyStatus(registerStatus);

        findViewById(R.id.button_register).setOnClickListener(this);
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_STORAGE_PERM) {
            Log.d(LOG_TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Storage permission granted");
            createDirectory();

            // we have permission, so create the camerasource
            return;
        }

        Log.e(LOG_TAG, "Storage permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("KYC Client")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private String writeXml(){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "messages");
            serializer.text("Salam");
            serializer.endTag("", "messages");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void applyStatus(String status) {
        Button loginButton = findViewById(R.id.button_login);
        if (status.equals("Approved")) {
            loginButton.setTextColor(getApplication().getResources().getColor(R.color.purpleSolid));
        } else {
            loginButton.setTextColor(getApplication().getResources().getColor(R.color.colorPrimary));
        }
    }

    private void checkStatus() throws MalformedURLException {
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
        String nonce = sharedPref.getString(getString(R.string.register_nonce), "");
        SharedPreferences settingSharedPref = this.getSharedPreferences("SETTING", Context.MODE_PRIVATE);
        String protocol = settingSharedPref.getString(getString(R.string.server_protocol), getString(R.string.server_protocol_default));
        String serverAddress = settingSharedPref.getString(getString(R.string.server_address), getString(R.string.server_address_default));
        String serverPort = settingSharedPref.getString(getString(R.string.server_port), getString(R.string.server_port_default));

        String urlString = protocol + "://" + serverAddress + ":" + serverPort + getString(R.string.check_status_subURL) + nonce;
        URL url = new URL(urlString);

        try {
            String registerStatusResult = new CheckRegisterStatus().execute(url).get();
            JSONObject jsonObject = new JSONObject(registerStatusResult);
            registerStatus = jsonObject.getString("Status");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.register_status), registerStatus);
            editor.commit();
            applyStatus(registerStatus);
            if (!registerStatus.equals("Pending")) {
                int guid = Integer.parseInt(jsonObject.getString("GUID"));
                editor.putInt(getString(R.string.register_guid), guid);
                editor.commit();
                timer.cancel();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    private void createDirectory() {
        Context context = getApplicationContext();
//        File file = new File(context.getExternalFilesDir(
//                Environment.DIRECTORY_PICTURES), "ssss");
//        File file = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "SS");
        File file = new File(Environment.getExternalStorageDirectory()+File.separator+"11KYC");
        if (!file.exists()) {
            file.mkdirs();

            Log.w("DEBUG", "Created default directory.");

        }
        File gpxfile = new File(file, "1.txt");
        try {
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(writeXml());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("DEBUG", "Directory not created");
        }
        return file;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_login) {
            if (!registerStatus.equals("Approved")) {
                Snackbar.make(findViewById(R.id.main_activity), getString(R.string.no_registered_message), Snackbar.LENGTH_LONG).show();
            } else {
                // launch login activity.
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
            }
        }
        if (v.getId() == R.id.text_configure) {
            // launch setting activity.
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.button_register) {
            SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
            Boolean isRegistered = sharedPref.getBoolean(getString(R.string.isRegistered), false);
            if (isRegistered) {
                Intent intent = new Intent(this, Profile.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, Register.class);
                startActivity(intent);
            }
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == RC_BARCODE_CAPTURE) {
//            if (resultCode == CommonStatusCodes.SUCCESS) {
//                if (data != null) {
//                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
//                    statusMessage.setText(R.string.barcode_success);
//                    barcodeValue.setText(barcode.displayValue);
//                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
//                } else {
//                    statusMessage.setText(R.string.barcode_failure);
//                    Log.d(TAG, "No barcode captured, intent data is null");
//                }
//            } else {
//                statusMessage.setText(String.format(getString(R.string.barcode_error),
//                        CommonStatusCodes.getStatusCodeString(resultCode)));
//            }
//        }
//        else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
}
