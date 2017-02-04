package com.example.chrisexn.hug;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.Manifest;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.chrisexn.hug.session.LoginActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chrisexn on 2/4/2017.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener {

    private String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private GoogleMap mMap;
    private ImageView mMainButton;
    private ProgressBar mProgress;
    private DrawerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        /**
         * INITIALIZE DRAWER
         */
        mTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        // Set the adapter for the list view
        mAdapter = new DrawerAdapter(this, mTitles);
        mDrawerList.setAdapter(mAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        /**
         * INIT SPINNER
         */

        mProgress = (ProgressBar)findViewById(R.id.spinner);

        int progressWidth = mProgress.getLayoutParams().width;
        int progressHeight = mProgress.getLayoutParams().height;
        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(progressWidth,progressHeight);
        progressParams.setMargins(0, 0, 0, 100);
        progressParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        progressParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        progressParams.height = 250;
        progressParams.width = 250;
        mProgress.setLayoutParams(progressParams);

        /**
         * INIT MAP
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Get the button view
        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            mMainButton = (ImageView)((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            int positionWidth = mMainButton.getLayoutParams().width;
            int positionHeight = mMainButton.getLayoutParams().height;

            //lay out position button
            RelativeLayout.LayoutParams positionParams = new RelativeLayout.LayoutParams(positionWidth,positionHeight);
            positionParams.setMargins(0, 0, 0, 100);
            positionParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            positionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            positionParams.height = 250;
            positionParams.width = 250;
            mMainButton.setLayoutParams(positionParams);
            mMainButton.setImageResource(R.mipmap.ic_logo_outline);

        }

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        zoomCurrentLocation();
    }
    public void zoomCurrentLocation(){

        //Request for permission
        if ((ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }else {

            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                    // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                //mMap.addMarker(new MarkerOptions().position(
                //        new LatLng(location.getLatitude(), location.getLongitude())).title("Current Location"));

            }
            mMap.setOnMyLocationButtonClickListener(this);
        }

    }

    //Handle drawer click
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mTitles[position].equals("Logout")){
                toggleLoading(true);
                new Logout().execute(Constants.getToken(MainActivity.this));
            }
        }
    }
    //Handle button click
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    public void toggleLoading(boolean toggle){
        if(mMainButton == null || mProgress==null){
            return;
        }
        if(toggle){
            mMainButton.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
        }else{
            mMainButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }
    }

    /**
     * Logout
     */
    private class Logout extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String ... str){
            String token = str[0];
            String status = "";
            String urlEndPoint = Constants.WEB_URL + "/api/v1/auth/logout" ;

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
            }catch (Exception e){
                e.printStackTrace();
                if(client!=null){
                    client.disconnect();
                }
                return false;
            }finally {
                if (client != null) {
                    client.disconnect();
                }
            }
            if(!status.equals("success")){
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean bool){
            if(bool){
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();

                //Toast.makeText(getApplicationContext(), "Test 1", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Logout Unsuccessful", Toast.LENGTH_SHORT).show();
                toggleLoading(false);
            }
        }
    }

}
