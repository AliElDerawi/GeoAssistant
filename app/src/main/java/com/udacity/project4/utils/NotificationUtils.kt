package com.udacity.project4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.locationreminders.reminderDescription.ReminderDescriptionFragment
import com.udacity.project4.features.main.view.MainActivity
import com.udacity.project4.utils.AppSharedMethods.createIntent
import com.udacity.project4.utils.AppSharedMethods.notificationManager


object NotificationUtils {

    private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    fun createChannel(context: Context) {
        AppSharedMethods.isSupportsOreo {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description =
                    context.getString(R.string.text_notification_channel_description)
            }
            context.getSystemService<NotificationManager>()?.apply {
                createNotificationChannel(notificationChannel)
            }
        }
    }

    fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

    fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.msg_geofence_not_available
            )

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.msg_geofence_too_many_geofences
            )

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.msg_geofence_too_many_pending_intents
            )

            else -> resources.getString(R.string.msg_unknown_geofence_error)
        }
    }


    fun sendNotificationAboutEnteredGeofence(
        context: Context,
        reminderDataItem: ReminderDataItem
    ) {
        val notificationManager = context.notificationManager
        val intent =
            context.createIntent<MainActivity>(ReminderDescriptionFragment.EXTRA_ReminderDataItem to reminderDataItem)
                .apply {
                    action = "actionstring" + System.currentTimeMillis()
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
        val notificationPendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_MUTABLE
        )
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(
                TextUtils.concat(
                    context.getString(R.string.msg_entered_geofence),
                    " ",
                    reminderDataItem.title
                )
            )
            .setContentText(reminderDataItem.location)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(getUniqueId(), notification)
    }
}


