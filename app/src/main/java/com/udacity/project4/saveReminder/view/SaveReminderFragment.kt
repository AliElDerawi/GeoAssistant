package com.udacity.project4.saveReminder.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.data.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setStatusStyle
import com.udacity.project4.utils.setTitle
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var mBinding: FragmentSaveReminderBinding
    private val mSharedViewModel: MainViewModel by inject()
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var mActivity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(mActivity, GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(mActivity, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geofencingClient = LocationServices.getGeofencingClient(mActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mSharedViewModel.setHideToolbar(false)
        setDisplayHomeAsUpEnabled(true)
        setTitle(mActivity.getString(R.string.text_add_reminder))
        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.viewModel = _viewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModelObservers()

//        mBinding.saveReminder.setOnClickListener {
//
//            // TODO: use the user entered reminder details to:
//            //  1) add a geofencing request
//            //  2) save the reminder to the local db
//        }
    }

    @SuppressLint("MissingPermission")
    private fun continueSaveReminder(reminderDataItem: ReminderDataItem) {

        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mActivity)) {
            _viewModel.showToast.value =
                mActivity.getString(R.string.location_required_for_create_geofence_error)
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
                        _viewModel.showToast.value = mActivity.getString(R.string.geofences_added)
                        Timber.d("Add Geofence", geofence.requestId)
                        _viewModel.onCreateGeofenceComplete()
                        _viewModel.navigationCommand.value = NavigationCommand.Back
                    }
                    addOnFailureListener {
                        _viewModel.onCreateGeofenceComplete()
                        _viewModel.showToast.value =
                            mActivity.getString(R.string.geofences_not_added)
                        if ((it.message != null)) {
                            Timber.w(it.message + "")
                        }
                    }
                }
            }
        }
    }

    private fun initViewModelObservers() {

        with(_viewModel) {
            completeSaveReminder.observe(viewLifecycleOwner) {
                if (it) {
                    continueSaveReminder(_viewModel.createGeofence.value!!)
                }
            }

            saveReminder.observe(viewLifecycleOwner) {
                Timber.d("saveReminder:called:saveReminder: " + it)
                if (it) {
                    if (AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mActivity)) {
                        Timber.d("Foreground and Background Permission granted")
                        if (AppSharedMethods.isLocationEnabled(mActivity)) {
                            handleNotificationPermission()
                        } else {
                            checkDeviceLocationSettings()
                        }
                    } else if (!AppSharedMethods.isForegroundPermissionGranted(mActivity)) {
                        Timber.d("Foreground Permission needed request again only once")
                        requestForegroundPermission()
                    } else if (!AppSharedMethods.isBackgroundPermissionGranted(mActivity)) {
                        Timber.d("Background Permission needed request again only once")
                        requestBackgroundPermission()
                    }
                }
            }

            selectLocation.observe(viewLifecycleOwner) {
                if (it) {
                    _viewModel.navigationCommand.value =
                        NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
                }
            }

            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    isCreateReminderEnabled.collect { isEnabled ->
                        mBinding.saveReminder.setStatusStyle(isEnabled)
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        removeGeofences()
    }

    private fun removeGeofences() {
        if (!AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mActivity)) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d(mActivity.getString(R.string.geofences_removed))
                _viewModel.showToast.value = mActivity.getString(R.string.geofences_removed)
            }
            addOnFailureListener {
                Timber.d(mActivity.getString(R.string.geofences_not_removed))
            }
        }
    }

    private fun requestForegroundPermission() {
        requestForegroundPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun requestBackgroundPermission() {
        requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            if (AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mActivity)) {
                Timber.d("Foreground and Background Permission granted")
                checkDeviceLocationSettings()
            } else {
                if (AppSharedMethods.showBackgroundLocationRequestPermission(mActivity)) {
                    Timber.d("Background Permission needed request again only once")
                    requestBackgroundPermission()
                    _viewModel.showToast.value =
                        mActivity.getString(R.string.location_required_for_create_geofence_error)
                } else {
                    _viewModel.showToast.value =
                        mActivity.getString(R.string.location_required_for_create_geofence_error)
                }
            }
        }

    private val requestForegroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            if (AppSharedMethods.isForegroundPermissionGranted(mActivity)) {
                Timber.d("Foreground Permission granted")
                requestBackgroundPermission()
            } else {
                if (AppSharedMethods.showForegroundLocationRequestPermission(mActivity)) {
                    Timber.d("Foreground Permission needed request again only once")
                    requestForegroundPermission()
                    _viewModel.showToast.value =
                        mActivity.getString(R.string.location_required_for_create_geofence_error)
                } else {
                    _viewModel.showToast.value =
                        mActivity.getString(R.string.location_required_for_create_geofence_error)
                }
            }
        }

    private val requestPostNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Timber.d("Post Permission granted")
                _viewModel.createGeofenceAfterGrantPermission()
            } else {
                Timber.d("Post Permission denied")
                _viewModel.showToast.value =
                    mActivity.getString(R.string.text_msg_cant_post_notification)
                _viewModel.createGeofenceAfterGrantPermission()
            }
        }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_LOW_POWER, Constants.MAX_LOCATION_UPDATE_INTERVAL
        ).setMinUpdateIntervalMillis(Constants.MIN_LOCATION_UPDATE_INTERVAL).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Create an IntentSenderRequest and launch it using ActivityResultLauncher
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResultLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.d("Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                if (AppSharedMethods.isLocationEnabled(requireContext())) {
                    handleNotificationPermission()
                } else {
                    Snackbar.make(
                        mBinding.saveReminder,
                        getString(R.string.location_required_for_create_geofence_error),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettings()
                    }.setAction(android.R.string.cancel) {
                        _viewModel.showToast.value =
                            getString(R.string.location_required_for_create_geofence_error)
                    }.show()
                }
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                handleNotificationPermission()
            }
        }
    }

    private val resolutionForResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Location settings are satisfied; handle accordingly
                handleNotificationPermission()
            } else {
                // Location settings were not satisfied; handle accordingly
                Snackbar.make(
                    mBinding.saveReminder,
                    getString(R.string.location_required_for_create_geofence_error),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.setAction(android.R.string.cancel) {
                    _viewModel.showToast.value =
                        getString(R.string.location_required_for_create_geofence_error)
                }.show()
            }
        }

    private fun handleNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!AppSharedMethods.isReceiveNotificationPermissionGranted(mActivity)) {
                requestPostNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                _viewModel.createGeofenceAfterGrantPermission()
            }
        } else {
            _viewModel.createGeofenceAfterGrantPermission()
        }
    }

}