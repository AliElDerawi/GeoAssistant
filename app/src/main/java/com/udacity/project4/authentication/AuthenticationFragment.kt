package com.udacity.project4.authentication

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentAuthenticationBinding
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.AppSharedMethods.setLoginStatus
import com.udacity.project4.utils.Constants
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AuthenticationFragment : BaseFragment() {

    override val mViewModel: AuthenticationViewModel by viewModel()
    private lateinit var mBinding: FragmentAuthenticationBinding
    private val mSharedViewModel: MainViewModel by inject()
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
        mViewModel.completeLoginSingleLiveEvent.observe(viewLifecycleOwner) { redirect ->
            if (redirect) {
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setLogo(R.drawable.ic_location_logo)
                    .setTheme(R.style.Theme_FirebaseAuthUI_EdgeToEdge) // Use the custom theme
                    .setAvailableProviders(Constants.FIREBASE_LOGIN_PROVIDER)
                    .build()
                signInLauncher.launch(signInIntent)
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseAuth.getInstance().currentUser?.let { user ->
                Timber.d("onSignInResult:userId: ${user.uid}" + " userToken: ${user.getIdToken(true)}")
                setLoginStatus(true, user.uid)
                mViewModel.navigationCommand.value = NavigationCommand.To(
                    AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment()
                )
            } ?: Timber.d("onSignInResult: User is null")

        } else {
            // Sign in failed.
            response?.error?.let { error ->
                Timber.d("onSignInResult:error $error")
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

}