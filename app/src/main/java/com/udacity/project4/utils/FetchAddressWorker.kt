package com.udacity.project4.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.project4.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import kotlin.coroutines.resume

class FetchAddressWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val receiver: MyResultIntentReceiver by inject(MyResultIntentReceiver::class.java)

    override suspend fun doWork(): Result {
        val latitude = inputData.getDouble(Constants.EXTRA_LATITUDE, 0.0)
        val longitude = inputData.getDouble(Constants.EXTRA_LONGITUDE, 0.0)

        val geocoder = Geocoder(applicationContext, AppSharedMethods.getCurrentLocale(applicationContext))

        return try {
            Timber.d("FetchAddressWorker:doWork:called")

            val addresses = geocoder.getAddressSuspend(latitude, longitude)

            addresses.takeIf { it.isNullOrEmpty() }?.let {
                val errorMessage = AppSharedMethods.setFormattedAddress(latitude, longitude)
                receiver.send(Constants.FAILURE_RESULT, createResultBundle(errorMessage))
                Result.failure()
            } ?: run {
                val address = addresses!![0]
                val addressFragments = ArrayList<String?>().apply {
                    for (i in 0..address.maxAddressLineIndex) {
                        add(address.getAddressLine(i))
                    }
                }
                val addressResult = TextUtils.join(System.lineSeparator(), addressFragments)
                receiver.send(Constants.SUCCESS_RESULT, createResultBundle(addressResult))
                Timber.d("FetchAddressWorker:doWork:success")
                Result.success()
            }
        } catch (e: Exception) {
            val errorMessage = context.getString(R.string.msg_address_location_network_issue)
            receiver.send(Constants.FAILURE_RESULT, createResultBundle(errorMessage))
            Result.failure()
        }
    }

    private fun createResultBundle(message: String): Bundle {
        return Bundle().apply {
            putString(Constants.EXTRA_RESULT_DATA_KEY, message)
        }
    }

    @Suppress("DEPRECATION")
    suspend fun Geocoder.getAddressSuspend(latitude: Double, longitude: Double): List<Address>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            suspendCancellableCoroutine { continuation ->
                getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) continuation.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                })
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    getFromLocation(latitude, longitude, 1)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}