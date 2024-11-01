package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.app.NotificationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.NotificationUtils
import kotlinx.coroutines.launch

class SaveReminderViewModel(
    val app: Application,
    val remindersLocalRepository: ReminderDataSource
) :
    BaseViewModel(app) {
    private val _reminderTitle = MutableLiveData<String?>()
    val reminderTitle: LiveData<String?>
        get() = _reminderTitle

    private val _reminderDescription = MutableLiveData<String?>()
    val reminderDescription: LiveData<String?>
        get() = _reminderDescription

    private var _reminderSelectedLocationStr = MutableLiveData<String?>()
    val reminderSelectedLocationStr: LiveData<String?>
        get() = _reminderSelectedLocationStr

    private var _selectedPOI = MutableLiveData<PointOfInterest?>()
    val selectedPOI: LiveData<PointOfInterest?>
        get() = _selectedPOI

    private var _latitude = MutableLiveData<Double?>()
    val latitude: LiveData<Double?>
        get() = _latitude

    private var _longitude = MutableLiveData<Double?>()
    val longitude: LiveData<Double?>
        get() = _longitude

    private var _moveMap = MutableLiveData<Boolean>()
    val moveMap: LiveData<Boolean>
        get() = _moveMap

    private var _saveLocation = MutableLiveData<Boolean>()
    val saveLocation: LiveData<Boolean>
        get() = _saveLocation

    private var _saveReminder = MutableLiveData<Boolean>()
    val saveReminder: LiveData<Boolean>
        get() = _saveReminder

    private var _completeSaveReminder = MutableLiveData<Boolean>()
    val completeSaveReminder: LiveData<Boolean>
        get() = _completeSaveReminder

    private var _createGeofence = MutableLiveData<ReminderDataItem?>()
    val createGeofence: LiveData<ReminderDataItem?>
        get() = _createGeofence

    private var _selectLocation = MutableLiveData<Boolean>()
    val selectLocation: LiveData<Boolean>
        get() = _selectLocation

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        _reminderTitle.value = null
        _reminderDescription.value = null
        _reminderSelectedLocationStr.value = null
        _selectedPOI.value = null
        _latitude.value = null
        _longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    fun onSaveReminderClick() {

        _saveReminder.value = true

    }

    fun setSelectedPOI(pointOfInterest: PointOfInterest) {
        _selectedPOI.value = pointOfInterest
    }

    fun createGeofenceAfterGrantPermission() {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(app)) {
            showToast.postValue(app.getString(R.string.text_enable_background_location_permission_msg))
            return
        }

        NotificationUtils.createChannel(app)

        val title = reminderTitle.value
        val description = reminderDescription.value
        val location = reminderSelectedLocationStr.value
        val latitude = latitude.value
        val longitude = longitude.value

        val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

        validateAndSaveReminder(reminderDataItem)

    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.postValue(true)
        viewModelScope.launch {
            remindersLocalRepository.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.postValue(false)
            showToast.postValue(app.getString(R.string.reminder_saved))
            _createGeofence.value = (reminderData)
            _completeSaveReminder.value = (true)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showToast.postValue(app.getString(R.string.err_enter_title))
            return false
        }

        if (reminderData.description.isNullOrEmpty()) {
            showToast.postValue(app.getString(R.string.text_msg_please_enter_description))
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showToast.postValue(app.getString(R.string.err_select_location))
            return false
        }
        return true
    }

    fun navigateToLastMarkedLocation() {
        _moveMap.value = true
    }

    fun onNavigationComplete() {
        _moveMap.value = false
    }

    fun saveLocation() {
        if (selectedPOI.value != null) {
            _reminderSelectedLocationStr.value = (selectedPOI.value!!.name)
            _latitude.value = (selectedPOI.value!!.latLng.latitude)
            _longitude.value = (selectedPOI.value!!.latLng.longitude)
            _saveLocation.value = (true)
        } else {
            showSnackBarInt.postValue((R.string.err_select_location))
        }
    }

    fun onLocationSaved() {
        _saveLocation.value = false
    }

    fun onTitleTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _reminderTitle.value = s.toString()
    }

    fun onDescriptionTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _reminderDescription.value = s.toString()
    }

    fun onCreateGeofenceComplete() {
        _createGeofence.value = null
        _saveReminder.value = false
        _completeSaveReminder.value = false
    }

    fun setSaveReminder(isSaveReminder: Boolean) {
        _saveReminder.value = isSaveReminder
        _completeSaveReminder.value = isSaveReminder
    }

    fun selectLocationClick() {
        _selectLocation.value = true
    }

    fun onNavigateToSelectLocation() {
        _selectLocation.value = false
    }
}