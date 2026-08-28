package com.udacity.project4.data.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(val app: Application) : AndroidViewModel(app) {
    val navigationCommand: Channel<NavigationCommand> = Channel(Channel.BUFFERED)
    val showErrorMessage: Channel<String> = Channel()
    val showSnackBar: Channel<String> = Channel()
    val showSnackBarInt: Channel<Int> = Channel()
    val showToast: Channel<String> = Channel()
    val showToastInt: Channel<Int> = Channel()
    val showLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showNoData: MutableStateFlow<Boolean> = MutableStateFlow(false)


    fun getLocalizedContext(): Context {

        val config = Configuration(app.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        config.setLayoutDirection(Locale.ENGLISH)

        return app.createConfigurationContext(config)
    }
}