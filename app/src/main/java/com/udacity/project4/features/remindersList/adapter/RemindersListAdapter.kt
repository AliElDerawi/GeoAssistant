package com.udacity.project4.features.remindersList.adapter

import androidx.recyclerview.widget.DiffUtil
import com.udacity.project4.R
import com.udacity.project4.data.base.BaseRecyclerViewAdapter
import com.udacity.project4.data.model.ReminderDataItem

// Use data binding to show the reminder on the item
class RemindersListAdapter(diffCallback: DiffUtil.ItemCallback<ReminderDataItem>,callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(diffCallback, callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}