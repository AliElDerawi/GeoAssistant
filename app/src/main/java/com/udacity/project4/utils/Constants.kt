package com.udacity.project4.utils

import androidx.multidex.BuildConfig
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

object Constants {
    const val REQUEST_LOCATION_PERMISSION = 3000

    private const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

    const val SUCCESS_RESULT = 0
    const val FAILURE_RESULT = 1
    const val EXTRA_RESULT_DATA_KEY = "$PACKAGE_NAME.EXTRA_RESULT_DATA_KEY"
    const val ACTION_GEOFENCE_EVENT = "$PACKAGE_NAME.action.ACTION_GEOFENCE_EVENT"
    const val EXTRA_FENCE_ID = "$PACKAGE_NAME.EXTRA_FENCE_ID"
    const val EXTRA_LATITUDE = "$PACKAGE_NAME.EXTRA_LATITUDE"
    const val EXTRA_LONGITUDE = "$PACKAGE_NAME.EXTRA_LONGITUDE"

    var CURRENT_LOCATION_ZOON = 15f
    var DEFAULT_LOCATION_ZOOM = 7f
    val MY_DEFAULT_LOCATION = LatLng(26.4207, 50.0888)
    const val MIN_LOCATION_UPDATE_INTERVAL = 1 * 60 * 1000L
    const val MAX_LOCATION_UPDATE_INTERVAL = 2 * 60 * 1000L

    const val GEOFENCE_RADIUS_IN_METERS = 100f
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    val FIREBASE_LOGIN_PROVIDER = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )
}