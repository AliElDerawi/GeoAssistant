package com.udacity.project4.data.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.NotificationUtils.errorMessage
import com.udacity.project4.utils.NotificationUtils.sendNotificationAboutEnteredGeofence
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        // TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        // TODO: handle the geofencing transition events and
        //  send a notification to the user when he enters the geofence area
        // TODO call @sendNotification

        Timber.d("onHandleWork:called")


        if (intent.action == Constants.ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)!!

            if (geofencingEvent!!.hasError()) {
                val errorMessage = errorMessage(this, geofencingEvent.errorCode)
                Timber.e(errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Timber.v(this.getString(R.string.geofence_entered))

                val fenceId = when {
                    geofencingEvent.triggeringGeofences!!.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences!![0].requestId

                    else -> {
                        Timber.e("No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                sendNotification(fenceId)

            }
        }

    }

//    triggeringGeofences: List<Geofence>
    // TODO: get the request id of the current geofence
    private fun sendNotification(fenceId : String) {
        val requestId = fenceId

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotificationAboutEnteredGeofence(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }
}