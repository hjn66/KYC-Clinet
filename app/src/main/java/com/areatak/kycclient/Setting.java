package com.areatak.kycclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        EditText textServer = findViewById(R.id.editAddress);
        String serverAddressDefaultValue = getResources().getString(R.string.server_address_default);
        String serverAddress = sharedPref.getString(getString(R.string.server_address), serverAddressDefaultValue);
        textServer.setText(serverAddress);

        EditText textPort = findViewById(R.id.editPort);
        String serverPortDefaultValue = getResources().getString(R.string.server_port_default);
        String serverPort = sharedPref.getString(getString(R.string.server_port), serverPortDefaultValue);
        textPort.setText(serverPort);


        Spinner protocol = findViewById(R.id.spinnerProtocol);
        String serverProtocolDefaultValue = getResources().getString(R.string.server_protocol_default);
        String serverProtocol = sharedPref.getString(getString(R.string.server_protocol), serverProtocolDefaultValue);
        for (int i = 0; i < protocol.getAdapter().getCount(); i++) {
            if (protocol.getAdapter().getItem(i).toString().equals(serverProtocol)){
                protocol.setSelection(i);
            }
        }
    }
    /** Called when the user taps the Save button */
    public void saveSetting(View view) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        EditText textServer = findViewById(R.id.editAddress);
        editor.putString(getString(R.string.server_address), textServer.getText().toString());
        editor.commit();

        EditText textPort = findViewById(R.id.editPort);
        editor.putString(getString(R.string.server_port), textPort.getText().toString());
        editor.commit();

        Spinner protocol = findViewById(R.id.spinnerProtocol);
        editor.putString(getString(R.string.server_protocol), protocol.getSelectedItem().toString());
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
