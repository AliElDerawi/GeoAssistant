package com.udacity.project4.features.saveReminder.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.data.base.BaseFragment
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.features.main.viewModel.MainViewModel
import com.udacity.project4.features.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.AppSharedMethods.getSnackBar
import com.udacity.project4.utils.AppSharedMethods.isLocationEnabled
import com.udacity.project4.utils.AppSharedMethods.setStatusStyle
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {

    override val mViewModel: SaveReminderViewModel by koinNavGraphViewModel(R.id.save_reminder_graph)
    private val mSharedViewModel: MainViewModel by activityViewModel()
    private lateinit var mBinding: FragmentSaveReminderBinding
    private lateinit var mActivity: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSaveReminderBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mViewModel
        }
        mSharedViewModel.apply {
            setHideToolbar(false)
            setToolbarTitle(mActivity.getString(R.string.app_name))
        }
        mActivity.setDisplayHomeAsUpEnabled(true)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModelObservers()
//        mBinding.saveReminder.setOnClickListener {
//            // TODO - Completed: use the user entered reminder details to:
//            //  1) add a geofencing request
//            //  2) save the reminder to the local db
//        }
    }

    private fun initViewModelObservers() {
        with(mViewModel) {

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                    launch {
                        saveReminderChannel.receiveAsFlow().collect { saveReminder ->
                            if (saveReminder) {
                                if (AppSharedMethods.isForegroundAndBackgroundPermissionGranted(
                                        mActivity
                                    )
                                ) {
                                    Timber.d("Foreground and Background Permission granted")
                                    if (mActivity.isLocationEnabled()) {
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
                    }

                    launch {
                        isCreateReminderEnabledStateFlow.collect { isEnabled ->
                            mBinding.saveReminder.setStatusStyle(isEnabled)
                        }
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        Timber.d("onDestroy called")
        // Make sure to clear the view model after destroy, as it's a single view model.
        mViewModel.removeGeofences()
        super.onDestroy()
    }

    private fun requestForegroundPermission() {
        requestForegroundPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MaterialAlertDialogBuilder(mActivity)
                .setTitle(mActivity.getString(R.string.title_background_location_permission)) // أضف النصوص في ملف strings.xml
                .setMessage(mActivity.getString(R.string.desc_background_location_permission))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                    mViewModel.sendToast(R.string.msg_background_location_required)
                }
                .show()
        }
    }

    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (AppSharedMethods.isForegroundAndBackgroundPermissionGranted(mActivity)) {
                Timber.d("Foreground and Background Permission granted")
                checkDeviceLocationSettings()
            } else {
                if (AppSharedMethods.shouldShowBackgroundLocationRequestPermission(mActivity)) {
                    Timber.d("Background Permission needed request again only once")
                    requestBackgroundPermission()
                    mViewModel.sendToast(R.string.msg_location_required_for_create_geofence_error)
                } else {
                    mViewModel.sendToast(R.string.msg_location_required_for_create_geofence_error)
                }
            }
        }

    private val requestForegroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (AppSharedMethods.isForegroundPermissionGranted(mActivity)) {
                Timber.d("Foreground Permission granted")
                requestBackgroundPermission()
            } else {
                if (AppSharedMethods.shouldShowForegroundLocationRequestPermission(mActivity)) {
                    Timber.d("Foreground Permission needed request again only once")
                    requestForegroundPermission()
                    mViewModel.sendToast(R.string.msg_location_required_for_create_geofence_error)
                } else {
                    mViewModel.sendToast(R.string.msg_location_required_for_create_geofence_error)
                }
            }
        }

    private val requestPostNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Timber.d("Post Permission granted")
                mViewModel.createGeofenceAfterGrantPermission()
            } else {
                Timber.d("Post Permission denied")
                mViewModel.sendToast(R.string.msg_cant_post_notification)
            }
        }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_LOW_POWER, Constants.MAX_LOCATION_UPDATE_INTERVAL
        ).setMinUpdateIntervalMillis(Constants.MIN_LOCATION_UPDATE_INTERVAL).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                handleNotificationPermission()
            } else {
                task.exception?.let { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(exception.resolution).build()
                            resolutionForResultLauncher.launch(intentSenderRequest)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Timber.d("Error getting location settings resolution: ${sendEx.message}")
                        }
                    } else {
                        if (mActivity.isLocationEnabled()) {
                            handleNotificationPermission()
                        } else {
                            showEnableLocationSnackBar()
                        }
                    }
                }
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
                showEnableLocationSnackBar()
            }
        }

    private fun showEnableLocationSnackBar() {
        mActivity.getSnackBar(
            mActivity.getString(R.string.msg_location_required_for_create_geofence_error),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            checkDeviceLocationSettings()
        }.setAction(android.R.string.cancel) {
            mViewModel.sendToast(R.string.msg_location_required_for_create_geofence_error)
        }.show()
    }

    private fun handleNotificationPermission() {
        if (AppSharedMethods.isSupportsAndroid33() &&
            !AppSharedMethods.isReceiveNotificationPermissionGranted(mActivity)
        ) {
            requestPostNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            mViewModel.createGeofenceAfterGrantPermission()
        }
    }

}
