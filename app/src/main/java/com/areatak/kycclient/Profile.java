package com.areatak.kycclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Profile extends AppCompatActivity {
    private String registerStatus;
    private Button buttonStatus;
    private Timer timer;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        buttonStatus = findViewById(R.id.buttonStatus);

        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);

        TextView textName = findViewById(R.id.textName);
        String NameDefaultValue = getResources().getString(R.string.name_default);
        String fullName = sharedPref.getString(getString(R.string.xml_firstName), NameDefaultValue);
        fullName += " " + sharedPref.getString(getString(R.string.xml_lastName), NameDefaultValue);
        textName.setText(fullName);

        EditText txtBirthDate = findViewById(R.id.textBirthDate);
        String birthDate = sharedPref.getString(getString(R.string.xml_birthDate), "");
        txtBirthDate.setText(birthDate);

        EditText txtNationalID = findViewById(R.id.textnationalId);
        String nationalID = sharedPref.getString(getString(R.string.xml_nationalID), "");
        txtNationalID.setText(nationalID);

        EditText txtPublicKey = findViewById(R.id.textPublickey);
        String publicKey = sharedPref.getString(getString(R.string.xml_publicKey), "");
        txtPublicKey.setText(publicKey);

        imageView = findViewById(R.id.profile_image);
        try {
            File folderKYC = new File(Environment.getExternalStorageDirectory(), getString(R.string.KYC_folder_name));
            File profileImageFile = new File(folderKYC,  getString(R.string.profile_image_file));
            InputStream inputStream = new FileInputStream(profileImageFile);
            imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        registerStatus = sharedPref.getString(getString(R.string.register_status), "Pending");
        applyStatus(registerStatus);

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
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void applyStatus(String status) {
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
                writeRegisterXML();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void writeRegisterXML()
    {
        String registerXML = generateRegisterXML();
        File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.KYC_folder_name));
        if (!file.mkdirs()) {
            Log.w("DEBUG", "directory not created");
        }
        File registerFile = new File(file, getString(R.string.register_file));
        try {
            FileWriter writer = new FileWriter(registerFile);
            writer.append(registerXML);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateRegisterXML(){
        SharedPreferences sharedPref = getSharedPreferences("PROFILE", Context.MODE_PRIVATE);

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "Register");

            serializer.startTag("", getString(R.string.register_guid));
            int guid = sharedPref.getInt(getString(R.string.register_guid), -1);
            serializer.text(String.valueOf(guid));
            serializer.endTag("", getString(R.string.register_guid));

            serializer.startTag("", getString(R.string.register_nonce));
            serializer.text(sharedPref.getString(getString(R.string.register_nonce), ""));
            serializer.endTag("", getString(R.string.register_nonce));

            serializer.startTag("", getString(R.string.xml_firstName));
            serializer.text(sharedPref.getString(getString(R.string.xml_firstName), ""));
            serializer.endTag("", getString(R.string.xml_firstName));

            serializer.startTag("", getString(R.string.xml_nationalID));
            serializer.text(sharedPref.getString(getString(R.string.xml_nationalID), ""));
            serializer.endTag("", getString(R.string.xml_nationalID));

            serializer.startTag("", getString(R.string.xml_lastName));
            serializer.text(sharedPref.getString(getString(R.string.xml_lastName), ""));
            serializer.endTag("", getString(R.string.xml_lastName));

            serializer.startTag("", getString(R.string.xml_birthDate));
            serializer.text(sharedPref.getString(getString(R.string.xml_birthDate), ""));
            serializer.endTag("", getString(R.string.xml_birthDate));

            serializer.startTag("", getString(R.string.xml_publicKey));
            serializer.text(sharedPref.getString(getString(R.string.xml_publicKey), ""));
            serializer.endTag("", getString(R.string.xml_publicKey));

            serializer.startTag("", getString(R.string.xml_privateKey));
            serializer.text(sharedPref.getString(getString(R.string.xml_privateKey), ""));
            serializer.endTag("", getString(R.string.xml_privateKey));

            serializer.endTag("", "Register");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
