package com.alexhogberg.android.bikeparkmaterial;

import android.app.Activity;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import com.alexhogberg.android.bikeparkmaterial.*;

import com.alexhogberg.android.bikeparkmaterial.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public class MainActivity extends Activity {

    private LocationManager mlocManager;
    private GPSListener mlocListener;
    private GoogleMap mMap;
    private Button parkButton;
    private Button findButton;
    private Button resetButton;
    private Marker currentTargetMarker;
    private Marker currentPositionMarker;
    private Polyline mapLine;
    private MapHelper mH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LinearLayout lnrMain = (LinearLayout) findViewById(R.id.adLayout);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdView adView = new AdView(MainActivity.this);
                adView.setAdUnitId("ca-app-pub-9579903615710521/7532020690");
                adView.setAdSize(AdSize.BANNER);
                AdRequest.Builder builder = new AdRequest.Builder();
                builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                adView.loadAd(builder.build());
                lnrMain.addView(adView);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
