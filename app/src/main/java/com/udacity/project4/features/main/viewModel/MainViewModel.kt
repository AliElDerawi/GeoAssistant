package com.udacity.project4.features.main.viewModel

import android.app.Application
import com.udacity.project4.data.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(private val mApp: Application) : BaseViewModel(mApp) {

    private var _hideToolbarStateFlow = MutableStateFlow<Boolean>(false)
    val hideToolbarStateFlow: StateFlow<Boolean>
        get() = _hideToolbarStateFlow
    private var _toolbarTitleStateFlow = MutableStateFlow<String>("")
    val toolbarTitleStateFlow: StateFlow<String>
        get() = _toolbarTitleStateFlow

    fun setHideToolbar(hideToolbar: Boolean) {
        _hideToolbarStateFlow.value = hideToolbar
    }

    fun setToolbarTitle(title: String) {
        _toolbarTitleStateFlow.value = title
    }

}