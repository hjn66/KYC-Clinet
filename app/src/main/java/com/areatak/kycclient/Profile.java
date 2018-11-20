package com.areatak.kycclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Profile extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE",Context.MODE_PRIVATE);

        TextView textName = findViewById(R.id.textName);
        String NameDefaultValue = getResources().getString(R.string.name_default);
        String fullName = sharedPref.getString(getString(R.string.firstName), NameDefaultValue);
        fullName += " " +sharedPref.getString(getString(R.string.lastName), NameDefaultValue);
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
            if (RSA.verify(pukey, message, signed)){
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


    }
}
