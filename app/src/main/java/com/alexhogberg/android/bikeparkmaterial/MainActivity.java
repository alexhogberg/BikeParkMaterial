package com.alexhogberg.android.bikeparkmaterial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alexhogberg.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.Date;
import java.util.HashMap;

import gps.GPSListener;
import map.MapHelper;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int SETTINGS_RESULT = 1;
    private LocationManager mLocManager;
    private GPSListener mLocListener;
    private GoogleMap mMap;
    private Marker currentTargetMarker;
    private Marker currentPositionMarker;
    private Polyline mapLine;
    private MapHelper mH;
    private MainHelper mMainHelper;

    //Material Design specifics
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        //start helper class
        mMainHelper = new MainHelper(getApplicationContext());

        //Initialize application
        setUpDrawer();
        startAds();
        setUpMap();
        initialMapZoom();
        // If the user has closed the app with a present parking, load these
        // settings
        loadSettings();
        // If GPS is disabled, send warning
        if (!mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void setUpDrawer() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(mToolbar);
        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch(Exception e) {
            Log.e("ACTION_BAR", e.getMessage());
        }
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle= new ActionBarDrawerToggle(this, mDrawerLayout,mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Log.e("ITEM", "selected item: " + menuItem.getItemId());
                performAction(menuItem.getItemId());
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void performAction(int itemId) {
        switch(itemId) {
            case R.id.drawer_park:
                buildQuestionAutoOrManual();
                break;
            case R.id.drawer_find:
                putPositionMarker();
                break;
            case R.id.drawer_reset:
                buildDeleteMessageMarker();
                break;
            case R.id.drawer_settings:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(i, SETTINGS_RESULT);
            default:
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.e("PREF CHANGE", "The following preference was changed:" + key);
        if ("map_type".equals(key))
            mH.generateMapOptions();
    }

    private void startAds() {
        final LinearLayout lnrMain = (LinearLayout) findViewById(R.id.adLayout);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdView adView = new AdView(MainActivity.this);
                adView.setAdUnitId("ca-app-pub-9579903615710521/7532020690");
                adView.setAdSize(AdSize.SMART_BANNER);
                AdRequest.Builder builder = new AdRequest.Builder();
                builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                adView.loadAd(builder.build());
                lnrMain.addView(adView);
            }
        });
    }

    private void setUpMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        mH = new MapHelper(mMap, getApplicationContext());
        mH.generateMapOptions();

        //Connect to the GPS service
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new GPSListener(mMap, currentPositionMarker, currentTargetMarker, this);
        if (mMainHelper.hasCorrectPermissions()) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    mLocListener);
        }
    }

    private void loadSettings() {
        HashMap<String, Object> prefs = mMainHelper.getSavedPrefs();
        // Load the previous settings if they exist and add a marker
        if ((double) prefs.get("lat") != 0 && (double) prefs.get("lon") != 0) {
            setMarkerWithValues((double) prefs.get("lat"), (double) prefs.get("lon"),
                    new Date((long) prefs.get("date")).toString());
        }
    }

    /**
     * Creates a marker where the user is located and maps it towards the parked
     * position
     */
    private void putPositionMarker() {
        if (currentPositionMarker != null)
            currentPositionMarker.remove();
        if (mapLine != null)
            mapLine.remove();

        if (currentTargetMarker != null) {
            if (mMainHelper.hasCorrectPermissions()) {
                Location loc = mLocManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc != null) {

                    // Add a marker for the users current position
                    LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());

                    //Notify the listener of the new marker
                    MarkerOptions markerOptions = mH.createPositionMarker(pos, currentTargetMarker);
                    Marker newMarker = mMap.addMarker(markerOptions);

                    mLocListener.setCurrentPosition(newMarker);

                    mH.zoomTo(mLocListener.getCurrentPosition());
                }

            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.permission_required,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    R.string.no_previous_parkings,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set a marker at the given GPS coordinates
     */
    private void setMarkerFromPosition() {
        if (mMainHelper.hasCorrectPermissions()) {
            Location loc = mLocManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {

                mMap.clear();
                if (currentPositionMarker != null)
                    currentPositionMarker = null;
                LatLng currPos = new LatLng(loc.getLatitude(), loc.getLongitude());
                currentTargetMarker = mMap.addMarker(mH.createTargetMarker(currPos, null));

                //Send the current marker to the listener
                mLocListener.setCurrentTarget(currentTargetMarker);
                currentTargetMarker.showInfoWindow();
                mH.zoomTo(currentTargetMarker);
                mMainHelper.setSavedPrefs(loc.getLatitude(), loc.getLongitude());
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.gps_make_sure_enabled,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets a marker at a given latitude and longitude
     *
     * @param lat  current latitude
     * @param lon  current longitude
     * @param date the current date
     */
    private void setMarkerWithValues(double lat, double lon, String date) {
        mMap.clear();
        if (currentPositionMarker != null)
            currentPositionMarker = null;
        LatLng currPos = new LatLng(lat, lon);
        String title = R.string.last_parking + " " + date;
        currentTargetMarker = mMap.addMarker(mH.createTargetMarker(currPos, title));
        mLocListener.setCurrentTarget(currentTargetMarker);
        currentTargetMarker.showInfoWindow();
        mH.zoomTo(currentTargetMarker);
    }

    private void setManualTargetMarker(LatLng point) {
        mMap.clear();
        if (currentPositionMarker != null)
            currentPositionMarker = null;

        LatLng position = point;
        currentTargetMarker = mMap.addMarker(mH.createTargetMarker(position, null));

        //Send the current marker to the listener
        mLocListener.setCurrentTarget(currentTargetMarker);
        currentTargetMarker.showInfoWindow();
        mH.zoomTo(currentTargetMarker);
        mMainHelper.setSavedPrefs(position.latitude, position.longitude);

        mMap.setOnMapClickListener(null);
    }

    /**
     * Clear the map and remove any saved preferences
     */
    protected void clearMap() {
        mMap.clear();
        currentTargetMarker = null;
        currentPositionMarker = null;
        mapLine = null;
        mLocListener.setCurrentTarget(null);
        mLocListener.setCurrentPosition(null);
        mLocListener.clear();
        mMainHelper.removeSavedPrefs();
    }

    private void initialMapZoom() {
        if (mMainHelper.hasCorrectPermissions()) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    mLocListener);
            Location loc = mLocManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                mH.zoom(loc);
            }
        }
    }

    /**
     * An alert that is being displayed if you dont have GPS enabled
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                R.string.gps_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.global_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton(R.string.global_no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildDeleteMessageMarker() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_title_reset)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_reset,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                clearMap();
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildQuestionAutoOrManual() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_title_select_method)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_gps,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                setMarkerFromPosition();
                            }
                        })
                .setNegativeButton(R.string.dialog_manual,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                                    @Override
                                    public void onMapClick(LatLng point) {
                                        setManualTargetMarker(point);
                                    }
                                });

                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }
}