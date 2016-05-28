package com.example.android.sunshine.app;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import android.app.Application;

import com.example.android.sunshine.app.wearable.CustomConnectionCallbackListener;
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
    }

    public synchronized GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

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
