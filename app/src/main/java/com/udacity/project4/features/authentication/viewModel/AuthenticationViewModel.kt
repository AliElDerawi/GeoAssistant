package com.udacity.project4.features.authentication.viewModel

import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.data.base.BaseViewModel
import com.udacity.project4.data.base.NavigationCommand
import com.udacity.project4.features.authentication.view.AuthenticationFragmentDirections
import com.udacity.project4.utils.AppSharedMethods.setLoginStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    private var _completeLoginChannel = Channel<Boolean>(Channel.BUFFERED)
    val completeLoginChannel: Channel<Boolean>
        get() = _completeLoginChannel

    fun loginClick() {
        viewModelScope.launch {
            _completeLoginChannel.send(true)
        }
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseAuth.getInstance().currentUser?.let { user ->
                Timber.d("onSignInResult:userId: ${user.uid}" + " userToken: ${user.getIdToken(true)}")
                setLoginStatus(true, user.uid)
                viewModelScope.launch {
                    navigationCommandChannel.send(NavigationCommand.To(
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