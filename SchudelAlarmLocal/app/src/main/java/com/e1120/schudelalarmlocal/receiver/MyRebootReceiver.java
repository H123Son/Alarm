package com.e1120.schudelalarmlocal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;

import com.e1120.schudelalarmlocal.service.RebootService;

public class MyRebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, RebootService.class);
        intent2.putExtra("caller", "RebootReceiver");
        if (VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent2);
        } else {
            context.startService(intent2);
        }
    }
}
