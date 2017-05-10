package com.illstop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import Definition.Festival;
import TourAPICall.TourAPIThread;


public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    LatLng festivalPositionLatLng;

    TourAPIThread tourAPIThread;
    ArrayList<Festival> festivalItems;

    private GoogleApiClient googleApiClient = null;
    private GoogleMap googleMap = null;
    Marker currentMarker, festivalPositionMarker;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int UPDATE_INTERVAL_MS = 5000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 5000;
    private static final int UPDATE_SMALLEST_DISPLACEMENT = 2000;

    int contentId = 0;

    private boolean connectFirst = true;

    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_maps);


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
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
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);

            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi
                        .removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
        }

        super.onDestroy();
    }


    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;

        setCurrentLocation(null, "", "");

        googleMap.getUiSettings().setCompassEnabled(true);

        if (googleApiClient == null) {
            buildGoogleApiClient();
        }

        googleMap.setMyLocationEnabled(true);

        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        tourAPIThread = new TourAPIThread();
        tourAPIThread.start();

        try {
            tourAPIThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        festivalItems = tourAPIThread.getNearFestivals();

        tourAPIThread = null;

        FestivalMarkersMaker festivalMarkersMaker = new FestivalMarkersMaker();
        Thread t = new Thread(festivalMarkersMaker);
        t.start();
    }


    @Override
    public void onLocationChanged(Location location) {

        float[] arr = new float[5];
        Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), arr);
        Toast.makeText(getApplicationContext(), String.valueOf(arr[0]), Toast.LENGTH_SHORT).show();

        if (arr[0] >= UPDATE_SMALLEST_DISPLACEMENT) {
            // TODO: 2017-05-10 2km 넘었을 때 정태균 api 호출
            tourAPIThread = new TourAPIThread();
            tourAPIThread.start();

            try {
                tourAPIThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

            festivalItems = tourAPIThread.getNearFestivals();

            tourAPIThread = null;
        }

        String markerTitle = "현재 위치";
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                + " 경도:" + String.valueOf(location.getLongitude());

        setCurrentLocation(location, markerTitle, markerSnippet);

        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }


    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        if (!checkLocationServicesStatus()) {
            showDialogForSetGps();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        finish();
    }

    public void showDialogForSetGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, 2001);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            Log.e("googlemap", "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e("googlemap", "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();


        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation)
                    .title(markerTitle)
                    .snippet(markerSnippet)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            currentMarker = googleMap.addMarker(markerOptions);

            if (connectFirst) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
                connectFirst = false;
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            }

            return;

        } else {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(DEFAULT_LOCATION)
                    .title(markerTitle)
                    .snippet(markerSnippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            currentMarker = googleMap.addMarker(markerOptions);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 20));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 2001:
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }

                        if (ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            googleMap.setMyLocationEnabled(true);
                        }

                        return;
                    }
                } else {
                    setCurrentLocation(null, "위치정보 가져올 수 없음",
                            "GPS 활성 여부를 확인하세요");
                }

                break;
        }
    }

    public void showFestivalInfoDialog() {
        FestivalInfoDialog dialog = new FestivalInfoDialog();
        dialog.show(getFragmentManager(), "fragment_dialog_test");
    }

    public void notiNewFestival() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(3000);
    }

    public class FestivalMarkersMaker implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                public void run() {
                    float[] arr = new float[5];
                    for (int i = 0; i < festivalItems.size(); i++) {
                        festivalPositionLatLng = new LatLng(festivalItems.get(i).getMapY(), festivalItems.get(i).getMapX());

                        // TODO: 2017-05-10 거리 초기화 및 다이얼로그에 찍기
                        Location.distanceBetween(latitude, longitude, festivalItems.get(i).getMapY(), festivalItems.get(i).getMapX(), arr);

                        festivalPositionMarker = googleMap.addMarker(new MarkerOptions()
                                .position(festivalPositionLatLng)
                                .snippet(String.valueOf(festivalItems.get(i).getContentid())));
                    }

                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            if (!marker.equals(currentMarker)) {
                                contentId = Integer.parseInt(marker.getSnippet());
                                showFestivalInfoDialog();
                            }
                            return true;
                        }
                    });
                }
            });
        }
    }
}