package com.udacity.project4.features.main.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.udacity.project4.R
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.databinding.ActivityMainBinding
import com.udacity.project4.features.main.viewModel.MainViewModel
import com.udacity.project4.locationreminders.reminderDescription.ReminderDescriptionFragment
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.AppSharedMethods.applyWindowsPadding
import com.udacity.project4.utils.AppSharedMethods.getCompatColor
import com.udacity.project4.utils.AppSharedMethods.setStatusBarColorAndStyle
import com.udacity.project4.utils.validateStartDestination
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class MainActivity : AppCompatActivity() {

    private val mMainViewModel: MainViewModel by inject()
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private lateinit var mAppBarConfiguration: AppBarConfiguration

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(ReminderDescriptionFragment.EXTRA_ReminderDataItem)) {
            val reminderDataItem = if (AppSharedMethods.isSupportsAndroid33()) {
                intent.getParcelableExtra(
                    ReminderDescriptionFragment.EXTRA_ReminderDataItem,
                    ReminderDataItem::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(ReminderDescriptionFragment.EXTRA_ReminderDataItem)
            }
            val bundle = Bundle().apply {
                putParcelable(ReminderDescriptionFragment.EXTRA_ReminderDataItem, reminderDataItem)
            }
            mNavController.navigate(R.id.reminderDescriptionFragment, bundle)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO - Completed: Implement the create account and sign in using FirebaseUI,
        //  use sign in using email and sign in using Google
        // TODO - Completed: If the user was authenticated, send him to RemindersActivity
        // TODO - Completed: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        enableEdgeToEdge()
        mBinding =
            DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
                .apply {
                    viewModel = mMainViewModel
                    lifecycleOwner = this@MainActivity
                    setSupportActionBar(mainToolbar)
                    supportActionBar?.title = null
                    root.applyWindowsPadding()
                    setStatusBarColorAndStyle(getCompatColor(R.color.colorPrimary))
                }
        initListener(savedInstanceState)
        initViewModelObservers()
    }

    private fun initListener(savedInstanceState: Bundle?) {
        mNavController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        mAppBarConfiguration = AppBarConfiguration(mNavController.graph)
        Timber.plant(Timber.DebugTree())
        if (savedInstanceState == null) {
            mNavController.validateStartDestination()
        }
    }

    private fun initViewModelObservers() {
        with(mMainViewModel) {
            showUpButtonLiveData.observe(this@MainActivity) {
                supportActionBar?.setDisplayHomeAsUpEnabled(it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
    }

}