package com.example.chrisexn.hug;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chrisexn on 2/4/2017.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private GoogleMap mMap;
    private ImageView mMainButton;
    private ProgressBar mProgress;
    private DrawerAdapter mAdapter;
    private Handler mHandler;

    private ImageView mLove;
    private ImageView mClock;

    private Location mLocation;
    private LocationManager mLocationManager;
    static final int TWO_MINUTES = 1000 * 60 * 2;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        /**
         * INIT SPINNER
         */

        mProgress = (ProgressBar) findViewById(R.id.spinner);
        mLove = (ImageView) findViewById(R.id.love);
        mClock = (ImageView) findViewById(R.id.clock);

        int progressWidth = mProgress.getLayoutParams().width;
        int progressHeight = mProgress.getLayoutParams().height;
        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(progressWidth, progressHeight);
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
            mMainButton = (ImageView) ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            int positionWidth = mMainButton.getLayoutParams().width;
            int positionHeight = mMainButton.getLayoutParams().height;

            //lay out position button
            RelativeLayout.LayoutParams positionParams = new RelativeLayout.LayoutParams(positionWidth, positionHeight);
            positionParams.setMargins(0, 0, 0, 100);
            positionParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            positionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            positionParams.height = 250;
            positionParams.width = 250;
            mMainButton.setLayoutParams(positionParams);
            mMainButton.setImageResource(R.mipmap.ic_logo_outline);

        }

        mHandler = new HandlerExtension(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    public void zoomCurrentLocation() {

        //Request for permission
        if ((ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {

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
            LocationListener locationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    //Handle drawer click
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mTitles[position].equals("Logout")) {
                toggleLoading(true);
                new Logout().execute(Constants.getToken(MainActivity.this));
            }
        }
    }

    //Handle button click
    @Override
    public boolean onMyLocationButtonClick() {
        //Start Search for thread
        Location loc = getLastBestLocation();
        if (loc != null && mHandler != null) {
            toggleLoading(true);
            Thread thread = new Thread(new SearchingHug(Constants.getToken(this), loc.getLatitude(), loc.getLongitude(), mHandler));
            thread.start();
        } else {
            makeToast("Location Not Found");
        }
        return false;
    }

    private static class HandlerExtension extends Handler {

        private final WeakReference<MainActivity> currentActivity;

        public HandlerExtension(MainActivity activity) {
            currentActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            MainActivity activity = currentActivity.get();
            if (activity != null) {
                // Do Something to activity
                Bundle bundle = message.getData();
                if (bundle != null) {
                    if (bundle.getString("status").equals("failed")) {
                        activity.makeToast("Error Searching");
                        activity.toggleLoading(false);
                    }
                    if (bundle.getString("status").equals("success")) {
                        activity.makeToast("Found Match");
                        activity.setMarker(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
                        activity.toggleButtons(true);
                        //activity.toggleLoading(false);

                    }
                }
                //activity.updateResults(message.getData().getString("result"));
            }
        }
    }

    public void toggleLoading(boolean toggle) {
        if (mMainButton == null || mProgress == null) {
            return;
        }
        if (toggle) {
            mMainButton.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
        } else {
            mMainButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }
    }

    public void toggleButtons(boolean toggle) {
        if (mMainButton == null || mProgress == null || mLove == null || mClock == null) {
            return;
        }
        if (toggle) {
            mLove.setVisibility(View.VISIBLE);
            mClock.setVisibility(View.VISIBLE);
            mMainButton.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        } else {
            mLove.setVisibility(View.GONE);
            mClock.setVisibility(View.GONE);
            mMainButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }
    }

    public void makeToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 500);
        toast.show();
    }

    public void setMarker(Double lat, Double lng) {
        mMap.clear();
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).draggable(true).title("Huggie!"));
    }

    /**
     * Location stff
     */

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            makeUseOfNewLocation(location);

            if (mLocation == null) {
                mLocation = location;
            }

        }


        /**
         * This method modify the last know good location according to the arguments.
         *
         * @param location The possible new location.
         */
        void makeUseOfNewLocation(Location location) {
            if (isBetterLocation(location, mLocation)) {
                mLocation = location;
            }
        }

        /**
         * Determines whether one location reading is better than the current location fix
         *
         * @param location            The new location that you want to evaluate
         * @param currentBestLocation The current location fix, to which you want to compare the new one.
         */
        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location,
            // because the user has likely moved.
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse.
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /**
         * Checks whether two providers are the same
         */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }


        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private Location getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return null;
        }
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    /**
     * Logout
     */
    private class Logout extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... str) {
            String token = str[0];
            String status = "";
            String urlEndPoint = Constants.WEB_URL + "/api/v1/auth/logout";

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
                    return false;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String text = br.readLine();
                JSONObject response = new JSONObject(text);

                status = response.getString("status");
            } catch (Exception e) {
                e.printStackTrace();
                if (client != null) {
                    client.disconnect();
                }
                return false;
            } finally {
                if (client != null) {
                    client.disconnect();
                }
            }
            if (!status.equals("success")) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Logout Unsuccessful", Toast.LENGTH_SHORT).show();
                toggleLoading(false);
            }
        }
    }

}
