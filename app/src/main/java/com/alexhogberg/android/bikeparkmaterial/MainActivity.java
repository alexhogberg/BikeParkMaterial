package com.alexhogberg.android.bikeparkmaterial;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.alexhogberg.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_RESULT = 1;
    //Material Design specifics
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //start helper class

        //Initialize application
        setUpDrawer();
        startAds();
        //setUpMap();
        //initialMapZoom();
        // If the user has closed the app with a present parking, load these
        // settings
        //loadSettings();
        // If GPS is disabled, send warning
        //if (!mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        //    buildAlertMessageNoGps();
        //}
    }

    private void startAds() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
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
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch(itemId) {
            case R.id.drawer_home:
                fragment = new CustomMapFragment();
                title = getString(R.string.nav_item_map);
                break;
            case R.id.drawer_park:
                fragment = new ParkingFragment();
                title = getString(R.string.nav_item_map);
                break;
            case R.id.drawer_find:
                break;
            case R.id.drawer_reset:
                break;
            case R.id.drawer_settings:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(i, SETTINGS_RESULT);
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
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