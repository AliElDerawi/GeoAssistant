package com.udacity.project4.locationreminders.reminderDescription

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentReminderDescriptionBinding
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionFragment : BaseFragment() {

    override val _viewModel: MainViewModel by inject()
    private lateinit var mBinding: FragmentReminderDescriptionBinding
    private lateinit var mActivity: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    companion object {
        const val EXTRA_ReminderDataItem = "reminderDataItem"
        // Receive the reminder object after the user clicks on the notification
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Add the implementation of the reminder details
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentReminderDescriptionBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            reminderDataItem = arguments?.let {
                ReminderDescriptionFragmentArgs.fromBundle(it).reminderDataItem
            }
        }
        setTitle(mActivity.getString(R.string.text_geofence_detail))
        setDisplayHomeAsUpEnabled(true)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}