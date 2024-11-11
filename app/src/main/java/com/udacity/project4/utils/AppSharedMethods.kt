package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.data.MyApp
import com.udacity.project4.utils.AppSharedMethods.getSharedPreference
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale


object AppSharedMethods {

    private var mToast: Toast? = null

    fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        mToast?.cancel()
        mToast = Toast.makeText(MyApp.getInstance().applicationContext, message, duration)
        mToast!!.show()
    }

    fun Activity.showToast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
        mToast?.cancel()
        mToast = Toast.makeText(
            MyApp.getInstance().applicationContext,
            getString(message),
            duration
        )
        mToast!!.show()
    }

    fun Activity.showSnackBar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).show()
    }

    fun Activity.showSnackBar(message: Int, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(findViewById(android.R.id.content), getString(message), duration).show()
    }

    fun getSharedPreference(): SharedPreferences {
        return MyApp.getInstance()
            .getSharedPreferences(AppSharedData.MY_PREF, Context.MODE_PRIVATE)
    }

    fun setLoginStatus(isLogin: Boolean) {
        getSharedPreference().edit().putBoolean(AppSharedData.PREF_IS_LOGIN, isLogin).apply()
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun isForegroundAndBackgroundPermissionGranted(mActivity: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            mActivity.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    mActivity.applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    fun isForegroundPermissionGranted(mActivity: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            mActivity.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun isForegroundPermissionGranted(mActivity: Application): Boolean {
        return (ContextCompat.checkSelfPermission(
            mActivity.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun isForegroundAndBackgroundPermissionGranted(mActivity: Application): Boolean {
        return (ContextCompat.checkSelfPermission(
            mActivity.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    mActivity.applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun isBackgroundPermissionGranted(mActivity: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            mActivity.applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun showAndRequestFineLocationDialog(mActivity: Activity) {
        ActivityCompat.requestPermissions(
            mActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.REQUEST_LOCATION_PERMISSION
        )
    }

    fun showForegroundLocationRequestPermission(mActivity: Activity): Boolean {
//         Return True if user denied , and false if a user select never show again
        return ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun showBackgroundLocationRequestPermission(mActivity: Activity): Boolean {
//         Return True if user denied , and false if a user select never show again
        return ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    fun showPostNotificationRequestPermission(mActivity: Activity): Boolean {
//         Return True if user denied , and false if a user select never show again
        return ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity, Manifest.permission.POST_NOTIFICATIONS
        )
    }

    fun Context.isLocationEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    fun startFetchAddressWorker(mLatLng: LatLng, mResultReceiver: MyResultIntentReceiver) {
        // Store the receiver in the singleton
        // Pass latitude and longitude as input data
        val inputData = Data.Builder()
            .putDouble(Constants.EXTRA_LATITUDE, mLatLng.latitude)
            .putDouble(Constants.EXTRA_LONGITUDE, mLatLng.longitude)
            .build()
        FetchAddressWorker.receiver = mResultReceiver
        val fetchAddressWorkRequest = OneTimeWorkRequestBuilder<FetchAddressWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(MyApp.getInstance().applicationContext)
            .enqueue(fetchAddressWorkRequest)
    }

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    fun setFormattedAddress(lat: Double, longt: Double): String {
        val formatter: NumberFormat = DecimalFormat("#00.000000")
        return formatter.format(lat).toString() + " " + formatter.format(longt).toString()
    }

    fun isReceiveNotificationPermissionGranted(mActivity: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            mActivity.applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun isSupportsOreo(f: () -> Unit): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            f()
            return true
        } else {
            return false
        }
    }

    fun isSupportsAndroidM(f: () -> Unit): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            f()
            return true
        } else {
            return false
        }
    }

    fun isSupportsAndroid33(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}

fun isLogin(): Boolean {
    return getSharedPreference().getBoolean(AppSharedData.PREF_IS_LOGIN, false)
}

inline fun <reified T : Activity> Context.createIntent(vararg params: Pair<String, Any>): Intent {
    val intent = Intent(this, T::class.java)
    intent.putExtras(bundleOf(*params))
    intent.setAction(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    return intent
}

fun Context.getCompatColorStateList(color: Int): ColorStateList {
    return ColorStateList.valueOf(
        ResourcesCompat.getColor(
            resources, color, null
        )
    )
}

fun FloatingActionButton.setStatusStyle(isEnabled: Boolean) {
    val backgroundColorStateList = if (isEnabled) {
        context.getCompatColorStateList(R.color.colorAccent)
    } else {
        context.getCompatColorStateList(R.color.colorGrayB2)
    }
    apply {
        backgroundTintList = backgroundColorStateList
    }
}

fun Activity.getSnackBar(message: String, duration: Int = Snackbar.LENGTH_LONG): Snackbar {
    return Snackbar.make(findViewById(android.R.id.content), message, duration)
}

fun GoogleMap.animateCameraToLocation(latLng: LatLng, zoom: Float) {
    animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
}

fun GoogleMap.moveCameraToLocation(latLng: LatLng, zoom: Float) {
    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
}

fun GoogleMap.setCustomMapStyle(mayStyle: Int) {
    try {
        // Customize the styling of the base map using a JSON object defined
        // in a raw resource file.
        val success = setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                MyApp.getInstance().applicationContext, mayStyle
            )
        )
        if (!success) {
            Timber.e("Style parsing failed.")
        }
    } catch (e: Resources.NotFoundException) {
        Timber.e("Can't find style. Error: ", e)
    }
}

fun GoogleMap.addMarkerWithName(latLng: LatLng, name: String): Marker? {
    return addMarker(
        MarkerOptions().position(latLng).title(name)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
    )
}

val Context.notificationManager: NotificationManager?
    get() = getSystemService<NotificationManager>()

val Context.locationManager: LocationManager?
    get() = getSystemService<LocationManager>()
