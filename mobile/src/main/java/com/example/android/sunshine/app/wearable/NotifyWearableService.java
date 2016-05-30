package com.example.android.sunshine.app.wearable;

import com.google.android.gms.common.api.GoogleApiClient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.android.sunshine.app.SunshineApplication;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;
import com.example.android.sunshine.app.util.MobileConstants;
import com.example.android.sunshine.app.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shamim on 5/26/16.
 */
public class NotifyWearableService extends IntentService {
    private static final String TAG = NotifyWearableService.class.getSimpleName();

    public NotifyWearableService() {
        super(NotifyWearableService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "NotifyWearableService is running...");
        notifyWearDevice();
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

                if (StringUtils.isBlank(summary) || StringUtils.isBlank(highTemperatureStr) || StringUtils.isBlank(lowTemperatureStr)) {
                    Log.w(TAG, "Weather data is incomplete. Wearable device will not be notified.");
                    return;
                }

                SunshineApplication application = (SunshineApplication) getApplication();
                ForecastData lastForecastData =  application.getLastForecastData();
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

                GoogleApiClient googleApiClient = application.getGoogleApiClient();

                if (googleApiClient == null) {
                    Log.w(TAG, "Google API client not initialized. No data will be sent to wearable");
                    return;
                }

                ForecastDataSenderTask task = new ForecastDataSenderTask(googleApiClient, forecastDataMap);
                task.execute();

                // store for future reference
                application.setLastForecastData(currentForecastData);
            } finally {
                cursor.close();
            }
        }
    }
}
