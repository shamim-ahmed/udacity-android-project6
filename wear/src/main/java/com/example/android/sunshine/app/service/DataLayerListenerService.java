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
import com.example.android.sunshine.app.task.DataRequestSenderTask;
import com.example.android.sunshine.app.task.ReadForecastDataTask;
import com.example.android.sunshine.app.util.WearableConstants;

import java.util.Map;

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

        // check if we have forecast data
        SunshineWatchFaceApplication application = (SunshineWatchFaceApplication) getApplication();
        Map<String, Object> dataMap = application.getForecastDataMap();

        if (dataMap == null || dataMap.isEmpty()) {
            // send a request for screen initialization with forecast data
            DataRequestSenderTask task = new DataRequestSenderTask(googleApiClient);
            task.execute();
        }
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

                if (WearableConstants.FORECAST_PATH.equals(path)) {
                    Log.i(TAG, String.format("Data Changed for path: %s", WearableConstants.FORECAST_PATH));
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    SunshineWatchFaceApplication application = (SunshineWatchFaceApplication) getApplication();

                    // start the background task that parses the data and stores it in application class
                    ReadForecastDataTask task = new ReadForecastDataTask(application, googleApiClient);
                    task.execute(dataMapItem);
                }
            }
        }
    }
}
