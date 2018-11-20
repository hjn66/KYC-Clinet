package com.areatak.kycclient;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class SendRegisterPost extends AsyncTask<RegisterData, Void, String> {

    protected String doInBackground(RegisterData... registerDatas) {
        try {
            RegisterData registerData = registerDatas[0];
            URL url = registerData.getUrl();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("NationalId", registerData.getNationalID());
            jsonParam.put("FirstName", registerData.getFirstName());
            jsonParam.put("LastName", registerData.getLastName());
            jsonParam.put("Nonce", registerData.getNonce());
            jsonParam.put("Photo", registerData.getEncodedPhoto());
            jsonParam.put("BirthDate", registerData.getBirthdate());
            jsonParam.put("Ticket", registerData.getTicket());

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
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
            Log.e("EMSG", e.toString());
            return null;
        }
    }

    protected void onPostExecute(Long result) {
        //
    }
}