package com.example.safeaid.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.fragment.app.activityViewModels
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
import com.example.safeaid.screens.map.bottom_sheet.PharmacyBottomSheet
import com.example.safeaid.screens.pharmacy.PharmacyDetailFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>() {
    private val viewModel: MapViewModel by activityViewModels()
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
        viewBinding.mapView.controller.setZoom(viewModel.zoomMap)
        val initMarker = Marker(viewBinding.mapView).apply {
            position = GeoPoint(20.980983103228652, 105.788156785282)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Địa chỉ hiện tại"
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_current_location)
        }
        viewBinding.mapView.controller.setCenter(GeoPoint(20.980983103228652, 105.788156785282))
        viewBinding.mapView.overlays.add(initMarker)
        viewBinding.mapView.invalidate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestLocationPermission()
        viewModel.searchPharmacyNear("20.980983103228652", "105.788156785282")
        if (viewModel.targetLocation != GeoPoint(0, 0)) {
            viewBinding.mapView.controller.animateTo(viewModel.targetLocation)
        }
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                updateUi(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.mapState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                viewBinding.mapView.overlays.removeAll { it is Polyline }

                val polyline = Polyline().apply {
                    setPoints(data)
                    outlinePaint.color = Color.BLUE
                    outlinePaint.strokeWidth = 8f
                }

                viewBinding.mapView.overlays.add(polyline)
                viewBinding.mapView.invalidate()

                val dest = data.lastOrNull()
                if (dest != null) {
                    viewBinding.mapView.controller.animateTo(dest)
                    viewBinding.mapView.controller.setZoom(viewModel.zoomMap)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewBinding.mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                event?.let {
                    val currentZoom = it.zoomLevel
                    viewModel.zoomMap = currentZoom
                }
                return true
            }
        })
    }

    override fun onInitListener() {
    }

    private fun updateUi(state: DataResult<MapState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is MapState.PharmaciesNearBy -> {
                    addMarkers(data.data)
                }

                else -> {}
            }
        }
        state?.doIfFailure { }
    }

    private fun addMarkers(pharmacies: List<PharmacyResponse>) {
        pharmacies.forEachIndexed { idx, pharmacy ->
            if (pharmacy.latitude != null && pharmacy.longitude != null) {
                val point = GeoPoint(pharmacy.latitude, pharmacy.longitude)
                val marker = Marker(viewBinding.mapView).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = pharmacy.name
                    subDescription = "${pharmacy.address}\nGiờ mở cửa: ${pharmacy.openTime}"
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_map)

                    setOnMarkerClickListener { clickedMarker, mapView ->
                        mapView.controller.animateTo(clickedMarker.position)
                        viewModel.targetLocation = clickedMarker.position

                        val bottomSheet = PharmacyBottomSheet.newInstance(pharmacy)
                        bottomSheet.setOnClick(object :
                            PharmacyBottomSheet.OnClickPharmacyBottomSheet {
                            override fun onClickViewDetail(data: PharmacyResponse) {
                                val bundle = Bundle()
                                bundle.putSerializable(PharmacyDetailFragment.ARG, data)
                                bundle.putBoolean(PharmacyDetailFragment.IS_DIRECTION, false)
                                findNavController().navigate(
                                    R.id.action_mainScreen_to_pharmacyDetailFragment,
                                    bundle
                                )
                            }

                            override fun onClickDirection(data: PharmacyResponse) {
                                val current = viewModel.currentLocation
                                val dest = GeoPoint(data.latitude, data.longitude)
                                viewModel.getRoute(current, dest)
                            }
                        })
                        bottomSheet.show(parentFragmentManager, "PharmacyBottomSheet")
                        true
                    }
                }

                viewBinding.mapView.overlays.add(marker)

                if (idx == 0 && viewModel.isPredicted) {
                    val line = Polyline().apply {
                        setPoints(listOf(viewModel.currentLocation, point))
                        title = "Đường đi tới ${pharmacy.name}"
                        outlinePaint.color = Color.RED
                        outlinePaint.strokeWidth = 8f
                    }
                    viewBinding.mapView.overlays.add(line)
                    viewModel.isPredicted = false
                }
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