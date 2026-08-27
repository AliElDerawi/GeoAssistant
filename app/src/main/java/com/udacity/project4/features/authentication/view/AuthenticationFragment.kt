package com.udacity.project4.features.authentication.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.udacity.project4.R
import com.udacity.project4.data.base.BaseFragment
import com.udacity.project4.databinding.FragmentAuthenticationBinding
import com.udacity.project4.features.authentication.viewModel.AuthenticationViewModel
import com.udacity.project4.features.main.viewModel.MainViewModel
import com.udacity.project4.utils.Constants
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationFragment : BaseFragment() {

    override val mViewModel: AuthenticationViewModel by viewModel()
    private lateinit var mBinding: FragmentAuthenticationBinding
    private val mSharedViewModel: MainViewModel by activityViewModel()
    private lateinit var mActivity: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentAuthenticationBinding.inflate(inflater, container, false).apply {
            viewModel = mViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        mSharedViewModel.setHideToolbar(true)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObserver()
    }

    private fun initViewModelObserver() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.completeLoginChannel.receiveAsFlow().collect { redirect ->
                    if (redirect) {

                        val enableCredentials = GoogleApiAvailability.getInstance()
                            .isGooglePlayServicesAvailable(mActivity) == ConnectionResult.SUCCESS

                        val signInIntent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setLogo(R.drawable.ic_app_logo)
                            .setCredentialManagerEnabled(enableCredentials)
                            .setTheme(R.style.Theme_FirebaseAuthUI_EdgeToEdge) // Use the custom theme
                            .setAvailableProviders(Constants.FIREBASE_LOGIN_PROVIDER)
                            .build()

                        signInLauncher.launch(signInIntent)
                    }
                }
            }
        }
    }


    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        mViewModel.onSignInResult(res)
    }

}