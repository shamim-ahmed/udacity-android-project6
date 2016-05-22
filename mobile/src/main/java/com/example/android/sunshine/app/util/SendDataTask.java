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

/**
 * Created by shamim on 5/11/16.
 */
public class SendDataTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = SendDataTask.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmap;

    public SendDataTask(GoogleApiClient pGoogleApiClient, Bitmap pBitmap) {
        mGoogleApiClient = pGoogleApiClient;
        mBitmap = pBitmap;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!mGoogleApiClient.isConnected()) {
            Log.i(TAG, "client not connected...");
            return null;
        }

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/forecast");
        DataMap dataMap = putDataMapRequest.getDataMap();

        JSONObject forecastData = new JSONObject();

        try {
            forecastData.put(MobileConstants.TIMESTAMP_KEY, Long.toString(new Date().getTime()));
            forecastData.put(MobileConstants.TEMPERATURE_HIGH_KEY, "23");
            forecastData.put(MobileConstants.TEMPERATURE_LOW_KEY, "15");

        } catch (Exception ex) {
            Log.e(TAG, "error while constructing json string", ex);
        }

        dataMap.putString("forecast", forecastData.toString());

        // send the forecast icon
        Asset iconAsset = toAsset(mBitmap);

        if (iconAsset != null) {
            dataMap.putAsset(MobileConstants.ICON_BITMAP_KEY, iconAsset);
        }

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        // TODO FIX THIS
        //putDataRequest.setUrgent();

        Log.i(TAG, "data is being sent....");

        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.i(TAG, "the status is : " + dataItemResult.getStatus());
                if (!dataItemResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error while sending data !!");
                }
            }
        });

        return null;
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
