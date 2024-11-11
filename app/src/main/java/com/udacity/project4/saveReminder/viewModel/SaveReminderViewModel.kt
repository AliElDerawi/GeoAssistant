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
    val app: Application,
    val remindersLocalRepository: ReminderDataSource,
    val geofencingClient: GeofencingClient
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

    private var _moveMap = SingleLiveEvent<Boolean>()
    val moveMap: LiveData<Boolean>
        get() = _moveMap

    private var _saveLocation = SingleLiveEvent<Boolean>()
    val saveLocation: LiveData<Boolean>
        get() = _saveLocation

    private var _saveReminder = SingleLiveEvent<Boolean>()
    val saveReminder: LiveData<Boolean>
        get() = _saveReminder

    private var _createGeofence = SingleLiveEvent<ReminderDataItem?>()
    val createGeofence: LiveData<ReminderDataItem?>
        get() = _createGeofence

    private var _selectLocation = SingleLiveEvent<Boolean>()
    val selectLocation: LiveData<Boolean>
        get() = _selectLocation

    private var _lastUserLocationFlow = MutableStateFlow<Location?>(null)
    val lastUserLocationFlow: StateFlow<Location?>
        get() = _lastUserLocationFlow

    private var _selectedLocationLatLng = MutableLiveData<LatLng?>()
    val selectedLocationLatLng: LiveData<LatLng?>
        get() = _selectedLocationLatLng

    private var _currentMapStyle = MutableLiveData<Int>(R.id.normal_map)
    val currentMapStyle: LiveData<Int>
        get() = _currentMapStyle


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

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(app, GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    fun onTitleTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _reminderTitle.value = s.toString()
    }

    fun onDescriptionTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _reminderDescription.value = s.toString()
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */

    fun onSaveReminderClick() {
        when {
            _reminderTitle.value.isNullOrEmpty() -> showToastInt.postValue(R.string.err_enter_title)
            _reminderDescription.value.isNullOrEmpty() -> showToastInt.postValue(
                R.string.text_msg_please_enter_description
            )
            _reminderSelectedLocationStr.value.isNullOrEmpty() -> showToastInt.postValue(
                R.string.err_select_location
            )
            else -> _saveReminder.postValue(true)
        }
    }

    fun setSelectedPOIAndShowName(pointOfInterest: PointOfInterest) {
        _selectedPOI.value = pointOfInterest
        startFetchAddressWorker(
            LatLng(
                pointOfInterest.latLng.latitude, pointOfInterest.latLng.longitude
            )
        )
    }

    fun setSelectedPOI(pointOfInterest: PointOfInterest) {
        _selectedPOI.value = pointOfInterest
    }

    fun setSelectedLocationLatLngAndShowName(latLng: LatLng) {
        _selectedLocationLatLng.value = latLng
        startFetchAddressWorker(
            LatLng(
                selectedLocationLatLng.value!!.latitude, selectedLocationLatLng.value!!.longitude
            ),
        )
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
                    userId = AppSharedMethods.getCurrentUserId(),
                    reminderData.id,
                )
            )
            showLoading.postValue(false)
            showToastInt.postValue(R.string.reminder_saved)
            _createGeofence.value = (reminderData)
            continueSaveReminder(reminderData)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */

    fun navigateToLastMarkedLocation() {
        _moveMap.value = true
    }

    fun saveLocation() {
        selectedPOI.value?.let {
            _reminderSelectedLocationStr.value = selectedPOI.value!!.name
            _latitude.value = selectedPOI.value!!.latLng.latitude
            _longitude.value = selectedPOI.value!!.latLng.longitude
            _saveLocation.value = true
        } ?: showSnackBarInt.postValue(R.string.err_select_location)
    }

    fun setCurrentMapStyle(style: Int) {
        _currentMapStyle.value = style
    }

    @SuppressLint("MissingPermission")
    private fun continueSaveReminder(reminderDataItem: ReminderDataItem) {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(app)) {
            showToastInt.value = R.string.location_required_for_create_geofence_error
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

        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        showToastInt.value = R.string.geofences_added
                        Timber.d("Add Geofence", geofence.requestId)
                        navigationCommand.value = NavigationCommand.Back
                    }
                    addOnFailureListener {
                        showToastInt.value = R.string.geofences_not_added
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
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(app)) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d(app.getString(R.string.geofences_removed))
                showToastInt.value = R.string.geofences_removed
            }
            addOnFailureListener {
                Timber.d(app.getString(R.string.geofences_not_removed))
            }
        }
    }

    fun getLastUserLocation() {
        if (isForegroundPermissionGranted(app)) {
            viewModelScope.launch(Dispatchers.IO) {
                remindersLocalRepository.getLastUserLocation().let {
                    if (it is Result.Success) {
                        it.data.collect { location ->
                            _lastUserLocationFlow.value = location
                        }
                    } else {
                        _lastUserLocationFlow.value = null
                    }
                }
            }
        }
    }
}