package com.areatak.kycclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Account extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        findViewById(R.id.button_logout).setOnClickListener(this);


        SharedPreferences sharedPref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE);

        TextView textName = findViewById(R.id.textName);
        String NameDefaultValue = getResources().getString(R.string.name_default);
        String fullName = sharedPref.getString(getString(R.string.firstName), NameDefaultValue);
        fullName += " " + sharedPref.getString(getString(R.string.lastName), NameDefaultValue);
        textName.setText(fullName);
        TextView textOrganization = findViewById(R.id.text_login_successful);
        textOrganization.setText(getString(R.string.Login_to) + " " + getIntent().getStringExtra(getString(R.string.login_organization)));
        imageView = findViewById(R.id.profile_image);
        Uri uri;
        try {
            uri = Uri.parse(sharedPref.getString(getString(R.string.profile_image_uri), ""));
            InputStream inputStream = getContentResolver().openInputStream(uri);
            imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_logout) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
