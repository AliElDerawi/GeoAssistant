package com.udacity.project4.data.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.utils.Constants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.Constants.EXTRA_FENCE_ID
import timber.log.Timber

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    //        // TODO: implement the onReceive method to receive the geofencing events at the background
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: Step 11 implement the onReceive method
        Timber.d("onReceive: called")
        // Get GeofencingEvent from the Intent
        GeofencingEvent.fromIntent(intent)?.let { geofencingEvent ->
            if (geofencingEvent.hasError()) {
                Timber.e("Geofence error: ${geofencingEvent.errorCode}")
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition
            geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId?.let { fenceId ->
                val data = workDataOf(
                    EXTRA_FENCE_ID to fenceId,
                    ACTION_GEOFENCE_EVENT to geofenceTransition
                )
                val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceTransitionsWorker>()
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context).enqueue(geofenceWorkRequest)
            } ?: run {
                Timber.e("No Geofence Trigger Found! Abort mission!")
            }
        }
    }

}