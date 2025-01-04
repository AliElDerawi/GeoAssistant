package com.udacity.project4.utils

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.base.BaseRecyclerViewAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object BindingAdapters {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T : Any> RecyclerView.setRecyclerViewData(items: List<T>?) {
        items?.let { list ->
            if (adapter == null) {
                this.adapter = adapter as? BaseRecyclerViewAdapter<T>
            }
            (adapter as? BaseRecyclerViewAdapter<T>)?.submitList(list)
            setHasFixedSize(true)
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun View.setFadeVisible( visible: Boolean? = true) {
        tag?.let {
            animate().cancel()
            if (visible == true && visibility == View.GONE) {
                fadeIn()
            } else if (visible != true && visibility == View.VISIBLE) {
                fadeOut()
            }
        } ?: run {
            tag = true
            visibility = if (visible == true) View.VISIBLE else View.GONE
        }
    }

    @BindingAdapter("locationTitle")
    @JvmStatic
    fun TextView.bindLocationTitle(title: String?) {
        text = title
        contentDescription = title
    }

    @BindingAdapter("locationDescription")
    @JvmStatic
    fun TextView.bindDescriptionTitle(description: String?) {
        text = description
        contentDescription = description
    }

    @BindingAdapter("location")
    @JvmStatic
    fun TextView.bindLocation(location: String?) {
        text = location
        contentDescription = location
    }

}