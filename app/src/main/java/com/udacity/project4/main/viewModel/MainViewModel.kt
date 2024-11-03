package com.udacity.project4.main.viewModel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.OnActivityResultModel

class MainViewModel(app: Application) : BaseViewModel(app) {

    private var _hideToolbar = MutableLiveData<Boolean>()
    val hideToolbar: LiveData<Boolean>
        get() = _hideToolbar

    private var _toolbarTitle = MutableLiveData<String>()
    val toolbarTitle: LiveData<String>
        get() = _toolbarTitle

    private var _showUpButton = MutableLiveData<Boolean>()
    val showUpButton: LiveData<Boolean>
        get() = _showUpButton

    private var _passOnActivityResult = MutableLiveData<OnActivityResultModel?>()
    val passOnActivityResult: LiveData<OnActivityResultModel?>
        get() = _passOnActivityResult

    fun setHideToolbar(hideToolbar: Boolean) {
        _hideToolbar.value = hideToolbar
    }

    fun setToolbarTitle(title: String) {
        _toolbarTitle.value = title
    }

    fun showUpButton(show: Boolean) {
        _showUpButton.value = show
    }

    fun passOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        _passOnActivityResult.value = OnActivityResultModel(requestCode, resultCode, data)
    }

    fun completePassOnActivityResult() {
        _passOnActivityResult.value = null
    }

}