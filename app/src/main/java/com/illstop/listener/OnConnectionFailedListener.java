package com.illstop.listener;

import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.illstop.activity.MainActivity;

/**
 * Created by dsm2015 on 2017-05-22.
 */

public class OnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
    private MainActivity mainActivity;

    public OnConnectionFailedListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mainActivity.finish();
    }
}