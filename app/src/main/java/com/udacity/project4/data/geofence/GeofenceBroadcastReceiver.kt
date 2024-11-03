package com.udacity.project4.data.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

        Timber.d("onReceive:called")
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)


    }

}