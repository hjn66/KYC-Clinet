package com.areatak.saderat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferences sharedPref = this.getSharedPreferences("SETTING", Context.MODE_PRIVATE);

        EditText textServer = findViewById(R.id.textAddress);
        String serverAddressDefaultValue = getResources().getString(R.string.server_address_default);
        String serverAddress = sharedPref.getString(getString(R.string.server_address), serverAddressDefaultValue);
        textServer.setText(serverAddress);

        EditText textPort = findViewById(R.id.textPort);
        String serverPortDefaultValue = getResources().getString(R.string.server_port_default);
        String serverPort = sharedPref.getString(getString(R.string.server_port), serverPortDefaultValue);
        textPort.setText(serverPort);

        EditText textProtocol = findViewById(R.id.textProtocol);
        String serverProtocolDefaultValue = getResources().getString(R.string.server_protocol_default);
        String serverProtocol = sharedPref.getString(getString(R.string.server_protocol), serverProtocolDefaultValue);
        textProtocol.setText(serverProtocol);
    }
    /** Called when the user taps the Save button */
    public void saveSetting(View view) {
        SharedPreferences sharedPref = this.getSharedPreferences("SETTING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        EditText textServer = findViewById(R.id.textAddress);
        editor.putString(getString(R.string.server_address), textServer.getText().toString());
        editor.commit();

        EditText textPort = findViewById(R.id.textPort);
        editor.putString(getString(R.string.server_port), textPort.getText().toString());
        editor.commit();

        EditText textProtocol = findViewById(R.id.textProtocol);
        editor.putString(getString(R.string.server_protocol), textProtocol.getText().toString());
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
