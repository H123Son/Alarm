package com.e1120.schedulealarmworkmanager

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.chanjungkim.alarm_workmanager.logdd

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        logdd("${AlarmBroadcastReceiver::class.java.name}: onReceive, ${intent?.action}}")
        if (intent != null && intent.extras != null) {
            with(context) {
                val channelId = 0 // TODO: get channelId from Intent
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                when (intent.action) {
                    AlarmAction.DONE.name -> {
                        notificationManager.cancel(channelId)
                    }
                    AlarmAction.CANCEL.name -> {
                        notificationManager.cancel(channelId)
                    }
                }
            }
        }
    }
}
