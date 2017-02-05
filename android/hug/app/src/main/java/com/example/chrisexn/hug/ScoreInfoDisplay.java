package com.example.chrisexn.hug;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chrisexn on 2/4/2017.
 */

public class ScoreInfoDisplay extends AppCompatActivity {

    TextView mTextview;
    TextView mUsername;
    @Override
    public void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);
        setContentView(R.layout.score_activity);
        mTextview = (TextView)findViewById(R.id.score);
        mUsername = (TextView)findViewById(R.id.username);
        new GetScore().execute(Constants.getToken(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetScore extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... str) {
            String[] output = null;
            String token = str[0];
            String status = "";
            String urlEndPoint = Constants.WEB_URL + "/api/v1/hugs/score";

            //Run the api call
            HttpURLConnection client = null;
            try {
                URL url = new URL(urlEndPoint);
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("GET");
                client.setConnectTimeout(10000);
                client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                client.setRequestProperty("Authorization", " Token " + token);
                client.connect();
                int t = client.getResponseCode();
                if (t != 200) {
                    return null;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String text = br.readLine();
                JSONObject response = new JSONObject(text);

                status = response.getString("status");
                if (status.equals("success")){
                    output = new String[2];
                    output[0] = response.getString("user");
                    output[1] = response.getString("score");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (client != null) {
                    client.disconnect();
                }
                return null;
            } finally {
                if (client != null) {
                    client.disconnect();
                }
            }
            if (!status.equals("success")) {
                return null;
            }
            return output;
        }

        @Override
        protected void onPostExecute(String[] str) {
            if (str==null) {
                Toast.makeText(ScoreInfoDisplay.this, "Fetch Failed",Toast.LENGTH_SHORT).show();
            } else {
                mUsername.setText(str[0]);
                mTextview.setText(str[1]);
            }
        }
    }
}
