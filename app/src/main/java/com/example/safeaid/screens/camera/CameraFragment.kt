package com.example.safeaid.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentCameraBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.onLoading
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.camera.viewmodel.PredictState
import com.example.safeaid.screens.camera.viewmodel.PredictViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>() {
    private val viewModel: PredictViewModel by activityViewModels()

    private var imageCapture: ImageCapture? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(
            requireContext(),
            "Cần quyền camera để sử dụng tính năng này",
            Toast.LENGTH_SHORT
        ).show()
    }

    // launcher chọn ảnh từ album
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.predict(imageUri = it, context = requireContext())
            }
        }

    override fun isHostFragment(): Boolean = true

    override fun onInit() {
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.btnUpload.setOnDebounceClick {
            pickImageLauncher.launch("image/*")
        }

        viewBinding.btnTake.setOnDebounceClick {
            takePhoto()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.cameraPreview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraFragment", "Lỗi khi khởi tạo camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().externalMediaDirs.firstOrNull(),
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        requireContext(),
                        "Chụp ảnh thất bại: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("CameraFragment", "takePhoto error: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    viewModel.predict(
                        imageUri = savedUri,
                        imageFile = photoFile,
                        context = requireContext()
                    )
                }
            })
    }

    private fun updateUi(state: DataResult<PredictState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is PredictState.PredictRes -> {
                    val bundle = Bundle()
                    bundle.putSerializable(
                        ScanResultFragment.argKey, data.data
                    )

                    viewBinding.progressBar.isVisible = false

                    findNavController().navigate(
                        R.id.action_cameraFragment_to_scanResultFragment,
                        bundle
                    )
                }

                else -> {}
            }
        }
        state?.doIfFailure { }
        state?.onLoading {
            viewBinding.progressBar.isVisible = true
        }
    }
}