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

        RSAPrivateKey pkey = null;
        RSAPublicKey pukey = null;
        try {
            String privateKey = sharedPref.getString(getString(R.string.privateKey), "");
            pkey = RSA.getPrivateKeyFromString(privateKey);
            pukey = RSA.getPublicKeyFromString(publicKey);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }


        String message = "ox0HD1cPgM";
        try {
            String signed = RSA.sign(pkey, message);
            txtPublicKey.setText(signed);
            if (RSA.verify(pukey, message, signed)) {
                txtPublicKey.setText("Verified");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        registerStatus = sharedPref.getString(getString(R.string.register_status), "Pending");
        if (!registerStatus.equals("Pending")) {

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

    private void checkStatus() throws MalformedURLException {
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
        String nonce = sharedPref.getString(getString(R.string.register_nonce), "");
        URL url = new URL("http://46.105.145.154:8003/registers?nonce=" + nonce);
        try {
            String registerStatusResult = new CheckRegisterStatus().execute(url).get();
            try {
                JSONObject jsonObject = new JSONObject(registerStatusResult);
                registerStatus = jsonObject.getString("Status");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.register_status), registerStatus);
                buttonStatus.setText(registerStatus);
                if (registerStatus.equals("Pending")) {
                    buttonStatus.setBackgroundResource(R.drawable.button_bg_rounded_corners_pending);
                    buttonStatus.setTextColor(getApplication().getResources().getColor(R.color.yellowSolid));
                }else {
                    timer.cancel();
                    if (registerStatus.equals("Approved")) {
                        buttonStatus.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                        buttonStatus.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
                    }
                    if (registerStatus.equals("Denied")) {
                        buttonStatus.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                        buttonStatus.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
