package com.alexhogberg.android.bikeparkmaterial;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.Console;
import java.util.Date;
import java.util.HashMap;

import gps.GPSListener;
import map.MapHelper;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private LocationManager mLocManager;
    private GPSListener mLocListener;
    private PackageManager pm;
    private GoogleMap mMap;
    private Marker currentTargetMarker;
    private Marker currentPositionMarker;
    private Polyline mapLine;
    private MapHelper mH;

    //Material Design specifics
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
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

        mH = new MapHelper(mMap);
        mH.generateMapOptions();

        //Connect to the GPS service
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new GPSListener(mMap, currentPositionMarker, currentTargetMarker, this);
        pm = getApplicationContext().getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                getApplicationContext().getPackageName());
        Log.e("PERMISSION", "You have permission: " + hasPerm);
        Log.e("PERMISSION", "You should have permission: " + PackageManager.PERMISSION_GRANTED);
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    mLocListener);
            Log.e("MAP", "Requesting updates!");
        }
    }

    private void loadSettings() {
        HashMap<String, Object> prefs = getSavedPrefs();
        // Load the previous settings if they exist and add a marker
        if ((double) prefs.get("lat") != 0 && (double) prefs.get("lon") != 0) {
            setMarkerWithValues((double) prefs.get("lat"), (double) prefs.get("lon"),
                    new Date((long) prefs.get("date")).toString());
        }

    }

    /**
     * Creates a marker where the user is located and maps it towards the parked
     * position
     *
     */
    private void putPositionMarker() {
        if (currentPositionMarker != null)
            currentPositionMarker.remove();
        if (mapLine != null)
            mapLine.remove();

        if (currentTargetMarker != null) {
            int hasPerm = pm.checkPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    getApplicationContext().getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
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
        int hasPerm = pm.checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                getApplicationContext().getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
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
                setSavedPrefs(loc.getLatitude(), loc.getLongitude());
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.gps_make_sure_enabled,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
        }

    }

    /**
     * Sets a marker at a given latitude and longitude
     *
     * @param lat
     *            current latitude
     * @param lon
     *            current longitude
     * @param date
     *            the current date
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
        setSavedPrefs(position.latitude, position.longitude);

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
        removeSavedPrefs();
    }

    private void initialMapZoom() {
        int hasPerm = pm.checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                getApplicationContext().getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    mLocListener);
            Location loc = mLocManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc != null) {
                mH.zoom(loc);
            }
        }


    }

    /**
     * Get the saved preferences from previous locations
     *
     * @return a hashmap containing where and when the last parking occured
     */
    private HashMap<String, Object> getSavedPrefs() {
        SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
        HashMap<String, Object> returnMap = new HashMap<String, Object>();

        returnMap.put("lat",
                Double.longBitsToDouble(settings.getLong("latitude", 0)));
        returnMap.put("lon",
                Double.longBitsToDouble(settings.getLong("longitude", 0)));
        returnMap.put("date", settings.getLong("date", 0));
        return returnMap;
    }

    /**
     * Saves the position in the local phone settings
     *
     * @param lat
     *            latitude to save
     * @param lon
     *            longitude to save
     */
    private void setSavedPrefs(double lat, double lon) {
        Date d = new Date();
        SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("latitude", Double.doubleToLongBits(lat));
        editor.putLong("longitude", Double.doubleToLongBits(lon));
        editor.putLong("date", d.getTime());

        // Commit the edits!
        editor.commit();
    }

    /**
     * Removes all saved position (used with the reset-button)
     */
    private void removeSavedPrefs() {
        SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        editor.commit();
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
    public void onDrawerItemSelected(View view, int position) {
        completeAction(position);
    }

    /**
     * Used for completing actions that are sent from the drawer
     * @param position
     */
    private void completeAction(int position) {
        switch (position) {
            case 0:
                buildQuestionAutoOrManual();
                break;
            case 1:
                putPositionMarker();
                break;
            case 2:
                buildDeleteMessageMarker();
                break;
            default:
                break;
        }
    }
}




