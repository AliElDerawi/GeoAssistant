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
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.MyApp
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale


object AppSharedMethods {

    private var mToast: Toast? = null

    fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast = Toast.makeText(MyApp.getInstance()!!.applicationContext, message, duration)
        mToast!!.show()
    }

    fun showToast(message: Int, duration: Int = Toast.LENGTH_LONG) {
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast = Toast.makeText(
            MyApp.getInstance()!!.applicationContext,
            MyApp.getInstance()!!.applicationContext.getString(message),
            duration
        )
        mToast!!.show()
    }

    fun EditText.isEmpty(): Boolean {
        return this.text.toString().isEmpty()
    }

    fun EditText.isValidEmail(): Boolean {
        return !this.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(this.text.toString()).matches()
    }

    fun String.isValidEmail(): Boolean {
        return !this.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(this.toString()).matches()
    }

    fun Context.setImageInAdapter(
        drawable: Int, mTargetImageView: ImageView
    ) {
        Glide.with(this).load(ResourcesCompat.getDrawable(this.resources, drawable, null))
            .into(mTargetImageView)
    }

    fun getSharedPreference(): SharedPreferences {
        return MyApp.getInstance()!!
            .getSharedPreferences(AppSharedData.MY_PREF, Context.MODE_PRIVATE)
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun checkIfUserExist(email: String, password: String): Boolean {
        if (getSharedPreference().contains(AppSharedData.PREF_USER_EMAIL)) {
            return getSharedPreference().getString(AppSharedData.PREF_USER_EMAIL, "") == email
                    && getSharedPreference().getString(
                AppSharedData.PREF_USER_PASSWORD,
                ""
            ) == password
        } else {
            return false
        }

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

    fun showAndRequestFineLocationDialog(mActivity: Activity?) {
        ActivityCompat.requestPermissions(
            mActivity!!,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.REQUEST_LOCATION_PERMISSION
        )
    }

    fun showForegroundLocationRequestPermission(mActivity: Activity?): Boolean {
//         Return True if user denied , and false if a user select never show again
        return ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity!!, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun showBackgroundLocationRequestPermission(mActivity: Activity?): Boolean {
//         Return True if user denied , and false if a user select never show again
        return ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    fun showPostNotificationRequestPermission(mActivity: Activity?): Boolean {
//         Return True if user denied , and false if a user select never show again
        return ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity!!, Manifest.permission.POST_NOTIFICATIONS
        )
    }

    fun isLocationEnabled(mContext: Context): Boolean {
        var locationMode = 0
        val locationProviders: String
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            locationMode = try {
                Settings.Secure.getInt(mContext.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(
                mContext.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            !TextUtils.isEmpty(locationProviders)
        }
    }

    fun Context.getLocationNameReceiver(mLatLng: LatLng, mResultReceiver: MyResultIntentReceiver) {
        val intent = Intent(this, FetchAddressIntentService::class.java)
        intent.putExtra(Constants.RECEIVER, mResultReceiver)
        intent.putExtra(Constants.EXTRA_LOCATION_DATA_EXTRA, mLatLng)
        startService(intent)
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
}

inline fun <reified T : Activity> Context.createIntent(vararg params: Pair<String, Any>): Intent {
    val intent = Intent(this, T::class.java)
    intent.putExtras(bundleOf(*params))
    return intent
}

val Context.notificationManager: NotificationManager?
    get() = getSystemService<NotificationManager>()

