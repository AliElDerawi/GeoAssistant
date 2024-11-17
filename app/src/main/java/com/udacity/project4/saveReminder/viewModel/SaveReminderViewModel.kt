package com.udacity.project4.saveReminder.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.saveReminder.view.SaveReminderFragmentDirections
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.AppSharedMethods.isForegroundPermissionGranted
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.NotificationUtils
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import com.udacity.project4.data.dto.Result
import com.udacity.project4.utils.AppSharedMethods.startFetchAddressWorker

class SaveReminderViewModel(
    private val mApp: Application,
    private val mRemindersLocalRepository: ReminderDataSource,
    private val mGeofencingClient: GeofencingClient
) : BaseViewModel(mApp) {

    private var _reminderTitleStateFlow = MutableStateFlow<String?>("")
    val reminderTitleStateFlow: StateFlow<String?>
        get() = _reminderTitleStateFlow

    private var _reminderDescriptionStateFlow = MutableStateFlow<String?>("")
    val reminderDescriptionStateFlow: StateFlow<String?>
        get() = _reminderDescriptionStateFlow

    private var _reminderSelectedLocationStrStateFlow = MutableStateFlow<String?>("")
    val reminderSelectedLocationStrStateFlow: StateFlow<String?>
        get() = _reminderSelectedLocationStrStateFlow

    val isCreateReminderEnabledStateFlow: StateFlow<Boolean> = combine(
        _reminderTitleStateFlow, _reminderDescriptionStateFlow, _reminderSelectedLocationStrStateFlow
    ) { title, description, location ->
        !title.isNullOrEmpty() && !description.isNullOrEmpty() && !location.isNullOrEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private var _selectedPOILiveData = MutableLiveData<PointOfInterest?>()
    val selectedPOILiveData: LiveData<PointOfInterest?>
        get() = _selectedPOILiveData

    private var _moveMapSingleLiveEvent = SingleLiveEvent<Boolean>()
    val moveMapSingleLiveEvent: LiveData<Boolean>
        get() = _moveMapSingleLiveEvent

    private var _saveLocationSingleLiveEvent = SingleLiveEvent<Boolean>()
    val saveLocationSingleLiveEvent: LiveData<Boolean>
        get() = _saveLocationSingleLiveEvent

    private var _saveReminderSingleLiveEvent = SingleLiveEvent<Boolean>()
    val saveReminderSingleLiveEvent: LiveData<Boolean>
        get() = _saveReminderSingleLiveEvent

    private var _createGeofenceSingleLiveEvent = SingleLiveEvent<ReminderDataItem?>()
    val createGeofenceSingleLiveEvent: LiveData<ReminderDataItem?>
        get() = _createGeofenceSingleLiveEvent

    private var _lastUserLocationStateFlow = MutableStateFlow<Location?>(null)
    val lastUserLocationStateFlow: StateFlow<Location?>
        get() = _lastUserLocationStateFlow

    private var _selectedLocationLatLngLiveData = MutableLiveData<LatLng?>()
    val selectedLocationLatLngLiveData: LiveData<LatLng?>
        get() = _selectedLocationLatLngLiveData

    private var _currentMapStyleLiveData = MutableLiveData<Int>(R.id.normal_map)
    val currentMapStyleLiveData: LiveData<Int>
        get() = _currentMapStyleLiveData

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        _reminderTitleStateFlow.value = null
        _reminderDescriptionStateFlow.value = null
        _reminderSelectedLocationStrStateFlow.value = null
        _selectedPOILiveData.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("onCleared called")
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(mApp, GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.EXTRA_ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(mApp, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    fun onTitleTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _reminderTitleStateFlow.value = s.toString()
    }

    fun onDescriptionTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _reminderDescriptionStateFlow.value = s.toString()
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */

    fun onSaveReminderClick() {
        when {
            _reminderTitleStateFlow.value.isNullOrEmpty() -> showToastInt.postValue(R.string.msg_enter_title)
            _reminderDescriptionStateFlow.value.isNullOrEmpty() -> showToastInt.postValue(
                R.string.msg_please_enter_description
            )
            _reminderSelectedLocationStrStateFlow.value.isNullOrEmpty() -> showToastInt.postValue(
                R.string.msg_select_location
            )
            else -> _saveReminderSingleLiveEvent.postValue(true)
        }
    }

    fun setSelectedPOIAndShowName(pointOfInterest: PointOfInterest) {
        _selectedPOILiveData.value = pointOfInterest
        startFetchAddressWorker(
            LatLng(
                pointOfInterest.latLng.latitude, pointOfInterest.latLng.longitude
            )
        )
    }

    fun setSelectedPOI(pointOfInterest: PointOfInterest) {
        _selectedPOILiveData.value = pointOfInterest
    }

    fun setSelectedLocationLatLngAndShowName(latLng: LatLng) {
        _selectedLocationLatLngLiveData.value = latLng
        startFetchAddressWorker(
            LatLng(
                selectedLocationLatLngLiveData.value!!.latitude, selectedLocationLatLngLiveData.value!!.longitude
            ),
        )
    }

    fun createGeofenceAfterGrantPermission() {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mApp)) {
            showToast.postValue(mApp.getString(R.string.msg_enable_background_location_permission))
            return
        }
        NotificationUtils.createChannel(mApp)
        val title = reminderTitleStateFlow.value
        val description = reminderDescriptionStateFlow.value
        val location = reminderSelectedLocationStrStateFlow.value
        val latitude = selectedPOILiveData.value!!.latLng.latitude
        val longitude = selectedPOILiveData.value!!.latLng.longitude
        val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
        saveReminder(reminderDataItem)
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem , userId : String? = AppSharedMethods.getCurrentUserId()) {
        showLoading.postValue(true)
        viewModelScope.launch {
            mRemindersLocalRepository.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    userId!!,
                    reminderData.id,
                )
            )
            showLoading.postValue(false)
            showToastInt.postValue(R.string.msg_reminder_saved)
            _createGeofenceSingleLiveEvent.value = (reminderData)
            continueSaveReminder(reminderData)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */

    fun navigateToLastMarkedLocation() {
        _moveMapSingleLiveEvent.value = true
    }

    fun saveLocation() {
        selectedPOILiveData.value?.let {
            _reminderSelectedLocationStrStateFlow.value = selectedPOILiveData.value!!.name
            _saveLocationSingleLiveEvent.value = true
        } ?: showSnackBarInt.postValue(R.string.msg_select_location)
    }

    fun setCurrentMapStyle(style: Int) {
        _currentMapStyleLiveData.value = style
    }

    @SuppressLint("MissingPermission")
    private fun continueSaveReminder(reminderDataItem: ReminderDataItem) {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mApp)) {
            showToastInt.value = R.string.msg_location_required_for_create_geofence_error
            return
        }
        val geofence = Geofence.Builder().setRequestId(reminderDataItem.id).setCircularRegion(
            reminderDataItem.latitude!!,
            reminderDataItem.longitude!!,
            Constants.GEOFENCE_RADIUS_IN_METERS
        ).setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build()

        val geofencingRequest =
            GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build()

        mGeofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                mGeofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        showToastInt.value = R.string.msg_geofences_added
                        Timber.d("Add Geofence", geofence.requestId)
                        navigationCommand.value = NavigationCommand.Back
                    }
                    addOnFailureListener {
                        showToastInt.value = R.string.msg_geofences_not_added
                        if ((it.message != null)) {
                            Timber.w(it.message + "")
                        }
                    }
                }
            }
        }
    }

    fun selectLocationClick() {
        navigationCommand.value = NavigationCommand.To(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
    }

    fun removeGeofences() {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mApp)) {
            return
        }
        mGeofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d(mApp.getString(R.string.msg_geofences_removed))
                showToastInt.value = R.string.msg_geofences_removed
            }
            addOnFailureListener {
                Timber.d(mApp.getString(R.string.msg_geofences_not_removed))
            }
        }
    }

    fun getLastUserLocation() {
        if (isForegroundPermissionGranted(mApp)) {
            viewModelScope.launch(Dispatchers.IO) {
                mRemindersLocalRepository.getLastUserLocation().let {
                    if (it is Result.Success) {
                        it.data.collect { location ->
                            _lastUserLocationStateFlow.value = location
                        }
                    } else {
                        _lastUserLocationStateFlow.value = null
                    }
                }
            }
        }
    }
}