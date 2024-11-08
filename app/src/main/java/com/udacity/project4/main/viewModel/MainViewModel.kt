package com.udacity.project4.main.viewModel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.base.BaseViewModel

class MainViewModel(app: Application) : BaseViewModel(app) {

    private var _hideToolbar = MutableLiveData<Boolean>()
    val hideToolbar: LiveData<Boolean>
        get() = _hideToolbar

    private var _showUpButton = MutableLiveData<Boolean>()
    val showUpButton: LiveData<Boolean>
        get() = _showUpButton

    fun setHideToolbar(hideToolbar: Boolean) {
        _hideToolbar.value = hideToolbar
    }

}