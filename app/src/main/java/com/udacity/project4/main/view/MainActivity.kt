package com.udacity.project4.main.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.ActivityMainBinding
import com.udacity.project4.locationreminders.reminderDescription.ReminderDescriptionFragment
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.isLogin
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class MainActivity : AppCompatActivity() {

    private val mMainViewModel: MainViewModel by inject()
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let {
            if (it.hasExtra(ReminderDescriptionFragment.EXTRA_ReminderDataItem)) {
                val bundle = Bundle().apply {
                    putParcelable(
                        ReminderDescriptionFragment.EXTRA_ReminderDataItem,
                        if (AppSharedMethods.isSupportsAndroid33()) {
                            intent.getParcelableExtra(ReminderDescriptionFragment.EXTRA_ReminderDataItem, ReminderDataItem::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(ReminderDescriptionFragment.EXTRA_ReminderDataItem)
                        }
                    )
                }
                navController.navigate(R.id.reminderDescriptionFragment, bundle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Implement the create account and sign in using FirebaseUI,
        //  use sign in using email and sign in using Google

        // TODO: If the user was authenticated, send him to RemindersActivity

        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout


        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.mainToolbar)
        mBinding.mainToolbar.setTitle(null)
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        Timber.plant(Timber.DebugTree())
        if (savedInstanceState == null) {
            val navGraph = navController.navInflater.inflate(R.navigation.main_navigation).apply {
                setStartDestination(
                    if (isLogin()) {
                        R.id.reminderListFragment
                    } else {
                        R.id.authenticationFragment
                    }
                )
            }
            navController.graph = navGraph
        }
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        with(mMainViewModel) {

            hideToolbar.observe(this@MainActivity) {
                mBinding.mainToolbar.visibility = if (it) View.GONE else View.VISIBLE
            }

            toolbarTitle.observe(this@MainActivity) {
                mBinding.textViewToolbarTitle.text = it
            }

            showUpButton.observe(this@MainActivity) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(it)
            }

            navigationCommand.observe(this@MainActivity) { command ->
                when (command) {
                    is NavigationCommand.To -> navController.navigate(command.directions)
                    is NavigationCommand.Back -> navController.popBackStack()
                    is NavigationCommand.BackTo -> navController.popBackStack(
                        command.destinationId,
                        false
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult:called")
        mMainViewModel.passOnActivityResult(requestCode, resultCode, data)
    }

}