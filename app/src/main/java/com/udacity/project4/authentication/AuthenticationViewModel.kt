package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.SingleLiveEvent

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    private var _completeLoginSingleLiveEvent = SingleLiveEvent<Boolean>()
    val completeLoginSingleLiveEvent: LiveData<Boolean>
        get() = _completeLoginSingleLiveEvent

    fun loginClick() {
        _completeLoginSingleLiveEvent.value = true
    }
}