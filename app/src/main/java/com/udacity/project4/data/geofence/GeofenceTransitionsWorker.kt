package com.udacity.project4.data.geofence

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.Result.Success
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.utils.Constants.EXTRA_ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.Constants.EXTRA_FENCE_ID
import com.udacity.project4.utils.NotificationUtils.sendNotificationAboutEnteredGeofence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class GeofenceTransitionsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val remindersLocalRepository: ReminderDataSource by inject(ReminderDataSource::class.java)

    override suspend fun doWork(): Result {
        Timber.d("doWork: called")

        return try {
            val fenceId = inputData.getString(EXTRA_FENCE_ID)
            val geofenceTransition = inputData.getInt(EXTRA_ACTION_GEOFENCE_EVENT, -1)

            fenceId?.let {
                when (geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Timber.d("Entered geofence: $fenceId")
                        sendNotification(fenceId)
                        Result.success()
                    }
                    else -> {
                        Timber.e("Invalid geofence transition type")
                        Result.failure()
                    }
                }
            } ?: run {
                Timber.e("Invalid data passed to worker")
                Result.failure()
            }
        } catch (e: Exception) {
            Timber.d("RefreshDataWorker:e $e")
            Result.retry()
        }
    }

    private suspend fun sendNotification(fenceId: String) {
        val resultFlow = remindersLocalRepository.getReminder(fenceId)
        if (resultFlow is Success<Flow<ReminderDTO?>>) {
            Timber.d("sendNotification:success")
            resultFlow.data.first()?.let { reminder ->
                Timber.d("sendNotification:success:reminder: $reminder")
                //send a notification to the user with the reminder details
                sendNotificationAboutEnteredGeofence(
                    applicationContext, ReminderDataItem(
                        reminder.title, reminder.description,
                        reminder.location,
                        reminder.latitude, reminder.longitude,
                        reminder.id
                    )
                )
            }
        }
    }
}
