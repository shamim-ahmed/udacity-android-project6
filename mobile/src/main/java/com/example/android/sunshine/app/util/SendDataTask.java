package com.example.android.sunshine.app.util;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Map;

/**
 * Created by shamim on 5/11/16.
 */
public class SendDataTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = SendDataTask.class.getSimpleName();

    private final GoogleApiClient googleApiClient;
    private final Map<String, Object> inputMap;

    public SendDataTask(GoogleApiClient googleApiClient, Map<String, Object> inputMap) {
        this.googleApiClient = googleApiClient;
        this.inputMap = inputMap;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!googleApiClient.isConnected()) {
            Log.i(TAG, "client not connected...");
            return null;
        }

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(MobileConstants.FORECAST_PATH);
        DataMap dataMap = putDataMapRequest.getDataMap();

        JSONObject forecastData = createJsonObjectFromInputData();
        dataMap.putString(MobileConstants.FORECAST_KEY, forecastData.toString());

        // send the forecast icon
        Bitmap bitmap = (Bitmap) inputMap.get(MobileConstants.ICON_KEY);
        Asset iconAsset = toAsset(bitmap);

        if (iconAsset != null) {
            dataMap.putAsset(MobileConstants.ICON_KEY, iconAsset);
        }

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        // TODO FIX THIS
        //putDataRequest.setUrgent();

        Log.i(TAG, "data is being sent...");

        Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.i(TAG, "the status is : " + dataItemResult.getStatus());

                if (!dataItemResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error while sending data !");
                }
            }
        });

        return null;
    }

    private JSONObject createJsonObjectFromInputData() {
        JSONObject forecastData = new JSONObject();

        String forecastSummary = (String) inputMap.get(MobileConstants.SUMMARY_KEY);
        String highTemperatureStr = (String) inputMap.get(MobileConstants.TEMPERATURE_HIGH_KEY);
        String lowTemperatureStr = (String) inputMap.get(MobileConstants.TEMPERATURE_LOW_KEY);

        try {
            forecastData.put(MobileConstants.TIMESTAMP_KEY, Long.toString(new Date().getTime()));
            forecastData.put(MobileConstants.SUMMARY_KEY, forecastSummary);
            forecastData.put(MobileConstants.TEMPERATURE_HIGH_KEY, highTemperatureStr);
            forecastData.put(MobileConstants.TEMPERATURE_LOW_KEY, lowTemperatureStr);
        } catch (Exception ex) {
            Log.e(TAG, "error while constructing json string", ex);
        }

        return forecastData;
    }

    private static Asset toAsset(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        Asset asset = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            asset = Asset.createFromBytes(outStream.toByteArray());
        } catch (Exception ex) {
            Log.e(TAG, "error while creating asset from bitmap", ex);
        }

        return asset;
    }
}
