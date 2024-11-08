package com.udacity.project4.utils

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import timber.log.Timber
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

class FetchAddressIntentService : IntentService(FetchAddressIntentService::class.java.name) {

    private var mReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {

        intent?.let {
            mReceiver = it.getParcelableExtra(Constants.RECEIVER)
            val geocoder = Geocoder(this, Locale(AppSharedMethods.getCurrentLocale(this).language))
            val mLatLng: LatLng = it.getParcelableExtra(Constants.EXTRA_LOCATION_DATA_EXTRA)!!
            var errorMessage = ""
            var addresses: List<Address>? = null

            try {
                addresses = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1)
            } catch (e: Exception) {
                errorMessage = resources.getString(R.string.msg_address_location_network_issue)
                Timber.e(errorMessage, e)
            }

            addresses.takeIf { it.isNullOrEmpty() }?.let {
                errorMessage = errorMessage.ifEmpty {
                    AppSharedMethods.setFormattedAddress(mLatLng.latitude, mLatLng.longitude)
                }
                Timber.e("Address Not Found: $errorMessage")
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
            } ?: run {
                val address = addresses!![0]
                val addressFragments = ArrayList<String?>()
                if (!address.getAddressLine(0).contains(getString(R.string.error_address_location_unknown))) {
                    for (i in 0..address.maxAddressLineIndex) {
                        addressFragments.add(address.getAddressLine(i))
                    }
                    deliverResultToReceiver(
                        Constants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator").toString(), addressFragments)
                    )
                } else {
                    addressFragments.add(AppSharedMethods.setFormattedAddress(mLatLng.latitude, mLatLng.longitude))
                    deliverResultToReceiver(
                        Constants.FAILURE_RESULT,
                        TextUtils.join(System.getProperty("line.separator").toString(), addressFragments)
                    )
                }
                Timber.d("Address Found: ${address.getAddressLine(0)} , countryName : ${address.countryName}  , featureName : ${address.featureName} , locality :  ${address.locality}")
            }
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString(Constants.EXTRA_RESULT_DATA_KEY, message)
        mReceiver?.send(resultCode, bundle)
    }

}