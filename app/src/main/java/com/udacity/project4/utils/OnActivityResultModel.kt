package com.udacity.project4.utils

import android.content.Intent

data class OnActivityResultModel(

    val requestCode: Int, val resultCode: Int, val data: Intent?
) {}