package com.example.safeaid.core.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

abstract class BaseContainerFragment<VB : ViewDataBinding>(
    private val journeyId: Int
) : BaseFragment<VB>() {
    private val TAG: String = "BaseFragment"
    protected val navHost: NavHostFragment?
        get() = ((childFragmentManager.findFragmentById(
            journeyId
        )) as? NavHostFragment)
    protected val nav: NavController? get() = navHost?.findNavController()

    /**
     * Called when a fragment is first attached to its context.
     *
     * @param context The application context.
     *
     * @see Fragment.onAttach
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onStart() {
        startListenOnNavigation()
        super.onStart()
    }

    override fun onStop() {
        stopListenOnNavigation()
        super.onStop()
    }
    override fun isHostFragment(): Boolean {
        return true
    }
    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param view The view returned by onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @see Fragment.onViewCreated
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                for (event in navigation) {
                    onNavigationEvent(event)
                }
            }
        }
    }

    open fun onNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NextScreen -> onNextScreen(event.action, event.extras)
            is PopScreen -> popScreen(event.action, event.extras)
        }
    }

    protected open fun popScreen(action: Int, extras: Bundle? = null): Boolean {
        nav?.let { n ->
            n.previousBackStackEntry?.let {
                n.currentBackStackEntry?.let {
                    if (n.popBackStack()) return true
                    n.previousBackStackEntry
                    n.navigate(it.destination.id)//reload here
                }
            }
        }
        return true
    }

    protected open fun onNextScreen(action: Int, extras: Bundle?): Boolean {
        try {
            nav?.navigate(action, extras)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "onNextScreen:   error", e)
        }
        return false
    }

    private var listenerOnNavigation: NavController.OnDestinationChangedListener? = null
    private fun startListenOnNavigation() {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            onNavigationChanged(destination)
        }
        nav?.addOnDestinationChangedListener(listener)
        listenerOnNavigation = listener
    }

    abstract fun onNavigationChanged(destination: NavDestination)

    private fun stopListenOnNavigation() {
        listenerOnNavigation?.let {
            nav?.removeOnDestinationChangedListener(it)
        }
        listenerOnNavigation = null
    }

    open class NavigationEvent {
        open val action: Int = 0
        open val extras: Bundle? = null
    }

    data class NextScreen(override val action: Int, override val extras: Bundle? = null) :
        NavigationEvent()

    data class PopScreen(override val extras: Bundle? = null) : NavigationEvent()
}