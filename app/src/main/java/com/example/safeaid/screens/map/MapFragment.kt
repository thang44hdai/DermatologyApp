package com.example.safeaid.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>() {
    private val viewModel: MapViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        viewBinding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        viewBinding.mapView.setMultiTouchControls(true)
        viewBinding.mapView.controller.setZoom(16.0)
        val initMarker = Marker(viewBinding.mapView).apply {
            position = GeoPoint(20.980983103228652, 105.788156785282)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "PTIT"
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location)
        }
        viewBinding.mapView.controller.setCenter(GeoPoint(20.980983103228652, 105.788156785282))
        viewBinding.mapView.overlays.add(initMarker)
        viewBinding.mapView.invalidate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestLocationPermission()
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
                    Log.i("hihihi", "${data.data}")
                    addMarkers(data.data)
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
                    GeoPoint(pharmacy.latitude, pharmacy.longitude)
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

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
                val geoPoint = GeoPoint(lat, lon)

                val mapController = viewBinding.mapView.controller
                mapController.setCenter(geoPoint)
                mapController.setZoom(16.0)

                // 🔹 Hiển thị marker cho vị trí hiện tại
                showCurrentLocationMarker(lat, lon)

                Log.i("hihihi", "${location}")

                // 🔹 Gọi API tìm nhà thuốc gần đó
                viewModel.searchPharmacyNear(
                    latitude = lat.toString(),
                    longitude = lon.toString(),
                )
            }
        }
    }

    private fun showCurrentLocationMarker(lat: Double, lon: Double) {
        val currentMarker = Marker(viewBinding.mapView).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Vị trí của bạn"

            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location)
        }

        viewBinding.mapView.overlays.add(currentMarker)
        viewBinding.mapView.invalidate()
    }
}