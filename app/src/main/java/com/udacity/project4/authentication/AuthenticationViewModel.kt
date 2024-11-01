package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.SingleLiveEvent

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    private var _completeLoginLiveData = SingleLiveEvent<Boolean>()
    val completeLoginLiveData: SingleLiveEvent<Boolean>
        get() = _completeLoginLiveData


    fun setCompleteLogin(completeLogin: Boolean) {
        _completeLoginLiveData.value = completeLogin
    }

    fun login() {

        _completeLoginLiveData.value = true

    }
}