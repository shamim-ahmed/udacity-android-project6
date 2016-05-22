package com.example.android.sunshine.app;

import android.app.Application;
import android.content.Intent;

import com.example.android.sunshine.app.service.DataLayerListenerService;

import java.util.Collections;
import java.util.Map;

/**
 * Created by shamim on 5/15/16.
 */
public class CustomWatchFaceApplication extends Application {
    private static final String TAG = CustomWatchFaceApplication.class.getSimpleName();
    private Map<String, Object> forecastDataMap = Collections.emptyMap();

    public synchronized Map<String, Object> getForecastDataMap() {
        return forecastDataMap;
    }

    public synchronized void setForecastDataMap(Map<String, Object> forecastDataMap) {
        this.forecastDataMap = forecastDataMap;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // somehow the service needs to be manually started
        startService(new Intent(this, DataLayerListenerService.class));
    }
}
