package com.example.android.sunshine.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by shamim on 5/26/16.
 */
public class CustomBroadcastReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotifyWearableService.class);
        context.startService(service);
    }
}
