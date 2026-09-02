package com.udacity.project4.data.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.utils.AppSharedMethods.showSnackBar
import com.udacity.project4.utils.AppSharedMethods.showToast
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {

    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val mViewModel: BaseViewModel
    private lateinit var mActivity: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                with(mViewModel) {


                    launch {
                        showErrorMessageChannel.receiveAsFlow().collect { message ->
                            showToast(message, Toast.LENGTH_LONG)
                        }
                    }

                    launch {
                        showToastChannel.receiveAsFlow().collect { message ->
                            showToast(message, Toast.LENGTH_LONG)
                        }
                    }

                    launch {
                        showToastIntChannel.receiveAsFlow().collect { message ->
                            mActivity.showToast(message, Toast.LENGTH_LONG)
                        }
                    }
                    launch {
                        showSnackBarChannel.receiveAsFlow().collect { message ->
                            mActivity.showSnackBar(message, Snackbar.LENGTH_LONG)
                        }
                    }

                    launch {
                        showSnackBarIntChannel.receiveAsFlow().collect { message ->
                            mActivity.showSnackBar(message, Snackbar.LENGTH_LONG)
                        }
                    }

                    launch {
                        navigationCommandChannel.receiveAsFlow().collect { command ->
                            when (command) {
                                is NavigationCommand.To -> findNavController().navigate(command.directions)
                                is NavigationCommand.Back -> findNavController().popBackStack()
                                is NavigationCommand.BackTo -> findNavController().popBackStack(
                                    command.destinationId,
                                    false
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}
