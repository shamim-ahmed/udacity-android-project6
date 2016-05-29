package com.example.android.sunshine.app.wearable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.android.sunshine.app.SunshineApplication;
import com.example.android.sunshine.app.util.MobileConstants;

/**
 * Created by shamim on 5/15/16.
 */
public class DataLayerListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = DataLayerListenerService.class.getSimpleName();

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        Log.i(TAG, "DataLayerListenerService initialized successfully");
    }

    @Override
    public void onDestroy() {
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected: Successfully connected to Google API client");
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "onConnectionSuspended: Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed: Failed to connect, with result: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();

                if (MobileConstants.GET_FORECAST_DATA_PATH.equals(path)) {
                    Log.i(TAG, String.format("Data Changed for path: %s", MobileConstants.GET_FORECAST_DATA_PATH));

                    // reset last forecast data
                    SunshineApplication application = (SunshineApplication) getApplication();
                    application.setLastForecastData(null);

                    // now send the latest forecast data to wearable
                    Context appContext = getApplicationContext();
                    Intent service = new Intent(appContext, NotifyWearableService.class);
                    appContext.startService(service);
                }
            }
        }
    }
}
