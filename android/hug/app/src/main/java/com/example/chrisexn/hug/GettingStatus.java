package com.example.chrisexn.hug;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chrisexn on 2/4/2017.
 */

public class GettingStatus implements Runnable {
    private String mkey;
    private Handler mHandler;

    public GettingStatus(String key, Handler handler){
        mHandler = handler;
        mkey = key;
    }


    @Override
    public void run() {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("status","failed");
        msg.setData(bundle);
        String status;
        String urlEndPoint = Constants.WEB_URL + "/api/v1/hugs/status" ;
        //Run the api call
        HttpURLConnection client = null;
        try {
            URL url = new URL(urlEndPoint);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.setConnectTimeout(10000);
            client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            client.setRequestProperty("Authorization", " Token " + mkey);
            client.connect();
            int t = client.getResponseCode();
            if (t != 200) {
                mHandler.sendMessage(msg);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        GettingStatus.this.run();
                    }
                }, 3000);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String text = br.readLine();
            JSONObject response = new JSONObject(text);

            status = response.getString("status");

            if (status.equals("success")) {
                bundle.putString("status","success");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                return;
            } else if (status.equals("wait")) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        GettingStatus.this.run();
                    }
                }, 3000);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (client != null) {
                client.disconnect();
            }
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
        mHandler.sendMessage(msg);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                GettingStatus.this.run();
            }
        }, 3000);
    }
}
