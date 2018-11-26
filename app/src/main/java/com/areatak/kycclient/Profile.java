package com.areatak.kycclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Profile extends AppCompatActivity {
    private String registerStatus;
    private Button buttonStatus;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        buttonStatus = findViewById(R.id.buttonStatus);

        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);

        TextView textName = findViewById(R.id.textName);
        String NameDefaultValue = getResources().getString(R.string.name_default);
        String fullName = sharedPref.getString(getString(R.string.firstName), NameDefaultValue);
        fullName += " " + sharedPref.getString(getString(R.string.lastName), NameDefaultValue);
        textName.setText(fullName);

        EditText txtBirthDate = findViewById(R.id.textBirthDate);
        String birthDate = sharedPref.getString(getString(R.string.birthDate), "");
        txtBirthDate.setText(birthDate);

        EditText txtNationalID = findViewById(R.id.textnationalId);
        String nationalID = sharedPref.getString(getString(R.string.nationalID), "");
        txtNationalID.setText(nationalID);

        EditText txtPublicKey = findViewById(R.id.textPublickey);
        String publicKey = sharedPref.getString(getString(R.string.publicKey), "");
        txtPublicKey.setText(publicKey);

        registerStatus = sharedPref.getString(getString(R.string.register_status), "Pending");
        applyStatus(registerStatus);
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
        buttonStatus.setText(status);
        if (status.equals("Pending")) {
            buttonStatus.setBackgroundResource(R.drawable.button_bg_rounded_corners_pending);
            buttonStatus.setTextColor(getApplication().getResources().getColor(R.color.yellowSolid));
        } else {
            if (status.equals("Approved")) {
                buttonStatus.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                buttonStatus.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
            }
            if (status.equals("Denied")) {
                buttonStatus.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                buttonStatus.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
            }
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
}
