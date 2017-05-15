package com.illstop.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.illstop.R;
import com.illstop.data.DBManager;
import com.illstop.tourAPICall.LocationDataStore;
import com.illstop.tourAPICall.TourAPIThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Definition.Festival;


public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private LatLng festivalPositionLatLng = null;

    private TourAPIThread tourAPIThread = null;
    private Thread festivalMarkerThread = null;
    private ArrayList<Festival> festivalItems = null;

    private GoogleApiClient googleApiClient = null;
    private GoogleMap googleMap = null;

    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000;
    private static final int UPDATE_FESTIVAL_DISPLACEMENT = 2000;
    private static final int UPDATE_SMALLEST_DISPLACEMENT = 1;
    private final DBManager dbManager = new DBManager(this);

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private boolean locationChangeFirst = true;
    private int contentId = 0;
    private double latitude = 0.0, longitude = 0.0;

    Marker festivalPositionMarker = null;

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

        setCurrentLocation(null, 0.0, 0.0);

        googleMap.getUiSettings().setCompassEnabled(true);

        if (googleApiClient == null) {
            buildGoogleApiClient();
        }

        googleMap.setMyLocationEnabled(true);

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                // TODO: 2017-05-15 현재 위치로 이동 코드 작성
                LatLng currentLocation = new LatLng(latitude, longitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));

                return false;
            }
        });

        googleMap.getUiSettings().setZoomGesturesEnabled(true);
    }


    @Override
    public void onLocationChanged(Location location) {

        float[] results = new float[2];
        Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), results);

        setCurrentLocation(location, latitude, longitude);

        if (!locationChangeFirst) {
            if (results[0] >= UPDATE_FESTIVAL_DISPLACEMENT) {
                tourAPIThread();

                this.latitude = location.getLatitude();
                this.longitude = location.getLongitude();
            }
        } else {
            locationChangeFirst = false;

            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();

            tourAPIThread();
        }


    }


    private void tourAPIThread() {
        handlingGeocoder();

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
        festivalMarkerThread = new Thread(festivalMarkersMaker);
        festivalMarkerThread.start();

        try {
            festivalMarkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        festivalMarkerThread = null;
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
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS)
                .setSmallestDisplacement(UPDATE_SMALLEST_DISPLACEMENT);

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
                    "connection lost Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e("googlemap", "onConnectionSuspended():  Google Play services " +
                    "connection lost Cause: service disconnected");
    }


    public void setCurrentLocation(Location location, double prevLatitude, double prevLongitude) {
        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
            return;
        } else {
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
                    Toast.makeText(getApplicationContext(), "GPS 활성 여부를 확인하세요", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public int getContentId() {
        return contentId;
    }

    public ArrayList<Festival> getFestivalItems() {
        return festivalItems;
    }

    public void showFestivalInfoDialog() {
        FestivalInfoDialog dialog = new FestivalInfoDialog();
        dialog.show(getFragmentManager(), "fragment_dialog_test");
    }

    public void handlingGeocoder() {

        Geocoder geocoder = new Geocoder(this);

        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(
                    this.latitude,
                    this.longitude,
                    1);
        } catch (IOException ioException) {
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();

        } else {
            LocationDataStore locationDataStore = new LocationDataStore();
            locationDataStore.setDbManager(dbManager);
            locationDataStore.setLocationName(addresses.get(0).getAdminArea());
            locationDataStore.setLocality(addresses.get(0).getLocality());
        }

    }


    public class FestivalMarkersMaker implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                public void run() {
                    float[] results = new float[3];
                    for (int i = 0; i < festivalItems.size(); i++) {
                        festivalPositionLatLng = new LatLng(festivalItems.get(i).getMapY(), festivalItems.get(i).getMapX());

                        // TODO: 2017-05-10 거리 초기화 및 다이얼로그에 찍기
                        Location.distanceBetween(latitude, longitude, festivalItems.get(i).getMapY(), festivalItems.get(i).getMapX(), results);

                        festivalPositionMarker = googleMap.addMarker(new MarkerOptions()
                                .position(festivalPositionLatLng)
                                .snippet(String.valueOf(festivalItems.get(i).getContentid())));
                    }

                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            contentId = Integer.parseInt(marker.getSnippet());
                            showFestivalInfoDialog();
                            return true;
                        }
                    });
                }
            });
        }
    }
}