package com.alexhogberg.android.bikeparkmaterial;

/**
 * Created by Alexander on 2015-09-14.
 */
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alexhogberg.android.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import gps.GPSListener;
import map.MapHelper;


public class CustomMapFragment extends Fragment {

    private MapView mapView;
    private GoogleMap map;
    private MapHelper mH;
    private Marker currentTargetMarker;
    private Marker currentPositionMarker;
    private MainHelper mMainHelper;
    private LocationManager mLocManager;
    private GPSListener mLocListener;

    public CustomMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mMainHelper = new MainHelper(getActivity().getApplicationContext());

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);

        MapsInitializer.initialize(this.getActivity());

        mH = new MapHelper(map, getActivity().getApplicationContext());
        mH.generateMapOptions();

        mLocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new GPSListener(map, currentPositionMarker, currentTargetMarker, getActivity().getApplicationContext());
        if (mMainHelper.hasCorrectPermissions()) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    mLocListener);
        }

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);

        return v;
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

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}