package com.example.safeaid.core.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.safeaid.core.base.AppResources
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Serializable
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewDataBinding> : Fragment() {
    private val TAG: String = "BaseFragment"
    protected lateinit var viewBinding: VB private set
    private val type = (javaClass.genericSuperclass as ParameterizedType)
    private val classVB = type.actualTypeArguments[0] as Class<VB>
    protected val _navigation = Channel<BaseContainerFragment.NavigationEvent>(Channel.UNLIMITED)

    var isViewBindingInitialized = false

    companion object {
        const val TAG_LOADING_DIALOG = "TAG_LOADING_DIALOG"
        const val TAG_CONFIRM_DIALOG = "TAG_CONFIRM_DIALOG"
    }

    protected val navigation: ReceiveChannel<BaseContainerFragment.NavigationEvent> get() = _navigation
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackLogic()
        }
    }

    fun offerNavEvent(event: BaseContainerFragment.NavigationEvent) {
        _navigation.trySend(event)
    }

    suspend fun sendNavEvent(event: BaseContainerFragment.NavigationEvent) {
        _navigation.send(event)
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be
     * attached to. The fragment should not add the view itself, but this can be used to generate
     * the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     * saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     *
     * @see Fragment.onCreateView
     */
    private val inflateMethod = classVB.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = inflateMethod.invoke(null, inflater, container, false) as VB
        isViewBindingInitialized= this::viewBinding.isInitialized
        return viewBinding.root
    }

    fun showMessage(request: ConfirmRequest) {
        AlertDialogFragment(request).show(parentFragmentManager, TAG_CONFIRM_DIALOG)
    }

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
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        onInit()
        onInitObserver()
        onInitListener()
    }

    open fun handleBackLogic() {
        if (isHostFragment()) {
            activity?.finish()
        } else {
            findNavController().navigateUp()
        }
    }

    abstract fun isHostFragment(): Boolean
    abstract fun onInit()
    abstract fun onInitObserver()
    abstract fun onInitListener()


    /**
     * Return the [AppCompatActivity] this fragment is currently associated with.
     *
     * @throws IllegalStateException if not currently associated with an activity or if associated
     * only with a context.
     * @throws TypeCastException if the currently associated activity don't extend [AppCompatActivity].
     *
     * @see requireActivity
     */
    fun requireCompatActivity(): AppCompatActivity {
        requireActivity()
        val activity = requireActivity()
        if (activity is AppCompatActivity) {
            return activity
        } else {
            throw TypeCastException("Main activity should extend from 'AppCompatActivity'")
        }
    }
}

inline fun <reified T> Fragment.setResultAndFinishByStateHandle(name: String, value: T) {
    findNavController().run {
        previousBackStackEntry?.savedStateHandle?.set(
            name, value
        )
        navigateUp()
    }
}

inline fun <reified T> Fragment.createOneShotLiveData(
    name: String,
    crossinline observer: (T) -> Unit
) {
    findNavController().currentBackStackEntry
        ?.savedStateHandle?.run {
            remove<T>(name)
            getLiveData<T>(name).observe(this@createOneShotLiveData, Observer { model ->
                observer(model)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle?.let {
                        it.getLiveData<T>(name).removeObservers(this@createOneShotLiveData)
                        it.remove<T>(name)
                    }
            })
        }
}
class AlertDialogFragment(private val confirmRequest: ConfirmRequest) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return confirmRequest.buildAlertDialog(requireContext())
    }
}

data class ConfirmRequest(
    val icon: Int? = null,
    val title: String? = null,
    val message: String? = null,
    val positive: String? = null,
    val negative: String? = null,
    val neutral: String? = null,
    val cancelable: Boolean = true,
    val timeOutInMillis: Long? = null,
    @Transient val onPositiveSelected: (() -> Unit)? = null,
    @Transient val onNegativeSelected: (() -> Unit)? = null,
    @Transient val onNeutralSelected: (() -> Unit)? = null,
) : Serializable

fun ConfirmRequest.buildAlertDialog(context: Context, @StyleRes style: Int = 0): AlertDialog =
    AlertDialog.Builder(context, style)
        .apply {
            setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .setIcon(icon?.let { if (it == 0) null else AppResources.getDrawable(it) })
            positive?.let {
                setPositiveButton(it) { dialog, _ ->
                    dialog.dismiss()
                    onPositiveSelected?.invoke()
                }
            }
            negative?.let {
                setNegativeButton(it) { dialog, _ ->
                    dialog.dismiss()
                    onNegativeSelected?.invoke()
                }
            }
            neutral?.let {
                setNeutralButton(it) { dialog, _ ->
                    dialog.dismiss()
                    onNeutralSelected?.invoke()
                }
            }
        }.create()

