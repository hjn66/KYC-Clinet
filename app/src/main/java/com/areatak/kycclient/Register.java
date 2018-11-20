package com.areatak.kycclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class Register extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private String ticket;
    private String nonce;
    private EditText organizationText;
    private ProgressBar spinner;
    private String encodedImage;


    private static final String TAG = "KYCRegister";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        organizationText = findViewById(R.id.textOrganization);
        findViewById(R.id.textnationalId).requestFocus();
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        Snackbar.make(findViewById(R.id.IDDDD), R.string.server_address,
                Snackbar.LENGTH_LONG)
                .show();
    }

    public void openBarcode(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
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
        if (requestCode == RC_BARCODE_CAPTURE && resultCode == CommonStatusCodes.SUCCESS) {
            if (resultData != null) {
                Barcode barcode = resultData.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                try {
                    JSONObject jsonObject = new JSONObject(barcode.displayValue);
                    organizationText.setText(jsonObject.getString("O"));
                    ticket = jsonObject.getString("T");
                    nonce = jsonObject.getString("N");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d(TAG, "Barcode read: " + barcode.displayValue);
            } else {
                Log.d(TAG, "No barcode captured, intent data is null");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void performRegister(View view) throws MalformedURLException {
        String nationalId = ((EditText) findViewById(R.id.textnationalId)).getText().toString();
        String firstName = ((EditText) findViewById(R.id.textFirstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.textLastName)).getText().toString();
        String birthDate = ((EditText) findViewById(R.id.textBirthDate)).getText().toString();
        RegisterData registerData = new RegisterData(nationalId, firstName, lastName, birthDate, encodedImage, nonce, ticket);
        spinner.setVisibility(View.VISIBLE);
        try {
            String result = new SendRegisterPost().execute(registerData).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        spinner.setVisibility(View.GONE);
        Snackbar.make(findViewById(R.id.IDDDD), R.string.server_address, Snackbar.LENGTH_LONG).show();
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.genKeyPair();
            String privateKey = java.util.Base64.getMimeEncoder().encodeToString( keyPair.getPrivate().getEncoded());
            String publicKey = java.util.Base64.getMimeEncoder().encodeToString( keyPair.getPublic().getEncoded());
            FileOutputStream outputStream;

            try {
                    outputStream = openFileOutput("publicKey.pem", Context.MODE_PRIVATE);
                outputStream.write(publicKey.getBytes());
                outputStream.close();

                outputStream = openFileOutput("private.pem", Context.MODE_PRIVATE);
                outputStream.write(privateKey.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SharedPreferences sharedPref = this.getSharedPreferences("PROFILE",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString(getString(R.string.nationalID), nationalId);
            editor.commit();

            editor.putString(getString(R.string.firstName), firstName);
            editor.commit();

            editor.putString(getString(R.string.lastName), lastName);
            editor.commit();

            editor.putString(getString(R.string.birthDate), birthDate);
            editor.commit();

            editor.putString(getString(R.string.publicKey), publicKey);
            editor.commit();

            editor.putString(getString(R.string.privateKey), privateKey);
            editor.commit();

        } catch (NoSuchAlgorithmException e) {
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
