package com.udacity.project4.features.authentication.viewModel

import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.data.base.BaseViewModel
import com.udacity.project4.data.base.NavigationCommand
import com.udacity.project4.features.authentication.view.AuthenticationFragmentDirections
import com.udacity.project4.utils.AppSharedMethods.setLoginStatus
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    private var _completeLoginSingleLiveEvent = SingleLiveEvent<Boolean>()
    val completeLoginSingleLiveEvent: LiveData<Boolean>
        get() = _completeLoginSingleLiveEvent

    fun loginClick() {
        _completeLoginSingleLiveEvent.value = true
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseAuth.getInstance().currentUser?.let { user ->
                Timber.d("onSignInResult:userId: ${user.uid}" + " userToken: ${user.getIdToken(true)}")
                setLoginStatus(true, user.uid)
                viewModelScope.launch {
                    navigationCommand.send(NavigationCommand.To(
                        AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment()
                    ))
                }
            } ?: Timber.d("onSignInResult: User is null")

        } else {
            // Sign in failed.
            response?.error?.let { error ->
                Timber.d("onSignInResult:error $error")
            }
        }
    }
}