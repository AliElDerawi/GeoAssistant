package com.udacity.project4.features.saveReminder.viewModel

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.data.base.BaseViewModel
import com.udacity.project4.data.base.NavigationCommand
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.AppSharedMethods.isForegroundPermissionGranted
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import com.udacity.project4.data.dto.Result
import com.udacity.project4.features.saveReminder.view.SaveReminderFragmentDirections
import com.udacity.project4.utils.AppSharedMethods.startFetchAddressWorker
import kotlinx.coroutines.channels.Channel

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
        _reminderTitleStateFlow,
        _reminderDescriptionStateFlow,
        _reminderSelectedLocationStrStateFlow
    ) { title, description, location ->
        !title.isNullOrEmpty() && !description.isNullOrEmpty() && !location.isNullOrEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private var _selectedPOIMutableStateFlow = MutableStateFlow<PointOfInterest?>(null)
    val selectedPOIStateFlow: StateFlow<PointOfInterest?>
        get() = _selectedPOIMutableStateFlow

    private var _moveMapChannel = Channel<Boolean>(Channel.BUFFERED)
    val moveMapSingleChannel: Channel<Boolean>
        get() = _moveMapChannel

    private var _saveLocationChannel = Channel<Boolean>(Channel.BUFFERED)
    val saveLocationChannel: Channel<Boolean>
        get() = _saveLocationChannel

    private var _saveReminderChannel = Channel<Boolean>(Channel.BUFFERED)
    val saveReminderChannel: Channel<Boolean>
        get() = _saveReminderChannel

    private var _createGeofenceStateFlow = MutableStateFlow<ReminderDataItem?>(null)
    val createGeofenceStateFlow: StateFlow<ReminderDataItem?>
        get() = _createGeofenceStateFlow

    private var _lastUserLocationStateFlow = MutableStateFlow<Location?>(null)
    val lastUserLocationStateFlow: StateFlow<Location?>
        get() = _lastUserLocationStateFlow

    private var _selectedLocationLatLngStateFlow = MutableStateFlow<LatLng?>(null)
    val selectedLocationLatLngStateFlow: StateFlow<LatLng?>
        get() = _selectedLocationLatLngStateFlow

    private var _currentMapStyleStateFlow = MutableStateFlow<Int>(R.id.normal_map)
    val currentMapStyleStateFlow: StateFlow<Int>
        get() = _currentMapStyleStateFlow


    fun onClear() {
        _reminderTitleStateFlow.value = null
        _reminderDescriptionStateFlow.value = null
        _reminderSelectedLocationStrStateFlow.value = null
        _selectedPOIMutableStateFlow.value = null
    }


    override fun onCleared() {
        super.onCleared()
        onClear()
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
            _reminderTitleStateFlow.value.isNullOrEmpty() -> sendToast(R.string.msg_enter_title)
            _reminderDescriptionStateFlow.value.isNullOrEmpty() -> sendToast(
                R.string.msg_please_enter_description
            )

            _reminderSelectedLocationStrStateFlow.value.isNullOrEmpty() -> sendToast(
                R.string.msg_select_location
            )

            else -> {
                viewModelScope.launch {
                    _saveReminderChannel.send(true)
                }
            }

        }
    }

    fun setSelectedPOIAndShowName(pointOfInterest: PointOfInterest) {
        _selectedPOIMutableStateFlow.value = pointOfInterest

        startFetchAddressWorker(
            LatLng(
                pointOfInterest.latLng.latitude, pointOfInterest.latLng.longitude
            )
        )
    }

    fun setSelectedPOI(pointOfInterest: PointOfInterest) {
        _selectedPOIMutableStateFlow.value = pointOfInterest
    }

    fun setSelectedLocationLatLngAndShowName(latLng: LatLng) {
        _selectedLocationLatLngStateFlow.value = latLng
        startFetchAddressWorker(
            LatLng(
                selectedLocationLatLngStateFlow.value!!.latitude,
                selectedLocationLatLngStateFlow.value!!.longitude
            ),
        )
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun createGeofenceAfterGrantPermission() {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mApp)) {
            viewModelScope.launch {
                showToast.send(getLocalizedContext().getString(R.string.msg_enable_background_location_permission))
            }
            return
        }
        val currentPOI = selectedPOIStateFlow.value ?: return

        NotificationUtils.createChannel(mApp)
        val title = reminderTitleStateFlow.value
        val description = reminderDescriptionStateFlow.value
        val location = reminderSelectedLocationStrStateFlow.value

        val latitude = currentPOI.latLng.latitude
        val longitude = currentPOI.latLng.longitude

        val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
        saveReminder(reminderDataItem)
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(
        reminderData: ReminderDataItem,
        userId: String? = AppSharedMethods.getCurrentUserId()
    ) {
        viewModelScope.launch {
            showLoading.value = true
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
            showLoading.value = false
            showToastInt.send(R.string.msg_reminder_saved)
            _createGeofenceStateFlow.value = reminderData
            continueSaveReminder(reminderData)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */

    fun navigateToLastMarkedLocation() {
        viewModelScope.launch {
            _moveMapChannel.send(true)
        }
    }

    fun saveLocation() {
        selectedPOIStateFlow.value?.let {
            _reminderSelectedLocationStrStateFlow.value = selectedPOIStateFlow.value!!.name
            viewModelScope.launch {
                _saveLocationChannel.send(true)
            }
        } ?: viewModelScope.launch {
            showSnackBarInt.send(R.string.msg_select_location)
        }
    }

    fun setCurrentMapStyle(style: Int) {
        _currentMapStyleStateFlow.value = style
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun continueSaveReminder(reminderDataItem: ReminderDataItem) {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mApp)) {
            sendToast(R.string.msg_location_required_for_create_geofence_error)
            return
        }
        val geofence = Geofence.Builder().setRequestId(reminderDataItem.id).setCircularRegion(
            reminderDataItem.latitude!!,
            reminderDataItem.longitude!!,
            Constants.GEOFENCE_RADIUS_IN_METERS
        ).setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build()

        Timber.d("Geofence created: ${geofence.requestId} " + "reminderId: ${reminderDataItem.id}")

        val geofencingRequest =
            GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build()

        mGeofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                mGeofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        sendToast(R.string.msg_geofences_added)
                        Timber.d("Add Geofence: $geofence.requestId")
                        viewModelScope.launch {
                            navigationCommand.send(NavigationCommand.Back)
                        }
                    }
                    addOnFailureListener {
                        sendToast(R.string.msg_geofences_not_added)
                        if ((it.message != null)) {
                            Timber.w(it.message + "")
                        }
                    }
                }
            }
        }
    }

    fun selectLocationClick() {
        viewModelScope.launch {
            navigationCommand.send(
                NavigationCommand.To(
                    SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
                )
            )
        }
    }

    fun onLocationSelected() {
        viewModelScope.launch {
            navigationCommand.send(NavigationCommand.Back)
        }
    }

    fun sendToast(@StringRes messageResId: Int) {
        viewModelScope.launch {
            showToastInt.send(messageResId)
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun removeGeofences() {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mApp)) {
            return
        }
        mGeofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d(getLocalizedContext().getString(R.string.msg_geofences_removed))
//                if (BuildConfig.DEBUG) {
//                    showToastInt.value = R.string.msg_geofences_removed
//                }
            }
            addOnFailureListener {
                Timber.d(getLocalizedContext().getString(R.string.msg_geofences_not_removed))
            }
        }
    }

    fun getCurrentUserLocation() {
        if (lastUserLocationStateFlow.value != null) return
        if (isForegroundPermissionGranted(mApp)) {
            viewModelScope.launch(Dispatchers.IO) {
                mRemindersLocalRepository.getCurrentUserLocation().let {
                    if (it is Result.Success) {
                        _lastUserLocationStateFlow.value = it.data
                    } else {
                        _lastUserLocationStateFlow.value = null
                    }
                }
            }
        }
    }
}
