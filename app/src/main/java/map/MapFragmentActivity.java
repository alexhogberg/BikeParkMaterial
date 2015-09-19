package map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.alexhogberg.android.R;
import com.alexhogberg.android.bikeparkmaterial.MainHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import gps.GPSListener;
import map.MapHelper;

/**
 * Created by Alexander on 2015-09-14.
 */


public class MapFragmentActivity extends FragmentActivity implements OnMapReadyCallback {

    private LocationManager mLocManager;
    private GPSListener mLocListener;
    private Marker currentTargetMarker;
    private Marker currentPositionMarker;
    private GoogleMap mMap;
    private Polyline mapLine;
    private MapHelper mH;
    private MainHelper mMainHelper;

    public MapFragmentActivity() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);
        mMainHelper = new MainHelper(getApplicationContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpMap();

    }

    private void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

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
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mH = new MapHelper(map, getApplicationContext());
        mH.generateMapOptions();

        //Connect to the GPS service
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new GPSListener(map, currentPositionMarker, currentTargetMarker, this);
        if (mMainHelper.hasCorrectPermissions()) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    mLocListener);
        }
    }
}
