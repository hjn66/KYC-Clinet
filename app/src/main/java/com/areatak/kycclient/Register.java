package com.areatak.kycclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private String ticket;
    private String nonce;
    private EditText organizationText;
    private EditText textNationalID;
    private EditText textFirstName;
    private EditText textLastName;
    private EditText textBirthDate;
    private ProgressBar spinner;
    private String encodedImage = "";
    private Button regButton;



    private static final String TAG = "KYCRegister";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        organizationText = findViewById(R.id.textOrganization);

        textNationalID = findViewById(R.id.textnationalId);
        textFirstName = findViewById(R.id.textFirstName);
        textLastName = findViewById(R.id.textLastName);
        textBirthDate = findViewById(R.id.textBirthDate);
        regButton = findViewById(R.id.button_register);
        textNationalID.requestFocus();

        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);

        textNationalID.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkNationalId(s.toString())) {
                    TextInputLayout til = findViewById(R.id.textLayoutNationalId);
                    til.setErrorEnabled(false);
                    til.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        textNationalID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!checkNationalId(textNationalID.getText().toString())) {
                        TextInputLayout til = findViewById(R.id.textLayoutNationalId);
                        til.setError(getText(R.string.invalid_nationalID));
                    }
                }
            }
        });

        textFirstName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    TextInputLayout til = findViewById(R.id.textLayoutFirstName);
                    til.setErrorEnabled(false);
                    til.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        textFirstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (textFirstName.getText().length() == 0) {
                        TextInputLayout til = findViewById(R.id.textLayoutFirstName);
                        til.setError(getText(R.string.invalid_firstName));
                    }
                }
            }
        });

        textLastName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    TextInputLayout til = findViewById(R.id.textLayoutLastName);
                    til.setErrorEnabled(false);
                    til.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        textLastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (textLastName.getText().length() == 0) {
                        TextInputLayout til = findViewById(R.id.textLayoutLastName);
                        til.setError(getText(R.string.invalid_lastName));
                    }
                }
            }
        });

        textBirthDate.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    TextInputLayout til = findViewById(R.id.textLayoutBirthDate);
                    til.setErrorEnabled(false);
                    til.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        textBirthDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (textBirthDate.getText().length() == 0) {
                        TextInputLayout til = findViewById(R.id.textLayoutBirthDate);
                        til.setError(getText(R.string.invalid_birthDate));
                    }
                }
            }
        });
    }

    public void openBarcode(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void profileImageError(CircleImageView imageView, String message) {
        imageView.setBorderColor(getApplication().getResources().getColor(R.color.redSolid));
        Snackbar.make(findViewById(R.id.activity_register), message, Snackbar.LENGTH_LONG).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
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
                    CircleImageView imageView = findViewById(R.id.profile_image);
                    imageView.setBorderColor(getApplication().getResources().getColor(R.color.purpleSolid));
                    int size = inputStream.available();
                    int max = getResources().getInteger(R.integer.max_image_size);
                    if (size > max) {
                        profileImageError(imageView, getString(R.string.profile_image_max));
                    } else {
                        byte[] bytes = readBytes(inputStream);
                        File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.KYC_folder_name));
                        if (!file.mkdirs()) {
                            Log.w("DEBUG", "directory not created");
                        }
                        File registerFile = new File(file, getString(R.string.profile_image_file));
                        try {
                            FileOutputStream os = new FileOutputStream(registerFile);
                            os.write(bytes);
                            os.flush();
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                        inputStream = getContentResolver().openInputStream(uri);
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                    }
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

    private boolean checkNationalId(String nationalID) {
        if (nationalID.length() != 10) {
            return false;
        }
        long nationalIdInt = Long.parseLong(nationalID);
        int sum = 0;
        int pos = 2;
        int controlDig = (int)(nationalIdInt % 10);
        nationalIdInt = nationalIdInt / 10;
        while (nationalIdInt > 0) {
            sum += (nationalIdInt % 10) * pos;
            nationalIdInt = nationalIdInt / 10;
            pos++;
        }
        if (sum % 11 < 2) {
            if (sum % 11 == controlDig)
                return true;
        } else {
            if (sum % 11 == (11 - controlDig)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkFields() {
        boolean error = false;
        if (!checkNationalId(textNationalID.getText().toString())) {
            TextInputLayout til = findViewById(R.id.textLayoutNationalId);
            til.setError(getText(R.string.invalid_nationalID));
            error = true;
        }
        if (textFirstName.getText().toString().length() == 0) {
            TextInputLayout til = findViewById(R.id.textLayoutFirstName);
            til.setError(getString(R.string.invalid_firstName));
            error = true;
        }
        if (textLastName.getText().toString().length() == 0) {
            TextInputLayout til = findViewById(R.id.textLayoutLastName);
            til.setError(getString(R.string.invalid_lastName));
            error = true;
        }
        if (textBirthDate.getText().toString().length() == 0) {
            TextInputLayout til = findViewById(R.id.textLayoutBirthDate);
            til.setError(getString(R.string.invalid_birthDate));
            error = true;
        }
        if (encodedImage.length() == 0) {
            CircleImageView imageView = findViewById(R.id.profile_image);
            profileImageError(imageView, getString(R.string.profile_image_required));
            error = true;
        }
        return !error;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void performRegister(View view) {
        regButton.setEnabled(false);
        regButton.setTextColor(getApplication().getResources().getColor(R.color.colorPrimary));
        if (!checkFields()){
            Snackbar.make(findViewById(R.id.activity_register), getString(R.string.register_error), Snackbar.LENGTH_LONG).show();
            regButton.setEnabled(true);
            regButton.setTextColor(getApplication().getResources().getColor(R.color.purpleSolid));
            return;
        }
        spinner.setVisibility(View.VISIBLE);
        String nationalId = textNationalID.getText().toString();
        String firstName = textLastName.getText().toString();
        String lastName = textLastName.getText().toString();
        String birthDate = textBirthDate.getText().toString();
        SharedPreferences sharedPref = this.getSharedPreferences("SETTING", Context.MODE_PRIVATE);
        String protocol = sharedPref.getString(getString(R.string.server_protocol), getString(R.string.server_protocol_default));
        String serverAddress = sharedPref.getString(getString(R.string.server_address), getString(R.string.server_address_default));
        String serverPort = sharedPref.getString(getString(R.string.server_port), getString(R.string.server_port_default));

        String url = protocol + "://" + serverAddress + ":" + serverPort + getString(R.string.register_subURL);

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.genKeyPair();
            String privateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT);
            String publicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
            privateKey = privateKey.replace("\n", "");
            publicKey = publicKey.replace("\n", "");
            publicKey = "-----BEGIN PUBLIC KEY-----\n" + publicKey + "\n-----END PUBLIC KEY-----";
            RegisterData registerData = new RegisterData(url, nationalId, firstName, lastName, birthDate, encodedImage, publicKey, nonce, ticket);
            String registerResult = new SendRegisterPost().execute(registerData).get();
            Snackbar.make(findViewById(R.id.activity_register), registerResult, Snackbar.LENGTH_LONG).show();
            FileOutputStream outputStream;

            outputStream = openFileOutput("publicKey.pem", Context.MODE_PRIVATE);
            outputStream.write(publicKey.getBytes());
            outputStream.close();

            outputStream = openFileOutput("private.pem", Context.MODE_PRIVATE);
            outputStream.write(privateKey.getBytes());
            outputStream.close();

            sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString(getString(R.string.register_status), "Pending");
            editor.commit();

            editor.putString(getString(R.string.register_nonce), nonce);
            editor.commit();

            editor.putString(getString(R.string.xml_nationalID), nationalId);
            editor.commit();

            editor.putString(getString(R.string.xml_firstName), firstName);
            editor.commit();

            editor.putString(getString(R.string.xml_lastName), lastName);
            editor.commit();

            editor.putString(getString(R.string.xml_birthDate), birthDate);
            editor.commit();

            editor.putString(getString(R.string.encoded_image), encodedImage);
            editor.commit();

            editor.putString(getString(R.string.xml_privateKey), privateKey);
            editor.commit();

            editor.putString(getString(R.string.xml_publicKey), publicKey);
            editor.commit();

            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
            spinner.setVisibility(View.GONE);
            finish();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
