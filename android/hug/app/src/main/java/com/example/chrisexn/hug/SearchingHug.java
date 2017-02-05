package com.example.chrisexn.hug;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import static android.R.attr.password;

/**
 * Created by chrisexn on 2/4/2017.
 */

public class SearchingHug implements Runnable {

    private String mKey;
    private double mLat;
    private double mLong;
    private Handler mHandler;

    public SearchingHug(String auth_key, double latitude, double longitude, Handler handler){
        mKey = auth_key;
        mLat = latitude;
        mLong = longitude;
        mHandler = handler;
    }


    @Override
    public void run() {

        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("status","failed");
        msg.setData(bundle);

        String urlEndPoint = Constants.WEB_URL + "/api/v1/hugs/search" ;
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("latitude",mLat);
            credentials.put("longitude",mLong);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        //Run the api call
        HttpURLConnection client = null;
        try {
            URL url = new URL(urlEndPoint);
            client =(HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setConnectTimeout(10000);
            client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            client.setRequestProperty("Authorization", " Token " + mKey);
            client.setDoOutput(true);
            OutputStream os = client.getOutputStream();
            os.write(credentials.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            int responseCode = client.getResponseCode();
            if(responseCode!= 200){
                mHandler.sendMessage(msg);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String text = br.readLine();
            JSONObject response= new JSONObject(text);
            String status = response.getString("status");
            if(status.equals("wait")){
                /*bundle.putString("status","success");
                bundle.putDouble("latitude",mLat);
                bundle.putDouble("longitude",mLong);
                msg.setData(bundle);
                mHandler.sendMessage(msg);*/
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SearchingHug.this.run();
                    }
                }, 3000);
                return;
            }else if (status.equals("success")){
                bundle.putString("status","success");
                bundle.putDouble("latitude",response.getDouble("latitude"));
                bundle.putDouble("longitude",response.getDouble("longitude"));
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            if(client!=null){
                client.disconnect();
            }
        }finally {
            if(client!=null){
                client.disconnect();
            }
        }
        mHandler.sendMessage(msg);
    }

}
