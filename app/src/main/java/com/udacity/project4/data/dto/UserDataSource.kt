package com.udacity.project4.data.dto

import android.location.Location

interface UserDataSource {
    suspend fun getCurrentUserLocation(): Result<Location>
}