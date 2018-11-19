package com.areatak.kycclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class Register extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private String ticket;
    private String nounce;
    private EditText organizationText;

    private static final String TAG = "KYCRegister";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        organizationText = (EditText) findViewById(R.id.textOrganization);
        findViewById(R.id.textnationalId).requestFocus();


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
                    ImageView imageView = (ImageView) findViewById(R.id.profile_image);

                    imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                } catch (FileNotFoundException e) {
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
                    nounce = jsonObject.getString("N");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d(TAG, "Barcode read: " + barcode.displayValue);
            } else {
                Log.d(TAG, "No barcode captured, intent data is null");
            }
        }
    }

    public void performRegister(View view) throws MalformedURLException {
        EditText nationalId = (EditText) findViewById(R.id.textnationalId);
        EditText firstName = (EditText) findViewById(R.id.textFirstName);
        EditText lastName = (EditText) findViewById(R.id.textLastName);
        RegisterData registerData = new RegisterData(nationalId.getText().toString(), nounce, ticket,firstName.getText().toString(),lastName.getText().toString());
        new SendRegisterPost().execute(registerData);
    }

}
