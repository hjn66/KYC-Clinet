package com.areatak.kycclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.util.concurrent.ExecutionException;


public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "KYCLogin";
    private String ticket;
    private String nonce;
    private String fields;
    private TextView loginOrganization;
    private Button firstNameButton;
    private Button lastNameButton;
    private Button nationalIdButton;
    private Button imageButton;
    private Button birthDateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_edit_login).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        loginOrganization = findViewById(R.id.textLoginTitle);
        firstNameButton = findViewById(R.id.button_firstName);
        lastNameButton = findViewById(R.id.button_lastName);
        nationalIdButton = findViewById(R.id.button_nationalId);
        imageButton = findViewById(R.id.button_image);
        birthDateButton = findViewById(R.id.button_birthDate);
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);

        EditText textNationalId = findViewById(R.id.textnationalId);
        textNationalId.setText(sharedPref.getString(getString(R.string.nationalID), ""));

        EditText textFirstName = findViewById(R.id.textFirstName);
        textFirstName.setText(sharedPref.getString(getString(R.string.firstName), ""));

        EditText textLastName = findViewById(R.id.textLastName);
        textLastName.setText(sharedPref.getString(getString(R.string.lastName), ""));

        EditText textBirthDate = findViewById(R.id.textBirthDate);
        textBirthDate.setText(sharedPref.getString(getString(R.string.birthDate), ""));

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == RC_BARCODE_CAPTURE && resultCode == CommonStatusCodes.SUCCESS) {
            if (resultData != null) {
                Barcode barcode = resultData.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                try {
                    JSONObject jsonObject = new JSONObject(barcode.displayValue);
                    loginOrganization.setText(getString(R.string.Login_to) + " " + jsonObject.getString("O"));
                    ticket = jsonObject.getString("T");
                    nonce = jsonObject.getString("N");
                    fields = jsonObject.getString("F");
                    if (fields.indexOf('F') >= 0) {
                        firstNameButton.setText(getString(R.string.required));
                        firstNameButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                        firstNameButton.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
                    } else {
                        firstNameButton.setText(getString(R.string.not_required));
                        firstNameButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                        firstNameButton.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
                    }

                    if (fields.indexOf('N') >= 0) {
                        nationalIdButton.setText(getString(R.string.required));
                        nationalIdButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                        nationalIdButton.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
                    } else {
                        nationalIdButton.setText(getString(R.string.not_required));
                        nationalIdButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                        nationalIdButton.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
                    }

                    if (fields.indexOf('L') >= 0) {
                        lastNameButton.setText(getString(R.string.required));
                        lastNameButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                        lastNameButton.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
                    } else {
                        lastNameButton.setText(getString(R.string.not_required));
                        lastNameButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                        lastNameButton.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
                    }

                    if (fields.indexOf('I') >= 0) {
                        imageButton.setText(getString(R.string.required));
                        imageButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                        imageButton.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
                    } else {
                        imageButton.setText(getString(R.string.not_required));
                        imageButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                        imageButton.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
                    }

                    if (fields.indexOf('B') >= 0) {
                        birthDateButton.setText(getString(R.string.required));
                        birthDateButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_approved);
                        birthDateButton.setTextColor(getApplication().getResources().getColor(R.color.greenSolid));
                    } else {
                        birthDateButton.setText(getString(R.string.not_required));
                        birthDateButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_denied);
                        birthDateButton.setTextColor(getApplication().getResources().getColor(R.color.redSolid));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d(TAG, "Barcode read: " + barcode.displayValue);
            } else {
                Log.d(TAG, "No barcode captured, intent data is null");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_login) {
            performLogin();
        }
        if (v.getId() == R.id.button_edit_login) {
            Intent intent = new Intent(this, LoginEdit.class);
            intent.putExtra(getString(R.string.login_fields),fields);
            intent.putExtra(getString(R.string.login_ticket),ticket);
            intent.putExtra(getString(R.string.login_nonce),nonce);
            intent.putExtra(getString(R.string.login_organization),loginOrganization.getText().toString());
            startActivity(intent);
        }
        if (v.getId() == R.id.button_cancel) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    private String signNonce(String nonce) {
        RSAPrivateKey pkey = null;
        String signed = "";
        try {
            SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
            String privateKey = sharedPref.getString(getString(R.string.privateKey), "");
            pkey = RSA.getPrivateKeyFromString(privateKey);
            signed = RSA.sign(pkey, nonce);
            signed = signed.replace("\n", "");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return signed;
    }

    public void performLogin() {
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
        int guid = sharedPref.getInt(getString(R.string.register_guid), 0);
//        int guid = 1006;
        String firstName = sharedPref.getString(getString(R.string.firstName), "");
        String lastName = sharedPref.getString(getString(R.string.lastName), "");
        String encodedImage = sharedPref.getString(getString(R.string.encoded_image), "");

        sharedPref = this.getSharedPreferences("SETTING", Context.MODE_PRIVATE);
        String protocol = sharedPref.getString(getString(R.string.server_protocol), getString(R.string.server_protocol_default));
        String serverAddress = sharedPref.getString(getString(R.string.server_address), getString(R.string.server_address_default));
        String serverPort = sharedPref.getString(getString(R.string.server_port), getString(R.string.server_port_default));

        String url = protocol + "://" + serverAddress + ":" + serverPort + getString(R.string.login_subURL);
        try {
            LoginData loginData = new LoginData(url, guid, firstName, lastName, encodedImage, signNonce(nonce), ticket);
            String loginResult = "";
            loginResult = new SendLoginPost().execute(loginData).get();
            JSONObject jsonObject = new JSONObject(loginResult);
            Snackbar.make(findViewById(R.id.activity_login), jsonObject.getString("Message"), Snackbar.LENGTH_LONG).show();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
