package com.example.android.sunshine.app;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.android.sunshine.app.service.DataLayerListenerService;
import com.example.android.sunshine.app.util.WearableConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shamim on 5/15/16.
 */
public class CustomWatchFaceApplication extends Application {
    private static final String TAG = CustomWatchFaceApplication.class.getSimpleName();
    private Map<String, Object> forecastDataMap = Collections.emptyMap();

    public synchronized Map<String, Object> getForecastDataMap() {

        if (forecastDataMap.isEmpty()) {
            populateWithDummyData();
        }

        return forecastDataMap;
    }

    public synchronized void setForecastDataMap(Map<String, Object> forecastDataMap) {
        this.forecastDataMap = forecastDataMap;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "application created");
        startService(new Intent(this, DataLayerListenerService.class));
    }

    public void populateWithDummyData() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun);
        forecastDataMap = new HashMap<>();
        forecastDataMap.put(WearableConstants.TEMPERATURE_HIGH_KEY, "23" + (char) 0x00B0);
        forecastDataMap.put(WearableConstants.TEMPERATURE_LOW_KEY, "15" + (char) 0x00B0);
        forecastDataMap.put(WearableConstants.ICON_BITMAP_KEY, bitmap);
    }
}
