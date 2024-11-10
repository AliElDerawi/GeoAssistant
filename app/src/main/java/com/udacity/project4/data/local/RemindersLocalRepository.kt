package com.udacity.project4.data.local

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.udacity.project4.R
import com.udacity.project4.data.MyApp
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import com.udacity.project4.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Concrete implementation of a data source as a db.
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param remindersDao the dao that does the Room db operations
 * @param ioDispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class RemindersLocalRepository(
    private val remindersDao: RemindersDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ReminderDataSource {

    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override fun getReminders(): Result<Flow<List<ReminderDTO>>> {
        return wrapEspressoIdlingResource {
            try {
                val reminders = remindersDao.getReminders()
                Result.Success(reminders)
            } catch (ex: Exception) {
                Result.Error(ex.localizedMessage)
            }
        }
    }

    /**
     * Insert a reminder in the db.
     * @param reminder the reminder to be inserted
     */
    override suspend fun saveReminder(reminder: ReminderDTO) =
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                remindersDao.saveReminder(reminder)
            }
        }

    /**
     * Get a reminder by its id
     * @param id to be used to get the reminder
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getReminder(id: String): Result<Flow<ReminderDTO?>> {
        return wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                try {
                    val reminder = remindersDao.getReminderById(id)
                    reminder.first()?.let {
                        Result.Success(reminder)
                    } ?: Result.Error(
                        MyApp.getInstance().getString(R.string.text_error_reminder_not_found)
                    )
                } catch (ex: Exception) {
                    Result.Error(ex.localizedMessage)
                }
            }
        }
    }

    /**
     * Deletes all the reminders in the db
     */
    override suspend fun deleteAllReminders() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                remindersDao.deleteAllReminders()
            }
        }
    }

    override suspend fun getLastUserLocation(): Result<Flow<Location?>> {
        return wrapEspressoIdlingResource {
            try {
                val location = fusedLocationProviderClient.lastLocation.await()
                Result.Success(flow { emit(location) })
            } catch (e: SecurityException) {
                Timber.e(e)
                Result.Error(e.localizedMessage)
            } catch (e: Exception) {
                Result.Error(e.localizedMessage)
            }
        }
    }
}
