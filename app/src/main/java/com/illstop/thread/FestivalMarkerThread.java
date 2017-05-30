package com.illstop.thread;

import android.annotation.SuppressLint;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.illstop.activity.FestivalInfoDialog;
import com.illstop.activity.MainActivity;

import java.util.ArrayList;

import Definition.Festival;

public class FestivalMarkerThread implements Runnable {
    MainActivity mainActivity;
    private LatLng festivalPositionLatLng = null;
    private ArrayList<Festival> festivalItems;
    private double latitude, longitude;
    private GoogleMap googleMap;

    public FestivalMarkerThread(MainActivity mainActivity, GoogleMap googleMap, ArrayList<Festival> festivalItems, double latitude, double longitude) {
        this.mainActivity = mainActivity;
        this.googleMap = googleMap;
        this.festivalItems = festivalItems;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void showFestivalInfoDialog() {
        FestivalInfoDialog dialog = new FestivalInfoDialog();
        dialog.show(mainActivity.getFragmentManager(), "fragment_dialog_test");
    }

    @Override
    public void run() {
        mainActivity.runOnUiThread(new Runnable() {
            @SuppressLint("NewApi")
            public void run() {
                googleMap.clear();
                float[] results = new float[3];
                for (int i = 0; i < festivalItems.size(); i++) {
                    festivalPositionLatLng = new LatLng(festivalItems.get(i).getMapY(), festivalItems.get(i).getMapX());

                    Location.distanceBetween(latitude, longitude, festivalItems.get(i).getMapY(), festivalItems.get(i).getMapX(), results);

                    googleMap.addMarker(new MarkerOptions()
                            .position(festivalPositionLatLng)
                            .snippet(String.valueOf(festivalItems.get(i).getContentid())));
                }

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        mainActivity.contentId = Integer.parseInt(marker.getSnippet());
                        showFestivalInfoDialog();
                        return true;
                    }
                });
            }
        });
    }
}
