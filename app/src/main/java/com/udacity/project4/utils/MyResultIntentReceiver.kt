package com.udacity.project4.utils

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class MyResultIntentReceiver(handler: Handler) : ResultReceiver(handler) {

    private var mReceiver: Receiver? = null

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle?)
    }

    fun setReceiver(receiver: Receiver?) {
        mReceiver = receiver
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        mReceiver?.onReceiveResult(resultCode, resultData)
    }
}