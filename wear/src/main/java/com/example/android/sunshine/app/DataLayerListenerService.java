package com.example.android.sunshine.app;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.os.Bundle;
import android.util.Log;

/**
 * Created by shamim on 5/15/16.
 */
public class DataLayerListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final String FORECAST_PATH = "/forecast";

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
        Log.i(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i(TAG, "onDataChanged() invoked ");

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (FORECAST_PATH.equals(path)) {
                    Log.i(TAG, "Data Changed for FORECAST_PATH");
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    CustomWatchFaceApplication application = (CustomWatchFaceApplication) getApplication();

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
