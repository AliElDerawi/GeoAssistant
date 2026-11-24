package com.udacity.project4

import android.location.Location
import com.udacity.project4.data.dto.Result
import com.udacity.project4.data.dto.UserDataSource

class FakeUserRepository : UserDataSource {

    override suspend fun getCurrentUserLocation(): Result<Location> = Result.Error(null)

}