package com.areatak.kycclient;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

class SendLoginPost extends AsyncTask<LoginData, Void, String> {

    protected String doInBackground(LoginData... loginInformation) {
        try {
            LoginData loginData = loginInformation[0];
            URL url = loginData.getUrl();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("FirstName", loginData.getFirstName());
            jsonParam.put("LastName", loginData.getLastName());
            jsonParam.put("SignedNonce", loginData.getSignedNonce());
            jsonParam.put("Image", loginData.getEncodedPhoto().replace("\n",""));
            jsonParam.put("Ticket", loginData.getTicket());
            jsonParam.put("GUID", loginData.getGUID());

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            conn.disconnect();
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}