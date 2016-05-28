package com.example.android.sunshine.app.util;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shamim on 5/26/16.
 */
public class NotifyWearableService extends IntentService {
    private static final String TAG = NotifyWearableService.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private ForecastData lastForecastData;

    public NotifyWearableService() {
        super(NotifyWearableService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeGoogleApiClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "NotifyWearableService is running...");
        notifyWearDevice();
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

    private void notifyWearDevice() {
        Context context = getApplicationContext();
        String locationQuery = Utility.getPreferredLocation(context);
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

        Cursor cursor = context.getContentResolver().query(weatherUri, SunshineSyncAdapter.NOTIFY_WEATHER_PROJECTION, null, null, null);

        if (cursor != null) {
            try {
                if (!cursor.moveToFirst()) {
                    Log.i(TAG, "No forecast data could be retrieved from database");
                    return;
                }

                int weatherId = cursor.getInt(SunshineSyncAdapter.INDEX_WEATHER_ID);
                double high = cursor.getDouble(SunshineSyncAdapter.INDEX_MAX_TEMP);
                double low = cursor.getDouble(SunshineSyncAdapter.INDEX_MIN_TEMP);
                String summary = cursor.getString(SunshineSyncAdapter.INDEX_SHORT_DESC);

                // Retrieve the icon
                Resources resources = context.getResources();
                int artResourceId = Utility.getArtResourceForWeatherCondition(weatherId);

                String highTemperatureStr = Utility.formatTemperature(context, high);
                String lowTemperatureStr = Utility.formatTemperature(context, low);

                ForecastData currentForecastData = new ForecastData(highTemperatureStr, lowTemperatureStr, summary);

                if (lastForecastData != null && lastForecastData.equals(currentForecastData)) {
                    Log.i(TAG, "No change in weather forecast detected. Wearable device will not be notified.");
                    return;
                }

                Bitmap weatherIcon = BitmapFactory.decodeResource(resources, artResourceId);

                Map<String, Object> forecastDataMap = new HashMap<>();
                forecastDataMap.put(MobileConstants.SUMMARY_KEY, summary);
                forecastDataMap.put(MobileConstants.TEMPERATURE_HIGH_KEY, highTemperatureStr);
                forecastDataMap.put(MobileConstants.TEMPERATURE_LOW_KEY, lowTemperatureStr);
                forecastDataMap.put(MobileConstants.ICON_KEY, weatherIcon);

                SendDataTask task = new SendDataTask(googleApiClient, forecastDataMap);
                task.execute();

                lastForecastData = currentForecastData;
            } finally {
                cursor.close();
            }
        }
    }

    private static class ForecastData {
        private final String highTemperature;
        private final String lowTemperature;
        private final String summary;

        public ForecastData(String highTemperature, String lowTemperature, String summary) {
            if (highTemperature == null || lowTemperature == null || summary == null) {
                throw new IllegalArgumentException("temperature value or summary cannot be null");
            }

            this.highTemperature = highTemperature;
            this.lowTemperature = lowTemperature;
            this.summary = summary;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ForecastData)) {
                return false;
            }

            ForecastData otherForecast = (ForecastData) obj;

            return highTemperature.equals(otherForecast.highTemperature) && lowTemperature.equals(otherForecast.lowTemperature)
                    && summary.equals(otherForecast.summary);
        }
    }
}
