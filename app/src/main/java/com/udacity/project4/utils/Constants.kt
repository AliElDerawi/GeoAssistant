package com.udacity.project4.utils

import androidx.multidex.BuildConfig
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

object Constants {
    const val REQUEST_LOCATION_PERMISSION = 3000
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

    private const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

    const val SUCCESS_RESULT = 0
    const val FAILURE_RESULT = 1
    const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
    const val EXTRA_RESULT_DATA_KEY = "$PACKAGE_NAME.EXTRA_RESULT_DATA_KEY"
    const val EXTRA_LOCATION_DATA_EXTRA = "$PACKAGE_NAME.EXTRA_LOCATION_DATA_EXTRA"
    const val EXTRA_APP_WEBSITE = PACKAGE_NAME + ".EXTRA_APP_WEBSITE"
    const val ACTION_GEOFENCE_EVENT = PACKAGE_NAME + ".action.ACTION_GEOFENCE_EVENT"

    var Current_Location_ZOOM = 15f
    var Default_Location_ZOOM = 7
    val mDefaultLocation = LatLng(26.4207, 50.0888)
    const val MIN_LOCATION_UPDATE_INTERVAL = 1 * 60 * 1000L
    const val MAX_LOCATION_UPDATE_INTERVAL = 2 * 60 * 1000L

    const val GEOFENCE_RADIUS_IN_METERS = 100f
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
}