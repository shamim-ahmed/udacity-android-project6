package com.example.android.sunshine.app.task;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.os.AsyncTask;
import android.util.Log;

import com.example.android.sunshine.app.util.WearableConstants;

import java.util.Date;

/**
 * Created by shamim on 5/28/16.
 */
public class DataRequestSenderTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = DataRequestSenderTask.class.getSimpleName();

    private final GoogleApiClient googleApiClient;

    public DataRequestSenderTask(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!googleApiClient.isConnected()) {
            Log.i(TAG, "Google API client not connected");
            return null;
        }

        Log.i(TAG, "Sending request to mobile app for latest weather data...");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(WearableConstants.GET_FORECAST_DATA_PATH);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putLong(WearableConstants.TIMESTAMP_KEY, new Date().getTime());

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();

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
}
