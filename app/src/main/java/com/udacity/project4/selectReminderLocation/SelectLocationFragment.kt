package com.udacity.project4.selectReminderLocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.AppSharedMethods.getLocationNameReceiver
import com.udacity.project4.utils.AppSharedMethods.isLocationEnabled
import com.udacity.project4.utils.AppSharedMethods.isForegroundPermissionGranted
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.MyResultIntentReceiver
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, MyResultIntentReceiver.Receiver {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var mBinding: FragmentSelectLocationBinding
    private val mSharedViewModel: MainViewModel by inject()
    private lateinit var mActivity: Activity
    private lateinit var mGoogleMap: GoogleMap
    private val mFusedLocationProviderClient: FusedLocationProviderClient by inject()
    private var mSelectedLocation: LatLng? = null
    private var mResultReceiver: MyResultIntentReceiver? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mSharedViewModel.setHideToolbar(false)
        mBinding.viewModel = _viewModel
        mBinding.lifecycleOwner = viewLifecycleOwner
        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.text_select_location))

        // TODO: add the map setup implementation
        // TODO: zoom to the user location after taking his permission
        // TODO: add style to the map
        // TODO: put a marker to location that the user selected

        // TODO: call this function after the user confirms on the selected location
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (AppSharedMethods.isForegroundPermissionGranted(mActivity)) {

            checkDeviceLocationSettings()

        } else {
            Timber.d("Permission not granted")
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

//        if (mFusedLocationProviderClient == null) mFusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(mActivity)


        if (mResultReceiver == null) {
            mResultReceiver = MyResultIntentReceiver(Handler())

            mResultReceiver!!.setReceiver(this)

        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initViewModelObserver() {

        _viewModel.selectedPOI.observe(viewLifecycleOwner) {
            if (it != null) {

                mGoogleMap.clear()

                val poiMarker = mGoogleMap.addMarker(
                    MarkerOptions().position(it.latLng).title(it.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))

                )
                mSelectedLocation = it.latLng

                if (context != null) requireContext().getLocationNameReceiver(
                    LatLng(
                        mSelectedLocation!!.latitude, mSelectedLocation!!.longitude
                    ), mResultReceiver!!
                )

                poiMarker!!.showInfoWindow()

            }
        }

        _viewModel.moveMap.observe(viewLifecycleOwner) {
            if (it) {
                updateLocation()
                _viewModel.onNavigationComplete()
            }
        }
        _viewModel.saveLocation.observe(viewLifecycleOwner) {
            if (it) {
                onLocationSelected()
            }
        }

    }


    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.onLocationSaved()
        _viewModel.navigationCommand.value = NavigationCommand.Back
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
                _viewModel.showToast.value =
                    mActivity.getString(R.string.text_msg_foreground_location_services)
                initMap()

            }
        }

    private fun requestPermission(permission: String) {
        requestPermissionLauncher.launch(permission)
    }

    override fun onMapReady(googleMap: GoogleMap) {


        Timber.d("onMapReady:called")

        mGoogleMap = googleMap

        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        setMapStyle(mGoogleMap)

        updateLocationUI()

        initViewModelObserver()

        getLastUserLocation()

//        mGoogleMap.setOnCameraIdleListener {
//
//            Timber.d("onCameraIdle:called")
//
//            requireContext().getLocationNameReceiver(
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

        mGoogleMap.setOnPoiClickListener { poi ->
            _viewModel.setSelectedPOI(
                PointOfInterest(poi.latLng, poi.placeId, poi.name)
            )
        }
        mGoogleMap.setOnMapLongClickListener {
            _viewModel.setSelectedPOI(
                PointOfInterest(it, UUID.randomUUID().toString(), "")
            )

            requireContext().getLocationNameReceiver(
                LatLng(
                    it.latitude, it.longitude
                ), mResultReceiver!!
            )
        }
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

    private fun getLastUserLocation() {
        if (isForegroundPermissionGranted(mActivity)) {
            try {
                mFusedLocationProviderClient.lastLocation.addOnSuccessListener(
                    mActivity
                ) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {

                        mSelectedLocation = LatLng(location.latitude, location.longitude)

                        Timber.d(
                            "getLastUserLocation:mLastKnownLocation: " + mSelectedLocation.toString()
                        )
                        mGoogleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mSelectedLocation!!.latitude, mSelectedLocation!!.longitude
                                ), Constants.Current_Location_ZOOM.toFloat()
                            )
                        )
//                        mGoogleMap.addMarker(MarkerOptions().position(mSelectedLocation!!))


                        //
                        //                                    setMyLocationMarker(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                        context?.let {
                            requireContext().getLocationNameReceiver(
                                LatLng(
                                    mSelectedLocation!!.latitude, mSelectedLocation!!.longitude
                                ), mResultReceiver!!
                            )
                        }
                    } else {
                        Timber.d("getLastUserLocation:currentLocation NULL")
                        setDefaultLocation()
                        if (!isLocationEnabled(mActivity)) {
                            _viewModel.showToast.value =
                                mActivity.getString(R.string.text_enable_gps_msg)
                        } else {
                            //                                        showGPSConnectivityLayout();
                            //                                        validateGPSConnectivityLayout(0);
                        }
                    }
                }.addOnFailureListener(mActivity) { e ->
                    Timber.d("Current location is null. Using defaults.")
                    Timber.e(e)
                    setDefaultLocation()
                    //                        validateGPSConnectivityLayout(0);
                }
            } catch (e: SecurityException) {
                Timber.e(e.message!!)
            }
        }
    }

    private fun setDefaultLocation() {
        mGoogleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                Constants.mDefaultLocation, Constants.Default_Location_ZOOM.toFloat()
            )
        )
        mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, Constants.MAX_LOCATION_UPDATE_INTERVAL
        ).setMinUpdateIntervalMillis(Constants.MIN_LOCATION_UPDATE_INTERVAL).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResultLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.d("Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                initMap()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                initMap()
            }
        }
    }

    private val resolutionForResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Location settings are satisfied; handle accordingly
                initMap()
            } else {
                // Location settings were not satisfied; handle accordingly
                initMap()
            }
        }

    private fun updateLocation() {
        if (mSelectedLocation != null) {
            Timber.d("updateLocation:mSelectedLocation " + mSelectedLocation!!.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    mSelectedLocation!!.latitude, mSelectedLocation!!.longitude
                ), Constants.Current_Location_ZOOM.toFloat()
            )
            mGoogleMap.animateCamera(cameraUpdate)
            //            validateGPSConnectivityLayout(1);
        } else {
            getLastUserLocation()
        }
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        Timber.d("onReceiveResult:called")

        if (resultData == null) {
            mBinding.progressBarLoading.setVisibility(View.INVISIBLE)
            mBinding.textViewLocationName.setVisibility(View.VISIBLE)
            mBinding.textViewLocationName.setText(mActivity.getString(R.string.msg_address_location_network_issue))
            return
        }

        val mAddressOutput = resultData.getString(Constants.EXTRA_RESULT_DATA_KEY)
        mBinding.progressBarLoading.setVisibility(View.INVISIBLE)
        when (resultCode) {
            Constants.SUCCESS_RESULT -> {
                mBinding.textViewLocationName.setVisibility(View.VISIBLE)
                mBinding.textViewLocationName.setText(mAddressOutput)

                if (_viewModel.selectedPOI.value != null && _viewModel.selectedPOI.value!!.name.isEmpty()) {
                    _viewModel.setSelectedPOI(
                        PointOfInterest(
                            _viewModel.selectedPOI.value!!.latLng,
                            _viewModel.selectedPOI.value!!.placeId,
                            mAddressOutput.toString()
                        )
                    )
                }
            }

            Constants.FAILURE_RESULT -> {
                mBinding.textViewLocationName.setVisibility(View.VISIBLE)
                mBinding.textViewLocationName.setText(mAddressOutput)
            }

            else -> {
            }
        }

        mBinding.textViewLocationName.contentDescription = mAddressOutput
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume:called")

    }

    override fun onDestroy() {
        super.onDestroy()
        mResultReceiver?.setReceiver(null)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    mActivity, R.raw.map_style
                )
            )
            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e("Can't find style. Error: ", e)
        }
    }

}