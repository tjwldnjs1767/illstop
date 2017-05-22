package com.illstop.listener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.illstop.activity.MainActivity;

import static android.content.Context.LOCATION_SERVICE;


public class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

    private MainActivity mainActivity;
    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000;
    private static final int UPDATE_SMALLEST_DISPLACEMENT = 2;

    public ConnectionCallbacks(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkLocationServicesStatus()) {
            showDialogForSetGps();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS)
                .setSmallestDisplacement(UPDATE_SMALLEST_DISPLACEMENT);

        LocationServices.FusedLocationApi.requestLocationUpdates(mainActivity.googleApiClient, locationRequest, mainActivity);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_NETWORK_LOST)
            Log.e("googlemap", "onConnectionSuspended(): Google Play services " +
                    "connection lost Cause: network lost.");
        else if (i == CAUSE_SERVICE_DISCONNECTED)
            Log.e("googlemap", "onConnectionSuspended():  Google Play services " +
                    "connection lost Cause: service disconnected");
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) mainActivity.getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void showDialogForSetGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mainActivity.startActivityForResult(callGPSSettingIntent, 2001);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                mainActivity.finish();
            }
        });
        builder.create().show();
    }
}
