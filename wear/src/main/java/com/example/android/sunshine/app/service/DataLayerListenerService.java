package com.example.android.sunshine.app.service;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.os.Bundle;
import android.util.Log;

import com.example.android.sunshine.app.SunshineWatchFaceApplication;
import com.example.android.sunshine.app.task.ReadForecastDataTask;
import com.example.android.sunshine.app.util.WearableConstants;

/**
 * Created by shamim on 5/15/16.
 */
public class DataLayerListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = DataLayerListenerService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        Log.i(TAG, "DataLayerListenerService initialized successfully");
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected: Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
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
        Log.i(TAG, "onDataChanged() invoked ");

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();

                if (WearableConstants.FORECAST_PATH.equals(path)) {
                    Log.i(TAG, String.format("Data Changed for path: %s", WearableConstants.FORECAST_PATH));
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    SunshineWatchFaceApplication application = (SunshineWatchFaceApplication) getApplication();

                    // start the background task that parses the data and stores it in application class
                    ReadForecastDataTask task = new ReadForecastDataTask(application, mGoogleApiClient);
                    task.execute(dataMapItem);
                } else {
                    Log.w(TAG, "Unrecognized path: " + path);
                }
            } else {
                Log.w(TAG, "unexpected event type : " + event.getType());
            }
        }
    }
}
