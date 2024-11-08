package com.udacity.project4.selectReminderLocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.view.*
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.AppSharedMethods.getLocationNameReceiver
import com.udacity.project4.utils.AppSharedMethods.isLocationEnabled
import com.udacity.project4.utils.AppSharedMethods.isForegroundPermissionGranted
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.MyResultIntentReceiver
import com.udacity.project4.utils.addMarkerWithName
import com.udacity.project4.utils.animateCameraToLocation
import com.udacity.project4.utils.moveCameraToLocation
import com.udacity.project4.utils.setCustomMapStyle
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, MyResultIntentReceiver.Receiver {

    // Use Koin to get the view model of the SaveReminder
    override val mViewModel: SaveReminderViewModel by inject()
    private lateinit var mBinding: FragmentSelectLocationBinding
    private val mSharedViewModel: MainViewModel by inject()
    private lateinit var mActivity: FragmentActivity
    private lateinit var mGoogleMap: GoogleMap
    private val mResultReceiver: MyResultIntentReceiver by inject()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // TODO: add the map setup implementation
        // TODO: zoom to the user location after taking his permission
        // TODO: add style to the map
        // TODO: put a marker to location that the user selected
        // TODO: call this function after the user confirms on the selected location
        mBinding = FragmentSelectLocationBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mViewModel
        }
        mSharedViewModel.setHideToolbar(false)
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.text_select_location))
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isForegroundPermissionGranted(mActivity)) {
            checkDeviceLocationSettings()
        } else {
            Timber.d("Permission not granted")
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun initMap() {
        mResultReceiver.setReceiver(this@SelectLocationFragment)
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).apply {
            getMapAsync(this@SelectLocationFragment)
        }
    }

    private fun initViewModelObserver() {
        with(mViewModel) {
            selectedPOI.observe(viewLifecycleOwner) {
                it?.let {
                    mGoogleMap.clear()
                    val poiMarker = mGoogleMap.addMarkerWithName(it.latLng, it.name)
                    mViewModel.setSelectedLocationLatLngAndShowName(it.latLng)
                    poiMarker?.showInfoWindow()
                }
            }
            moveMap.observe(viewLifecycleOwner) {
                if (it) {
                    updateLocation()
                }
            }
            saveLocation.observe(viewLifecycleOwner) {
                if (it) {
                    onLocationSelected()
                }
            }
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    lastUserLocationFlow.collect { location ->
                        location?.let {
                            mViewModel.setSelectedLocationLatLngAndShowName(LatLng(it.latitude, it.longitude))
                            Timber.d("getLastUserLocation:mLastKnownLocation: $mViewModel.selectedLocationLatLng.value!!")
                            mGoogleMap.moveCameraToLocation(
                                mViewModel.selectedLocationLatLng.value!!,
                                Constants.CURRENT_LOCATION_ZOON
                            )
                        } ?: run {
                            Timber.d("getLastUserLocation:currentLocation NULL")
                            setDefaultLocation()
                            if (!isLocationEnabled(mActivity)) {
                                mViewModel.showToast.value =
                                    mActivity.getString(R.string.text_enable_gps_msg)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
        mViewModel.navigationCommand.value = NavigationCommand.Back
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            mGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            mGoogleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            mGoogleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Timber.d("Location Permission granted")
                checkDeviceLocationSettings()
            } else {
                mViewModel.showToast.value =
                    mActivity.getString(R.string.text_msg_foreground_location_services)
                initMap()
            }
        }

    private fun requestPermission(permission: String) {
        requestPermissionLauncher.launch(permission)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Timber.d("onMapReady:called")
        mGoogleMap = googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnPoiClickListener { poi ->
                mViewModel.setSelectedPOIAndShowName(
                    PointOfInterest(poi.latLng, poi.placeId, poi.name)
                )
            }
            setOnMapLongClickListener {
                mViewModel.setSelectedPOIAndShowName(
                    PointOfInterest(it, UUID.randomUUID().toString(), "")
                )
            }
            setCustomMapStyle(R.raw.map_style)
        }
        updateLocationUI()
        initViewModelObserver()
        mViewModel.getLastUserLocation()

//      TODO : Comment: Logic if we need to update the location name on camera move
//        mGoogleMap.setOnCameraIdleListener {
//
//            Timber.d("onCameraIdle:called")
//
//            getLocationNameReceiver(
//                LatLng(
//                    mGoogleMap.cameraPosition.target.latitude,
//                    mGoogleMap.cameraPosition.target.longitude
//                ), mResultReceiver!!
//            )
//        }
//        mGoogleMap.setOnCameraMoveStartedListener { reason ->
//            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
//                Timber.d("onCameraMoveStarted:REASON_GESTURE")
//                mBinding.progressBarLoading.setVisibility(View.VISIBLE)
//                mBinding.textViewLocationName.setVisibility(View.GONE)
//            } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
//                Timber.d("onCameraMoveStarted:REASON_API_ANIMATION")
//            } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
//                Timber.d("onCameraMoveStarted:REASON_DEVELOPER_ANIMATION")
//            }
//        }
//        mGoogleMap.setOnCameraMoveListener {
//            Timber.d("onCameraMove:called")
//            if (mBinding.progressBarLoading.getVisibility() == View.INVISIBLE) {
//                mBinding.progressBarLoading.setVisibility(View.VISIBLE)
//                mBinding.textViewLocationName.setVisibility(View.GONE)
//            }
//        }
//        mGoogleMap.setOnCameraMoveCanceledListener {
//            Timber.d("onCameraMoveCanceled:called")
//        }
    }

    private fun updateLocationUI() {
        try {
            if (isForegroundPermissionGranted(mActivity)) {
                mGoogleMap.isMyLocationEnabled = true
                mBinding.fabSelectLocation.visibility = View.VISIBLE
            } else {
                mBinding.fabSelectLocation.visibility = View.GONE
            }
        } catch (e: SecurityException) {
            Timber.e("Exception: %s", e.message!!)
        }
    }

    private fun setDefaultLocation() {
        mGoogleMap.apply {
            animateCameraToLocation(Constants.MY_DEFAULT_LOCATION, Constants.DEFAULT_LOCATION_ZOOM)
            uiSettings.isMyLocationButtonEnabled = false
        }
    }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, Constants.MAX_LOCATION_UPDATE_INTERVAL
        ).setMinUpdateIntervalMillis(Constants.MIN_LOCATION_UPDATE_INTERVAL).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                initMap()
            } else {
                task.exception?.let { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(exception.resolution).build()
                            resolutionForResultLauncher.launch(intentSenderRequest)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Timber.d("Error getting location settings resolution: " + sendEx.message)
                        }
                    } else {
                        initMap()
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
                initMap()
            } else {
                // Location settings were not satisfied; Allow user to choose location manually
                initMap()
            }
        }

    private fun updateLocation() {
        mViewModel.selectedLocationLatLng.value?.let {
            Timber.d("updateLocation:mSelectedLocation ${it.longitude}")
            mGoogleMap.animateCameraToLocation(it, Constants.CURRENT_LOCATION_ZOON)
        } ?: mViewModel.getLastUserLocation()
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        Timber.d("onReceiveResult:called")

        val mAddressOutput = resultData?.getString(Constants.EXTRA_RESULT_DATA_KEY)
        mBinding.progressBarLoading.visibility = View.INVISIBLE
        mBinding.textViewLocationName.visibility = View.VISIBLE
        mBinding.textViewLocationName.text =
            mAddressOutput ?: mActivity.getString(R.string.msg_address_location_network_issue)

        if (resultCode == Constants.SUCCESS_RESULT && mViewModel.selectedPOI.value != null && mViewModel.selectedPOI.value!!.name.isEmpty()) {
            Timber.d("onReceiveResult:called:updateName")
            mViewModel.setSelectedPOI(
                PointOfInterest(
                    mViewModel.selectedPOI.value!!.latLng,
                    mViewModel.selectedPOI.value!!.placeId,
                    mAddressOutput.toString()
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume:called")
    }

    override fun onDestroy() {
        super.onDestroy()
        mResultReceiver.setReceiver(null)
    }

}