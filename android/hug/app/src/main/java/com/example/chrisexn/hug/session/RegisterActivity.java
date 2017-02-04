package com.example.chrisexn.hug.session;

import android.content.Context;
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
import com.example.chrisexn.hug.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Christophe Gaboury on 18/09/2016. Copyright (c) 2016
 */
public class RegisterActivity extends AppCompatActivity {


    private EditText mUsernameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private TextView mErrorText;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;



    @Override
    public void onCreate(Bundle SavedInstances){
        super.onCreate(SavedInstances);
        setContentView(R.layout.activity_register);

        mUsernameField = (EditText)findViewById(R.id.username_field);
        mEmailField = (EditText)findViewById(R.id.email_field);
        mPasswordField = (EditText)findViewById(R.id.password_field);
        mPasswordField.setTypeface(Typeface.DEFAULT);
        mConfirmPasswordField = (EditText)findViewById(R.id.password_confirm_field);
        mConfirmPasswordField.setTypeface(Typeface.DEFAULT);
        mErrorText = (TextView)findViewById(R.id.error_text);
        mRegisterButton = (Button)findViewById(R.id.register_button);
        mProgressBar = (ProgressBar)findViewById(R.id.loading_icon);
        mConfirmPasswordField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onRegister(null);
                }
                return false;
            }
        });

    }


    /**
     * Display an error message
     */
    public void displayErrorMessage(){
        mErrorText.setText(R.string.register_error);
        mErrorText.setVisibility(View.VISIBLE);
    }
    public void displayErrorMessage(String message){
        mErrorText.setText(message);
        mErrorText.setVisibility(View.VISIBLE);
    }
    public void onRegister(View v){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (!networkInfo.isConnected()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if(mUsernameField.getText().toString().equals("")){
            mErrorText.setText(R.string.invalid_username);
            mErrorText.setVisibility(View.VISIBLE);
            return;
        }
        if(mEmailField.getText().toString().equals("")){
            mErrorText.setText(R.string.invalid_email);
            mErrorText.setVisibility(View.VISIBLE);
            return;
        }
        if(mPasswordField.getText().toString().equals("")){
            mErrorText.setText(R.string.invalid_password);
            mErrorText.setVisibility(View.VISIBLE);
            return;
        }
        if(!mConfirmPasswordField.getText().toString().equals(mPasswordField.getText().toString())){
            mErrorText.setText(R.string.invalid_password_confirm);
            mErrorText.setVisibility(View.VISIBLE);
            return;
        }
        startLoading();
        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();
        String email = mEmailField.getText().toString();
        new LoginAndGetSession().execute(username,password,email);
    }

    /**
     * Start a login network call
     */
    private class LoginAndGetSession extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String ... str){
            String username = str[0];
            String password = str[1];
            String email = str[2];
            String sessionID = "";
            String status = "";
            String urlEndPoint = Constants.WEB_URL + "/api/v1/auth/register" ;
            JSONObject credentials = new JSONObject();
            try {
                credentials.put("username",username);
                credentials.put("password",password);
                credentials.put("email",email);
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
            //Run the api call
            HttpURLConnection client = null;
            try {
                URL url = new URL(urlEndPoint);
                client =(HttpURLConnection) url.openConnection();
                client.setConnectTimeout(10000);
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                client.setDoOutput(true);
                OutputStream os = client.getOutputStream();
                os.write(credentials.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                if(client.getResponseCode()!= 200){
                    return "";
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String text = br.readLine();
                JSONObject response= new JSONObject(text);

                status = response.getString("status");
                if (!status.equals("success")){
                    return   response.getString("message");
                }
                sessionID = response.getString("token");
            }catch (Exception e){
                e.printStackTrace();
                if(client!=null){
                    client.disconnect();
                }
                return "";
            }finally {
                if(client!=null){
                    client.disconnect();
                }
            }


            //Register to shared memeory
            Constants.updateAccount(getBaseContext(),sessionID);
            return "success";
        }
        @Override
        protected void onPostExecute(String output){
            if(output.equals("success")){
                Toast.makeText(RegisterActivity.this,"Account Registered",Toast.LENGTH_LONG).show();
                finish();
            }else{
                if(!output.equals("")){
                    displayErrorMessage(output);
                }else {
                    displayErrorMessage();
                }
                stopLoading();
            }
        }
    }

    public void startLoading(){
        mErrorText.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRegisterButton.setVisibility(View.GONE);
        mUsernameField.setEnabled(false);
        mPasswordField.setEnabled(false);
        mEmailField.setEnabled(false);
        mConfirmPasswordField.setEnabled(false);
    }
    public void stopLoading(){
        mErrorText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.VISIBLE);
        mUsernameField.setEnabled(true);
        mPasswordField.setEnabled(true);
        mEmailField.setEnabled(true);
        mConfirmPasswordField.setEnabled(true);
    }

}