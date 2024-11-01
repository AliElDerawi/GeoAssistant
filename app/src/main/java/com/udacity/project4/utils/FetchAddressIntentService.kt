package com.udacity.project4.utils

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.MyApp
import com.udacity.project4.R
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

class FetchAddressIntentService : IntentService(FetchAddressIntentService::class.java.name) {

    protected var mReceiver: ResultReceiver? = null


    private val TAG = FetchAddressIntentService::class.java.simpleName


    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER)
        val geocoder = Geocoder(
            this,
            Locale(
                AppSharedMethods.getCurrentLocale(this).language
            )
        )
        var errorMessage = ""

        // Get the location passed to this service through an extra.
        val mLatLng: LatLng =
            intent.getParcelableExtra<LatLng>(Constants.EXTRA_LOCATION_DATA_EXTRA)!!


        // ...
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(
                mLatLng.latitude,
                mLatLng.longitude,  // In this sample, get just a single address.
                1
            )
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
//            Crashlytics.logException(ioException);
            errorMessage = resources.getString(R.string.msg_address_location_network_issue)
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
//            Crashlytics.logException(illegalArgumentException);
            // Catch invalid latitude or longitude values.
            errorMessage = resources.getString(R.string.msg_address_location_network_issue)
            Log.e(
                TAG, errorMessage + ". " +
                        "Latitude = " + mLatLng.latitude +
                        ", Longitude = " +
                        mLatLng.longitude, illegalArgumentException
            )
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage =
                    AppSharedMethods.setFormattedAddress(mLatLng.latitude, mLatLng.longitude)
                Log.e(TAG, "Address Not Found: $errorMessage")
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
        } else {
            val address = addresses[0]
            val addressFragments = ArrayList<String?>()

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            if (!address.getAddressLine(0)
                    .contains(getString(R.string.error_address_location_unknown))
            ) {
                for (i in 0..address.maxAddressLineIndex) {
                    addressFragments.add(address.getAddressLine(i))
                }

                deliverResultToReceiver(
                    Constants.SUCCESS_RESULT,
                    TextUtils.join(
                        System.getProperty("line.separator").toString(),
                        addressFragments
                    )
                )
            } else {
                addressFragments.add(
                    AppSharedMethods.setFormattedAddress(
                        mLatLng.latitude,
                        mLatLng.longitude
                    )
                )

                deliverResultToReceiver(
                    Constants.FAILURE_RESULT,
                    TextUtils.join(
                        System.getProperty("line.separator").toString(),
                        addressFragments
                    )
                )
            }
            Log.d(
                TAG,
                "Address Found: " + address.getAddressLine(0) + " , countryName : " + address.countryName + "  , featureName : " + address.featureName + " , locality :  " + address.locality
            )
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString(Constants.EXTRA_RESULT_DATA_KEY, message)
        mReceiver!!.send(resultCode, bundle)
    }

}