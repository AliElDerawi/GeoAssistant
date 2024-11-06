package com.udacity.project4.saveReminder.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.NotificationUtils
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Thread.State

class SaveReminderViewModel(
    val app: Application, val remindersLocalRepository: ReminderDataSource
) : BaseViewModel(app) {
    private var _reminderTitle = MutableStateFlow<String?>("")
    val reminderTitle: StateFlow<String?>
        get() = _reminderTitle

    private var _reminderDescription = MutableStateFlow<String?>("")
    val reminderDescription: StateFlow<String?>
        get() = _reminderDescription

    private var _reminderSelectedLocationStr = MutableStateFlow<String?>("")
    val reminderSelectedLocationStr: StateFlow<String?>
        get() = _reminderSelectedLocationStr


    val isCreateReminderEnabled: StateFlow<Boolean> = combine(
        _reminderTitle, _reminderDescription, _reminderSelectedLocationStr
    ) { title, description, location ->
        !title.isNullOrEmpty() && !description.isNullOrEmpty() && !location.isNullOrEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


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

    private var _saveReminder = SingleLiveEvent<Boolean>()
    val saveReminder: LiveData<Boolean>
        get() = _saveReminder

    private var _completeSaveReminder = MutableLiveData<Boolean>()
    val completeSaveReminder: LiveData<Boolean>
        get() = _completeSaveReminder

    private var _createGeofence = MutableLiveData<ReminderDataItem?>()
    val createGeofence: LiveData<ReminderDataItem?>
        get() = _createGeofence

    private var _selectLocation = SingleLiveEvent<Boolean>()
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

    override fun onCleared() {
        super.onCleared()
        Timber.d("onCleared called")
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */

    fun onSaveReminderClick() {
        when {
            _reminderTitle.value.isNullOrEmpty() -> showToastInt.value = R.string.err_enter_title
            _reminderDescription.value.isNullOrEmpty() -> showToastInt.value = R.string.text_msg_please_enter_description
            _reminderSelectedLocationStr.value.isNullOrEmpty() -> showToastInt.value = R.string.err_select_location
            else -> _saveReminder.value = true
        }
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

        saveReminder(reminderDataItem)

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

    fun selectLocationClick() {
        _selectLocation.value = true
    }

}