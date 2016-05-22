package com.example.android.sunshine.app.util;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.os.Bundle;
import android.util.Log;

/**
 * Created by shamim on 5/22/16.
 */
public class CustomConnectionCallbackListener implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = CustomConnectionCallbackListener.class.getSimpleName();

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connection to Google API has been established successfully");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection to Google API has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection to Google API failed with result: " + connectionResult);
    }
}
