package com.example.chrisexn.hug.session;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.chrisexn.hug.Constants;
import com.example.chrisexn.hug.MainActivity;
import com.example.chrisexn.hug.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Christophe Gaboury on 16/09/2016. Copyright (c) 2016
 */
public class LoginActivity extends AppCompatActivity {

    private EditText mUserNameField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private TextView mRegisterButton;
    private TextView mErrorText;
    private ProgressBar mLoadingIcon;
    private ProgressBar mBigLoadingIcon;


    @Override
    public void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);

        //Set the UI
        setContentView(R.layout.activity_login);
        mUserNameField = (EditText) findViewById(R.id.username_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);
        mPasswordField.setTypeface(Typeface.DEFAULT);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mRegisterButton = (TextView) findViewById(R.id.register_button);
        mErrorText = (TextView) findViewById(R.id.error_text);
        mLoadingIcon = (ProgressBar) findViewById(R.id.loading_icon);
        mBigLoadingIcon = (ProgressBar) findViewById(R.id.bigLoadingIcon);

        //Allows the user to register when they are done typing
        mPasswordField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onLogin(null);
                }
                return false;
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        //Tests to see if they registered already
        startCheckAnim();
        new TestLogin().execute(Constants.getToken(this));

    }
    /**
     * Function that triggers when a user has logged in
     */
    public void onLogin(View v) {
        // Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (!networkInfo.isConnected()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String username = mUserNameField.getText().toString();
        String password = mPasswordField.getText().toString();
        if (username.equals("")) {
            mErrorText.setText(R.string.error_text);
            mErrorText.setVisibility(View.VISIBLE);
            return;
        }
        if (password.equals("")) {
            mErrorText.setText(R.string.error_text);
            mErrorText.setVisibility(View.VISIBLE);
            return;
        }
        mErrorText.setVisibility(View.INVISIBLE);
        startLoading();
        //Request a login
        new LoginAndGetSession().execute(username,password);
    }

    /**
     * Display an error message
     */
    public void displayErrorMessage() {
        mErrorText.setVisibility(View.VISIBLE);

    }

    /**
     * Start a login network call
     */
    private class LoginAndGetSession extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String ... str){
            String username = str[0];
            String password = str[1];
            String sessionID = "";
            String status = "";
            String urlEndPoint = Constants.WEB_URL + "/api/v1/auth/login" ;
            JSONObject credentials = new JSONObject();
            try {
                credentials.put("username",username);
                credentials.put("password",password);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            //Run the api call
            HttpURLConnection client = null;
            try {
                URL url = new URL(urlEndPoint);
                client =(HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setConnectTimeout(10000);
                client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                client.setDoOutput(true);
                OutputStream os = client.getOutputStream();
                os.write(credentials.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                if(client.getResponseCode()!= 200){
                    return false;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String text = br.readLine();
                JSONObject response= new JSONObject(text);
                status = response.getString("status");
                if(!status.equals("success")){
                    return false;
                }
                sessionID = response.getString("token");
            }catch (Exception e){
                e.printStackTrace();
                if(client!=null){
                    client.disconnect();
                }
                return false;
            }finally {
                if(client!=null){
                    client.disconnect();
                }
            }

            //Write to shared memory
            //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            //SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putString(getString(R.string.session_id), sessionID);
            //editor.apply();
            Constants.updateAccount(getBaseContext(),sessionID);



            return true;
        }
        @Override
        protected void onPostExecute(Boolean bool){
            if(bool){
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);

                //Toast.makeText(getApplicationContext(), "Test ", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0, 0);
            }else{
                displayErrorMessage();
                stopLoading();
            }
        }
    }


    /**
     * Check login token already
     */
    private class TestLogin extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String ... str){
            String token = str[0];
            String status = "";
            String sessionID = "";
            String urlEndPoint = Constants.WEB_URL + "/api/v1/auth/token" ;

            //Run the api call
            HttpURLConnection client = null;
            try {
                URL url = new URL(urlEndPoint);
                client =(HttpURLConnection) url.openConnection();
                client.setRequestMethod("GET");
                client.setConnectTimeout(10000);
                client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                client.setRequestProperty("Authorization", " Token " + token);
                client.connect();
                int t  =client.getResponseCode();
                if(t!= 200){
                    return false;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String text = br.readLine();
                JSONObject response= new JSONObject(text);

                status = response.getString("status");
                sessionID = response.getString("token");
            }catch (Exception e){
                e.printStackTrace();
                if(client!=null){
                    client.disconnect();
                }
                return false;
            }finally {
                if(client!=null){
                    client.disconnect();
                }
            }

            if(!status.equals("success")){
                return false;
            }

            //Write to shared memory
            //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            //SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putString(getString(R.string.session_id), sessionID);
            //editor.apply();
            Constants.updateAccount(getBaseContext(),sessionID);
            return true;
        }
        @Override
        protected void onPostExecute(Boolean bool){
            if(bool){
                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);

                //Toast.makeText(getApplicationContext(), "Test 1", Toast.LENGTH_SHORT).show();
            }else{
                startLogin();
                //displayErrorMessage();
                //stopLoading();
            }
        }
    }

    /**
     * Function that triggers when a user has tried registering
     * Start a new activity?
     *
     * @param v
     */
    public void onRegister(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }


    public void startCheckAnim(){
        mBigLoadingIcon.setVisibility(View.VISIBLE);
        mLoadingIcon.setVisibility(View.GONE);
        mErrorText.setVisibility(View.INVISIBLE);
        mUserNameField.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.GONE);
        mPasswordField.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.GONE);
        mUserNameField.setEnabled(true);
        mPasswordField.setEnabled(true);
        mRegisterButton.setEnabled(true);
    }

    public void startLogin(){
        mBigLoadingIcon.setVisibility(View.GONE);
        mLoadingIcon.setVisibility(View.GONE);
        mErrorText.setVisibility(View.INVISIBLE);
        mUserNameField.setVisibility(View.VISIBLE);
        mLoginButton.setVisibility(View.VISIBLE);
        mPasswordField.setVisibility(View.VISIBLE);
        mRegisterButton.setVisibility(View.VISIBLE);
        mUserNameField.setEnabled(true);
        mPasswordField.setEnabled(true);
        mRegisterButton.setEnabled(true);

    }
    public void startLoading() {
        mLoadingIcon.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.INVISIBLE);
        mLoginButton.setVisibility(View.GONE);
        mUserNameField.setEnabled(false);
        mPasswordField.setEnabled(false);
        mRegisterButton.setEnabled(false);
    }
    public void stopLoading() {
        mLoadingIcon.setVisibility(View.GONE);
        mErrorText.setVisibility(View.VISIBLE);
        mLoginButton.setVisibility(View.VISIBLE);
        mUserNameField.setEnabled(true);
        mPasswordField.setEnabled(true);
        mRegisterButton.setEnabled(true);
    }

}