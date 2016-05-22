package com.example.android.sunshine.app.task;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.sunshine.app.CustomWatchFaceApplication;
import com.example.android.sunshine.app.util.WearableConstants;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shamim on 5/14/16.
 */
public class ReadForecastDataTask extends AsyncTask<DataMapItem, Void, Map<String, Object>> {
    private static final String TAG = ReadForecastDataTask.class.getSimpleName();
    private final CustomWatchFaceApplication application;
    private final GoogleApiClient googleApiClient;

    public ReadForecastDataTask(CustomWatchFaceApplication application, GoogleApiClient googleApiClient) {
        this.application = application;
        this.googleApiClient = googleApiClient;
    }

    @Override
    protected Map<String, Object> doInBackground(DataMapItem... params) {
        if (!googleApiClient.isConnected()) {
            Log.w(TAG, "google api client not connected");
            return Collections.emptyMap();
        }

        if (params.length < 1) {
            return Collections.emptyMap();
        }

        DataMapItem dataMapItem = params[0];
        String forecastData = dataMapItem.getDataMap().get(WearableConstants.FORECAST_KEY);
        Log.i(TAG, String.format("forecast received : %s", forecastData));

        // parse json to get forecast data
        Map<String, Object> forecastMap = parseForecastData(forecastData);

        // retrieve the bitmap for icon
        Bitmap bitmap = retrieveBitmap(dataMapItem);

        if (bitmap != null) {
            forecastMap.put(WearableConstants.ICON_KEY, bitmap);
        }

        return forecastMap;
    }

    @Override
    protected void onPostExecute(Map<String, Object> resultMap)  {
        application.setForecastDataMap(resultMap);
    }

    private Map<String, Object> parseForecastData(String inputStr) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(inputStr);
            resultMap.put(WearableConstants.SUMMARY_KEY, jsonObject.getString(WearableConstants.SUMMARY_KEY));
            resultMap.put(WearableConstants.TEMPERATURE_HIGH_KEY, jsonObject.getString(WearableConstants.TEMPERATURE_HIGH_KEY));
            resultMap.put(WearableConstants.TEMPERATURE_LOW_KEY, jsonObject.getString(WearableConstants.TEMPERATURE_LOW_KEY));
        } catch (Exception ex) {
            Log.e(TAG, "error while parsing json string");
        }

        return resultMap;
    }

    private Bitmap retrieveBitmap(DataMapItem dataMapItem) {
        Asset asset = dataMapItem.getDataMap().getAsset(WearableConstants.ICON_KEY);
        InputStream inStream = Wearable.DataApi.getFdForAsset(googleApiClient, asset).await().getInputStream();

        if (inStream == null) {
            Log.w(TAG, "cannot retrieve inputStream for asset");
            return null;
        }

        return BitmapFactory.decodeStream(inStream);
    }
}
