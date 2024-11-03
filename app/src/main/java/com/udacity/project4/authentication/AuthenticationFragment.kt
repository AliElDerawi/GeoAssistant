package com.udacity.project4.authentication

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentAuthenticationBinding
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.AppSharedData
import com.udacity.project4.utils.AppSharedMethods.getSharedPreference
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AuthenticationFragment : BaseFragment() {

    private lateinit var mBinding: FragmentAuthenticationBinding

    private val mSharedViewModel: MainViewModel by inject()

    private lateinit var mActivity: Activity

    private lateinit var mLifecycleOwner: LifecycleOwner

    override val _viewModel: AuthenticationViewModel by viewModel()



    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_authentication, container, false
        )
        mSharedViewModel.setHideToolbar(true)
        mBinding.authenticationViewModel = _viewModel
        mLifecycleOwner = this
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObserver()
//        Timber.d("onViewCreated: ")
    }

    private fun initViewModelObserver() {

        _viewModel.completeLoginLiveData.observe(mLifecycleOwner) { redirect ->


            if (redirect) {

                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setLogo(R.drawable.ic_location_logo)
                    .setAvailableProviders(providers)
                    .build()
                signInLauncher.launch(signInIntent)

            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // ...
            Timber.d("onSignInResult: " + user!!.email)
            _viewModel.setCompleteLogin(false)

            getSharedPreference().edit {
                putBoolean(AppSharedData.PREF_IS_LOGIN, true)
            }


            val directions =
                AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)


        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            if (response != null)
                Timber.d("onSignInResult:error " + response.error.toString())

        }
    }
}