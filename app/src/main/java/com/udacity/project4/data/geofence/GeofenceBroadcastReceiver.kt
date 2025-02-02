package com.udacity.project4.data.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.utils.Constants.EXTRA_ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.Constants.EXTRA_FENCE_ID
import com.udacity.project4.utils.FetchAddressWorker
import com.udacity.project4.utils.NotificationUtils.errorMessage
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
    //        // TODO - Completed: implement the onReceive method to receive the geofencing events at the background
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: Step 11 - Completed implement the onReceive method
        Timber.d("onReceive: called")
        // Get GeofencingEvent from the Intent
        GeofencingEvent.fromIntent(intent)?.let { geofencingEvent ->
            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                Timber.d("onReceive:Geofence Error: $errorMessage")
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition
            geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId?.let { fenceId ->
                Timber.d("onReceive:Geofence Triggered: $fenceId")
                val data = workDataOf(
                    EXTRA_FENCE_ID to fenceId,
                    EXTRA_ACTION_GEOFENCE_EVENT to geofenceTransition
                )
                val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceTransitionsWorker>()
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context).beginUniqueWork(
                    FetchAddressWorker::class.java.simpleName,
                    ExistingWorkPolicy.REPLACE,
                    geofenceWorkRequest
                ).enqueue()
            } ?: run {
                Timber.e("onReceive:No Geofence Trigger Found! Abort mission!")
            }
        }
    }

}