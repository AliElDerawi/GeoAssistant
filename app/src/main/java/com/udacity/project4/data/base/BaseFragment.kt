package com.udacity.project4.data.base

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.utils.AppSharedMethods.showSnackBar
import com.udacity.project4.utils.AppSharedMethods.showToast

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

    override fun onStart() {
        super.onStart()
        with(mViewModel){
            showErrorMessage.observe(viewLifecycleOwner) {
                showToast(it, Toast.LENGTH_LONG)
            }
            showToast.observe(viewLifecycleOwner) {
                showToast(it, Toast.LENGTH_LONG)
            }
            showToastInt.observe(viewLifecycleOwner) {
                mActivity.showToast(it, Toast.LENGTH_LONG)
            }
            showSnackBar.observe(viewLifecycleOwner) {
                mActivity.showSnackBar(it, Snackbar.LENGTH_LONG)
            }
            showSnackBarInt.observe(viewLifecycleOwner) {
                mActivity.showSnackBar(it, Snackbar.LENGTH_LONG)
            }
            navigationCommand.observe(viewLifecycleOwner) { command ->
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