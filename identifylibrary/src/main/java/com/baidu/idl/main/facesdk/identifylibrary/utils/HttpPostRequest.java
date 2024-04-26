package com.baidu.idl.main.facesdk.identifylibrary.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpPostRequest extends AsyncTask<Void, Void, String> {

    private static final String TAG = "hcy--HttpPostRequest";
    private final String url;
    private final JSONObject json;
    private final OnResponseListener onResponseListener;

    public interface OnResponseListener {
        void onResponse(String response);
    }

    public HttpPostRequest(String url, JSONObject json, OnResponseListener onResponseListener) {
        this.url = url;
        this.json = json;
        this.onResponseListener = onResponseListener;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return performPostRequest();
        } catch (IOException e) {
            Log.e(TAG, "Error during POST request", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (onResponseListener != null) {
            onResponseListener.onResponse(result);
        }
    }

    private String performPostRequest() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            // Create connection
            URL urlObject = new URL(url);
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            // Write data to the connection
            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
            //outputStream.writeBytes(json.toString());
            outputStream.write(json.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            // Get the response
            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG,"responseCode="+responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            } else {
                Log.e(TAG, "POST request failed with response code: " + responseCode);
                return null;
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String buildPostDataString(Map<String, String> data) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result.toString();
    }
}


