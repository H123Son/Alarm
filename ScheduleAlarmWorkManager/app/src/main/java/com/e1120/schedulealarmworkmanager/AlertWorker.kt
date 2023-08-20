package com.e1120.schedulealarmworkmanager

import android.app.*
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import io.chanjungkim.alarm_workmanager.logdd
import java.util.concurrent.TimeUnit

class AlertWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        logdd("doWork()")
        val title = inputData.getString("title")
        val message = inputData.getString("message")
        val id = inputData.getLong("idNotification", 0).toInt()
        val context = this.applicationContext

        // Show Notification
//        val notificationUtil = NotificationUtil(context)

        showNotification(context)
        return Result.success()
    }


    private fun showNotification(context: Context) {
        val ALARM_CHANNEL_ID = 0 // TODO: MORNING -> 0 NIGHT -> 1

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val fullscreenIntent = Intent(context, AlarmActivity::class.java).apply {
            action = "fullscreen_activity"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        val donePendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack

            val acceptIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = AlarmAction.DONE.name
            }
            addNextIntentWithParentStack(acceptIntent)

            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val cancelIntent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = AlarmAction.CANCEL.name
            putExtra(EXTRA_NOTIFICATION_ID, 0)
        }

        val cancelPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                0,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val fullscreenPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                0,
                fullscreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                0,
                fullscreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val builder = NotificationCompat.Builder(context, "$ALARM_CHANNEL_ID").apply {
            setSmallIcon(R.drawable.ic_stat_name)
//            setLargeIcon(bitmap)

            setContentTitle("섭취 알람")
            setContentText("이번에 누르면, 연속 100일 달성! 랭킹 상승! 레벨업 상승!")
            // setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLocalOnly(true)
            addAction(android.R.drawable.star_on, "ON", donePendingIntent)
            addAction(android.R.drawable.star_off, "OFF", cancelPendingIntent)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(fullscreenPendingIntent)
            setOngoing(false)
            setAutoCancel(true)
            setFullScreenIntent(fullscreenPendingIntent, false)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "$ALARM_CHANNEL_ID",
                "ALARM CHANNEL",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.apply {
                val soundUri =
                    Uri.parse("android.resource://" + context.packageName + "/" + R.raw.alert_sound)
                val att = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                setSound(soundUri, att)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mNotification = builder.build().apply {
            flags = Notification.FLAG_INSISTENT or Notification.FLAG_ONGOING_EVENT
        }
        notificationManager.notify(ALARM_CHANNEL_ID, mNotification)
    }

    companion object {
        @JvmStatic
        fun saveNotification(context: Context, duration: Long, data: Data?, tag: String?) {
            val notificationWork = OneTimeWorkRequest.Builder(AlertWorker::class.java)
                .setInitialDelay(duration, TimeUnit.MILLISECONDS).addTag(tag!!)
                .setInputData(data!!)
                .build()
            val instaWorkManager = WorkManager.getInstance(context)
            instaWorkManager.enqueue(notificationWork)
        }
    }
}