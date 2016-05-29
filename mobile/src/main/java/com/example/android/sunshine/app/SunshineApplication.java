package com.example.android.sunshine.app;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import android.app.Application;
import android.content.Intent;

import com.example.android.sunshine.app.wearable.CustomConnectionCallbackListener;
import com.example.android.sunshine.app.wearable.DataLayerListenerService;
import com.example.android.sunshine.app.wearable.ForecastData;

/**
 * Created by shamim on 5/28/16.
 */
public class SunshineApplication extends Application {
    private GoogleApiClient googleApiClient;
    private ForecastData lastForecastData;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeGoogleApiClient();
        startService(new Intent(this, DataLayerListenerService.class));
    }

    // this instance of GoogleApiClient is used by NotifyWearableListenerService to send
    // data to the wearable device. It is stored in global context so that multiple instances
    // are not created every time the service is invoked.
    public synchronized GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    // the summary of the last forecast data sent to the wearable
    public synchronized ForecastData getLastForecastData() {
        return lastForecastData;
    }

    public synchronized void setLastForecastData(ForecastData lastForecastData) {
        this.lastForecastData = lastForecastData;
    }

    private void initializeGoogleApiClient() {
        CustomConnectionCallbackListener connectionListener = new CustomConnectionCallbackListener();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(connectionListener)
                .addOnConnectionFailedListener(connectionListener)
                .build();

        googleApiClient.connect();
    }
}
