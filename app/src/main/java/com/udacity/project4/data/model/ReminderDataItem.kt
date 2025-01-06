package com.udacity.project4.data.model

import android.os.Parcelable
import com.udacity.project4.data.base.GenericModelCallBack
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */

@Parcelize
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString()
) : Parcelable{
    companion object{
        fun getReminderDataDiffCallback(): GenericModelCallBack<ReminderDataItem> {
            return GenericModelCallBack(_areItemsTheSame = { oldItem, newItem ->
                oldItem.id == newItem.id
            }, _areContentsTheSame = { oldItem, newItem -> oldItem == newItem })
        }
    }
}