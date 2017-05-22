package com.illstop.data;

import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.illstop.activity.MainActivity;

import java.io.IOException;
import java.util.List;

/**
 * Created by dsm2015 on 2017-05-22.
 */

public class GeoCoderConverter {
    private MainActivity mainActivity;
    private double latitude, longitude;

    public GeoCoderConverter(MainActivity mainActivity, double latitude, double longitude) {
        this.mainActivity = mainActivity;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public void handlingGeocoder() {

        Geocoder geocoder = new Geocoder(mainActivity);

        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(
                    this.latitude,
                    this.longitude,
                    1);
        } catch (IOException ioException) {
            Toast.makeText(mainActivity, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(mainActivity, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(mainActivity, "주소 미발견", Toast.LENGTH_LONG).show();

        } else {
            final DBManager dbManager = new DBManager(mainActivity);
            LocationDataStore locationDataStore = new LocationDataStore();
            locationDataStore.setDbManager(dbManager);
            locationDataStore.setLocationName(addresses.get(0).getAdminArea());
            locationDataStore.setLocality(addresses.get(0).getLocality());
        }

    }
}
