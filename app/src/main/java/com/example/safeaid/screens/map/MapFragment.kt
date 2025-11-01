package com.example.safeaid.screens.map

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMapBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.screens.camera.ScanResultFragment
import com.example.safeaid.screens.camera.viewmodel.PredictState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>() {
    private val viewModel: MapViewModel by viewModels()
    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        viewBinding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        viewBinding.mapView.setMultiTouchControls(true)
        viewBinding.mapView.controller.setZoom(16.0)

        val startPoint = GeoPoint(20.980877271409287, 105.78740658872302)
        viewBinding.mapView.controller.setCenter(startPoint)

        // Thêm marker
        val marker = Marker(viewBinding.mapView)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Hà Nội - Việt Nam"

        val endPoint = GeoPoint(20.982859, 105.782881)
        viewBinding.mapView.controller.setCenter(startPoint)

        // Thêm marker
        val marker2 = Marker(viewBinding.mapView)
        marker2.position = endPoint
        marker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker2.title = "Thanh Hoa - Việt Nam"
        viewBinding.mapView.overlays.add(marker)
        viewBinding.mapView.overlays.add(marker2)
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
    }

    private fun updateUi(state: DataResult<MapState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is MapState.PharmaciesNearBy -> {
//                    addMarkers(data.data)
                }

                else -> {}
            }
        }
        state?.doIfFailure { }
    }

    private fun addMarkers(pharmacies: List<PharmacyResponse>) {
        pharmacies.forEach { pharmacy ->
            if (pharmacy.latitude != null && pharmacy.longitude != null) {
                val point =
                    GeoPoint(pharmacy.latitude!!.toDouble(), pharmacy.longitude!!.toDouble())
                val marker = Marker(viewBinding.mapView)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = pharmacy.name
                marker.subDescription = "${pharmacy.address}\nGiờ mở cửa: ${pharmacy.openHours}"
                viewBinding.mapView.overlays.add(marker)
            }
        }
        viewBinding.mapView.invalidate()
    }

}