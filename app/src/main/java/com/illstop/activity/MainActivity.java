package com.illstop.activity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import com.illstop.R;
import com.illstop.data.GeoCoderConverter;
import com.illstop.listener.GoogleApiClientConnectionCallbacks;
import com.illstop.listener.OnConnectionFailedListener;
import com.illstop.thread.FestivalMarkerThread;
import com.illstop.tourAPICall.TourAPIThread;

import java.util.ArrayList;

import Definition.Festival;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private ArrayList<Festival> festivalItems = null;

    public GoogleApiClient googleApiClient = null;
    public GoogleMap googleMap = null;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private boolean locationChangeFirst = true;
    public int contentId = 0;
    private double latitude = 0.0, longitude = 0.0;
    private GoogleApiClientConnectionCallbacks callbacks;
    private OnConnectionFailedListener connectionFailedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_maps);


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onPause() {
        if (googleApiClient != null && googleApiClient.isConnected()) {

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

            googleApiClient.disconnect();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (googleApiClient != null) {
            googleApiClient.unregisterConnectionCallbacks(callbacks);
            googleApiClient.unregisterConnectionFailedListener(connectionFailedListener);

            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
        }

        super.onDestroy();
    }


    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;
        setCurrentLocation(null);

        googleMap.getUiSettings().setCompassEnabled(true);

        if (googleApiClient == null) {
            buildGoogleApiClient();
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            LatLng currentLocation = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
            return false;
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        int UPDATE_FESTIVAL_DISPLACEMENT = 2000;
        float[] results = new float[2];
        Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), results);

        setCurrentLocation(location);

        if (!locationChangeFirst) {
            if (results[0] >= UPDATE_FESTIVAL_DISPLACEMENT) {
                callTourAPIThread();

                this.latitude = location.getLatitude();
                this.longitude = location.getLongitude();
            }
        } else {
            locationChangeFirst = false;

            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();

            callTourAPIThread();
        }
    }

    private void callTourAPIThread() {
        GeoCoderConverter geoCoderConverter = new GeoCoderConverter(this, latitude, longitude);
        geoCoderConverter.handlingGeocoder();

        TourAPIThread tourAPIThread = new TourAPIThread();
        tourAPIThread.start();

        try {
            tourAPIThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        festivalItems = tourAPIThread.getNearFestivals();

        FestivalMarkerThread festivalMarkersMakerThread = new FestivalMarkerThread(this, festivalItems, latitude, longitude);
        Thread festivalMarkerThread = new Thread(festivalMarkersMakerThread);
        festivalMarkerThread.start();

        try {
            festivalMarkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        callbacks = new GoogleApiClientConnectionCallbacks(this);
        connectionFailedListener = new OnConnectionFailedListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(Location location) {
        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
            return;

        } else
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 20));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 2001:
                if (checkLocationServicesStatus()) {

                    if (googleApiClient == null)
                        buildGoogleApiClient();

                    googleMap.setMyLocationEnabled(true);
                    return;
                } else
                    Toast.makeText(getApplicationContext(), "GPS 활성 여부를 확인하세요", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    public int getContentId() {
        return contentId;
    }

    public ArrayList<Festival> getFestivalItems() {
        return festivalItems;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}