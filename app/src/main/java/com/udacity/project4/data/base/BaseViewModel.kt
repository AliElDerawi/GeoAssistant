package com.udacity.project4.data.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

/**
 * Base class for View Models to declare the common Channel and StateFlow objects in one place
 */
abstract class BaseViewModel(val app: Application) : AndroidViewModel(app) {
    val navigationCommandChannel = Channel<NavigationCommand>(Channel.BUFFERED)
    val showErrorMessageChannel = Channel<String>(Channel.BUFFERED)
    val showSnackBarChannel = Channel<String>(Channel.BUFFERED)
    val showSnackBarIntChannel = Channel<Int>(Channel.BUFFERED)
    val showToastChannel = Channel<String>(Channel.BUFFERED)
    val showToastIntChannel = Channel<Int>(Channel.BUFFERED)
    val showLoadingMutableStateFlow = MutableStateFlow<Boolean>(false)
    val showNoDataMutableStateFlow = MutableStateFlow<Boolean>(false)


    fun getLocalizedContext(): Context {

        val config = Configuration(app.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        config.setLayoutDirection(Locale.ENGLISH)

        return app.createConfigurationContext(config)
    }
}