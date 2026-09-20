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
abstract class BaseViewModel(
    val app: Application,
) : AndroidViewModel(app) {
    val navigationCommandChannel: Channel<NavigationCommand> = Channel(Channel.BUFFERED)
    val showErrorMessageChannel: Channel<String> = Channel(Channel.BUFFERED)
    val showSnackBarChannel: Channel<String> = Channel(Channel.BUFFERED)
    val showSnackBarIntChannel: Channel<Int> = Channel(Channel.BUFFERED)
    val showToastChannel: Channel<String> = Channel(Channel.BUFFERED)
    val showToastIntChannel: Channel<Int> = Channel(Channel.BUFFERED)
    val _showLoadingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val _showNoDataStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun getLocalizedContext(): Context {
        val config = Configuration(app.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        config.setLayoutDirection(Locale.ENGLISH)

        return app.createConfigurationContext(config)
    }
}
