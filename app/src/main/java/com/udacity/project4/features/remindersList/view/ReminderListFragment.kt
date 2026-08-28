package com.udacity.project4.features.remindersList.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.data.base.BaseFragment
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.features.main.viewModel.MainViewModel
import com.udacity.project4.features.remindersList.adapter.RemindersListAdapter
import com.udacity.project4.features.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.features.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.utils.AppSharedMethods.setLoginStatus
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    override val mViewModel: RemindersListViewModel by viewModel()
    private val mSharedViewModel: MainViewModel by activityViewModel()
    private val mSaveReminderViewModel: SaveReminderViewModel by activityViewModel()
    private lateinit var mBinding: FragmentRemindersBinding
    private lateinit var mActivity: FragmentActivity

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
        mBinding = FragmentRemindersBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mViewModel
        }
        mSharedViewModel.apply {
            setHideToolbar(false)
            setToolbarTitle(mActivity.getString(R.string.app_name))
        }
        mSaveReminderViewModel.onClear()
        mActivity.setDisplayHomeAsUpEnabled(false)
        initViewModelObservers()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenu()
        setupRecyclerView()
        initListener()
    }

    private fun initListener() {
        with(mBinding) {
            refreshLayout.setOnRefreshListener {
                mViewModel.loadReminders()
            }
        }
    }

    private fun initViewModelObservers() {

    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter(ReminderDataItem.getReminderDataDiffCallback()) {
            mViewModel.navigateToReminderDescription(it)
        }
        // Setup the recycler view using the extension function
        mBinding.remindersRecyclerView.setup(adapter)
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
                            setLoginStatus(false)
                            mViewModel.navigateToLoginScreen()
                        }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}