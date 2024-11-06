package com.udacity.project4.remindersList.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.remindersList.adapter.RemindersListAdapter
import com.udacity.project4.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.AppSharedData
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.AppSharedMethods.showForegroundLocationRequestPermission
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var mBinding: FragmentRemindersBinding
    private lateinit var mActivity: FragmentActivity
    private val mSharedViewModel: MainViewModel by inject()
    private val mSaveReminderViewModel: SaveReminderViewModel by inject()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reminders, container, false
        )
        mBinding.lifecycleOwner = this
        mBinding.viewModel = _viewModel
        mSharedViewModel.setHideToolbar(false)
        mSaveReminderViewModel.onClear()
        setDisplayHomeAsUpEnabled(false)
        setTitle(mActivity.getString(R.string.app_name))
        initViewModelObservers()
        mBinding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenu()
        setupRecyclerView()
        initListener()
    }

    @VisibleForTesting
    fun initListener() {
        mBinding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    private fun initViewModelObservers() {
        _viewModel.addReminderLiveData.observe(viewLifecycleOwner) {
            if (it) {
                navigateToAddReminder()
            }
        }
    }

    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        mSharedViewModel.navigationCommand.value = NavigationCommand.To(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            mSharedViewModel.navigationCommand.value =
                NavigationCommand.To(
                    ReminderListFragmentDirections.actionReminderListFragmentToReminderDescriptionFragment(
                        it
                    )
                )
        }
        // Setup the recycler view using the extension function
        mBinding.reminderssRecyclerView.setup(adapter)
    }

    private fun initMenu() {

        val menuHost: MenuHost = mActivity

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // TODO Comment : We can use NavigationUI.onNavDestinationSelected() to handle the navigation
                if (menuItem.itemId == R.id.logout) {
                    AuthUI.getInstance()
                        .signOut(mActivity)
                        .addOnCompleteListener {
                            AppSharedMethods.getSharedPreference().edit {
                                putBoolean(AppSharedData.PREF_IS_LOGIN, false)
                            }
                            _viewModel.navigationCommand.value = NavigationCommand.To(
                                ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment()
                            )
                        }
                }
                return true

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            if (it) {
                navigateToAddReminder()
                Timber.d("Permission granted")
            } else {

                if (showForegroundLocationRequestPermission(mActivity)) {
                    Timber.d("Permission denied:request again")
                    requestPermission()
                    _viewModel.showToast.value =
                        mActivity.getString(R.string.text_enable_background_location_permission_msg)
                } else {
                    Timber.d("Permission denied:can't request again")
                    _viewModel.showToast.value =
                        mActivity.getString(R.string.text_msg_cant_access_background_location_services)
                }

            }
        }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}