package com.areatak.saderat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.util.concurrent.ExecutionException;


public class LoginEdit extends AppCompatActivity implements View.OnClickListener {
    private String ticket;
    private String nonce;
    private TextView loginOrganization;
    private String fields;
    private String organization;
    private String encodedImage;
    private static final int READ_REQUEST_CODE = 45;
    private EditText textNationalId;
    private EditText textFirstName;
    private EditText textLastName;
    private EditText textBirthDate;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_login);
        fields = getIntent().getStringExtra(getString(R.string.login_fields));
        ticket = getIntent().getStringExtra(getString(R.string.login_ticket));
        nonce = getIntent().getStringExtra(getString(R.string.login_nonce));
        loginOrganization = findViewById(R.id.textLoginTitle);
        organization = getIntent().getStringExtra(getString(R.string.login_organization));
        loginOrganization.setText(getString(R.string.Login_to) + " " + organization);

        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        loginOrganization = findViewById(R.id.textLoginTitle);
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);

        textNationalId = findViewById(R.id.textnationalId);
        textNationalId.setText(sharedPref.getString(getString(R.string.xml_nationalID), ""));

        textFirstName = findViewById(R.id.textFirstName);
        textFirstName.setText(sharedPref.getString(getString(R.string.xml_firstName), ""));

        textLastName = findViewById(R.id.textLastName);
        textLastName.setText(sharedPref.getString(getString(R.string.xml_lastName), ""));

        textBirthDate = findViewById(R.id.textBirthDate);
        textBirthDate.setText(sharedPref.getString(getString(R.string.xml_birthDate), ""));

        imageView = findViewById(R.id.profile_image);
        try {
            File folderKYC = new File(Environment.getExternalStorageDirectory(), getString(R.string.KYC_folder_name));
            File profileImageFile = new File(folderKYC,  getString(R.string.profile_image_file));
            InputStream inputStream = new FileInputStream(profileImageFile);
            imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            inputStream = new FileInputStream(profileImageFile);
            byte[] bytes = readBytes(inputStream);
            encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fields.indexOf('N') >= 0) {
            textNationalId.setEnabled(true);
        } else {
            textNationalId.setEnabled(false);
        }
        if (fields.indexOf('F') >= 0) {
            textFirstName.setEnabled(true);
        } else {
            textFirstName.setEnabled(false);
        }
        if (fields.indexOf('L') >= 0) {
            textLastName.setEnabled(true);
        } else {
            textLastName.setEnabled(false);
        }
        if (fields.indexOf('B') >= 0) {
            textBirthDate.setEnabled(true);
        } else {
            textBirthDate.setEnabled(false);
        }
        if (fields.indexOf('I') >= 0) {
            imageView.setOnClickListener(this);
            findViewById(R.id.buttonChooseImage).setOnClickListener(this);
        }
    }

    public void performFileSearch(View view) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    int size =inputStream.available();
                    Snackbar.make(findViewById(R.id.activity_edit_login), String.valueOf(size), Snackbar.LENGTH_LONG).show();
                    ImageView imageView = findViewById(R.id.profile_image);
                    byte[] bytes = readBytes(inputStream);
                    encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                    inputStream = getContentResolver().openInputStream(uri);

                    imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_login) {
            performEditLogin();
        }
        if (v.getId() == R.id.button_cancel) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.buttonChooseImage || v.getId() == R.id.profile_image) {
            performFileSearch(v);
        }

    }

    private String signNonce(String nonce) {
        RSAPrivateKey pkey = null;
        String signed = "";
        try {
            SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
            String privateKey = sharedPref.getString(getString(R.string.xml_privateKey), "");
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

    private void performEditLogin() {
        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
        int guid = sharedPref.getInt(getString(R.string.register_guid), 0);
//        int guid = 1006;
        String firstName = textFirstName.getText().toString();
        String lastName = textLastName.getText().toString();

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
            Snackbar.make(findViewById(R.id.activity_edit_login), jsonObject.getString("Message"), Snackbar.LENGTH_LONG).show();
            if (jsonObject.getBoolean("CheckImage") &&jsonObject.getBoolean("CheckFirstName") &&jsonObject.getBoolean("CheckLastName") ){
                Intent intent = new Intent(this, Account.class);
                intent.putExtra(getString(R.string.login_organization),organization);
                startActivity(intent);
            }

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

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
}
