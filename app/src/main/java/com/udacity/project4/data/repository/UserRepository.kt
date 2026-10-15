package com.udacity.project4.data.repository

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.udacity.project4.data.dto.Result
import com.udacity.project4.data.dto.UserDataSource
import com.udacity.project4.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class UserRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : UserDataSource {

    override suspend fun getCurrentUserLocation(): Result<Location> {
        return wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                try {
                    val cancellationTokenSource = CancellationTokenSource()

                    val location = fusedLocationProviderClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).await()

                    location?.let {
                        Result.Success(it)
                    } ?: Result.Error("No Location detected")

                } catch (e: SecurityException) {
                    ensureActive()
                    Timber.e(e)
                    Result.Error(e.localizedMessage)
                } catch (e: Exception) {
                    ensureActive()
                    Result.Error(e.localizedMessage)
                }
            }
        }
    }
}