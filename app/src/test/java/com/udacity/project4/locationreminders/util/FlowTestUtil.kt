package com.udacity.project4.locationreminders.util

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
suspend fun <T> Flow<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterCollect: () -> Unit = {}
): T {
    return withTimeout(timeUnit.toMillis(time)) {
        afterCollect()  // Perform any action before collecting the first value
        this@getOrAwaitValue.first()  // Collect the first value from the Flow
    }
}