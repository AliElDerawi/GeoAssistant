package com.udacity.project4.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityMainBinding
import com.udacity.project4.utils.AppSharedData
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.Constants
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
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        Timber.plant(Timber.DebugTree())
        initViewModelObservers()
        initListeners()


    }

    private fun initViewModelObservers() {
        mMainViewModel.hideToolbar.observe(this) {
            if (it) {
                mBinding.mainToolbar.visibility = View.GONE
            } else {
                mBinding.mainToolbar.visibility = View.VISIBLE
            }
        }

        mMainViewModel.toolbarTitle.observe(this) {
            mBinding.textViewToolbarTitle.text = it
        }

        mMainViewModel.showUpButton.observe(this) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(it)
        }
    }

    private fun initListeners() {

        if (AppSharedMethods.getSharedPreference().getBoolean(AppSharedData.PREF_IS_LOGIN, false)) {
            navController.navigate(R.id.reminderListFragment)
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