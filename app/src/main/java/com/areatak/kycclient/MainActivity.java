package com.areatak.kycclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
//    private TextView statusMessage;
//    private TextView barcodeValue;
    private String registerStatus;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
        registerStatus = sharedPref.getString(getString(R.string.register_status), "Pending");
        applyStatus(registerStatus);

        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.text_configure).setOnClickListener(this);

        if (registerStatus.equals("Pending")) {

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
    }

    private void applyStatus(String status){
        Button loginButton = findViewById(R.id.button_login);
        if (status.equals("Approved")) {
            loginButton.setTextColor(getApplication().getResources().getColor(R.color.purpleSolid));
        }else{
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
            }else {
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
            SharedPreferences sharedPref = this.getSharedPreferences("PROFILE",Context.MODE_PRIVATE);
            Boolean isRegistered = sharedPref.getBoolean(getString(R.string.isRegistered),false);
            if (isRegistered){
                Intent intent = new Intent(this, Profile.class);
                startActivity(intent);
            }else {
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
