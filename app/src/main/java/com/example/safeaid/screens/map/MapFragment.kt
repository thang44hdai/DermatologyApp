package com.example.safeaid.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
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
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.map.bottom_sheet.PharmacyBottomSheet
import com.example.safeaid.screens.map.viewmodel.MapState
import com.example.safeaid.screens.map.viewmodel.MapViewModel
import com.example.safeaid.screens.pharmacy.PharmacyDetailFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

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
        // Setup map
        viewBinding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        viewBinding.mapView.setMultiTouchControls(true)
        showCurrentLocationMarker(
            viewModel.currentLocation.latitude,
            viewModel.currentLocation.longitude
        )
        viewBinding.mapView.controller.setZoom(viewModel.zoomMap)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Request location permission and get current location
        requestLocationPermission()
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

        viewModel.filterState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                viewBinding.tvFilter.text = data
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewBinding.mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                event?.let {
                    val currentZoom = it.zoomLevel
                    Log.i("MapLocation", "$currentZoom")
                    viewModel.zoomMap = currentZoom
                }
                return true
            }
        })
    }

    override fun onInitListener() {
        viewBinding.layoutSearch.setOnClickListener {
            navigateToFilterScreen()
        }
        viewBinding.btnDelete.setOnDebounceClick {
            viewBinding.tvFilter.text = ""
            viewModel.filter("")
        }
    }

    private fun showPharmacyBottomSheet(pharmacy: PharmacyResponse) {
        val bottomSheet = PharmacyBottomSheet.newInstance(pharmacy)
        bottomSheet.setOnClick(object : PharmacyBottomSheet.OnClickPharmacyBottomSheet {
            override fun onClickViewDetail(data: PharmacyResponse) {
                val bundle = Bundle().apply {
                    putSerializable(PharmacyDetailFragment.ARG, data)
                    putBoolean(PharmacyDetailFragment.IS_DIRECTION, false)
                }
                findNavController().navigate(
                    R.id.action_mainScreen_to_pharmacyDetailFragment,
                    bundle
                )
            }

            override fun onClickDirection(data: PharmacyResponse) {
                if (data.latitude != null && data.longitude != null) {
                    val current = viewModel.currentLocation
                    val dest = GeoPoint(data.latitude, data.longitude)
                    viewModel.getRoute(current, dest)
                }
            }
        })
        bottomSheet.show(parentFragmentManager, "PharmacyBottomSheet")
    }

    private fun updateUi(state: DataResult<MapState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is MapState.PharmaciesNearBy -> {
                    addMarkers(data.data)
                    if (viewModel.directionToLocation != null) {
                        val current = viewModel.currentLocation
                        val data = viewModel.directionToLocation
                        val dest = data?.let { GeoPoint(it.latitude, data.longitude) }
                        dest?.let { viewModel.getRoute(current, it) }
                    }
                }
            }
        }
        state?.doIfFailure { error ->
            android.widget.Toast.makeText(
                requireContext(),
                "${error.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addMarkers(pharmacies: List<PharmacyResponse>) {
        // Remove old pharmacy markers (keep current location marker)
        viewBinding.mapView.overlays.removeAll { overlay ->
            overlay is Marker && overlay.title != "Vị trí của bạn"
        }

        pharmacies.forEach { pharmacy ->
            if (pharmacy.latitude != null && pharmacy.longitude != null) {
                val point = GeoPoint(pharmacy.latitude, pharmacy.longitude)
                val marker = Marker(viewBinding.mapView).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = pharmacy.name
                    snippet = "${pharmacy.address}"
                    subDescription = "Giờ mở cửa: ${pharmacy.openTime ?: "Không rõ"}"

                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_plus_hospital)
                }

                marker.setOnMarkerClickListener { _, mapView ->
                    mapView.controller.animateTo(marker.position)
                    viewModel.targetLocation = marker.position
                    showPharmacyBottomSheet(pharmacy)
                    marker.showInfoWindow()

                    true
                }

                viewBinding.mapView.overlays.add(marker)
            }
        }

        viewBinding.mapView.invalidate()
    }

    private fun requestLocationPermission() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
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
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                        grantResults.getOrNull(1) == PackageManager.PERMISSION_GRANTED)
            ) {
                getCurrentLocation()
            } else {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Cần cấp quyền truy cập vị trí để sử dụng chức năng này",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Log.d("MapLocation", "🔍 [1/4] START - Trying lastLocation...")
        val startTime = System.currentTimeMillis()

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val elapsed = System.currentTimeMillis() - startTime
            if (location != null) {
                Log.d(
                    "MapLocation",
                    "✅ [1/4] SUCCESS - Got lastLocation in ${elapsed}ms: (${location.latitude}, ${location.longitude})"
                )
                handleLocationResult(location.latitude, location.longitude)
            } else {
                Log.d("MapLocation", "⚠️ [1/4] NULL - lastLocation is null after ${elapsed}ms")
                requestCurrentLocation()
            }
        }.addOnFailureListener { e ->
            val elapsed = System.currentTimeMillis() - startTime
            Log.e(
                "MapLocation",
                "❌ [1/4] FAIL - lastLocation failed after ${elapsed}ms: ${e.message}"
            )
            requestCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        Log.d("MapLocation", "🔍 [2/4] START - Trying BALANCED_POWER_ACCURACY (WiFi/Network)...")
        val startTime = System.currentTimeMillis()
        val cancellationTokenSource = com.google.android.gms.tasks.CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            val elapsed = System.currentTimeMillis() - startTime
            if (location != null) {
                Log.d(
                    "MapLocation",
                    "✅ [2/4] SUCCESS - Got BALANCED location in ${elapsed}ms: (${location.latitude}, ${location.longitude})"
                )
                handleLocationResult(location.latitude, location.longitude)
            } else {
                Log.d("MapLocation", "⚠️ [2/4] NULL - BALANCED location is null after ${elapsed}ms")
                tryHighAccuracyLocation()
            }
        }.addOnFailureListener { e ->
            val elapsed = System.currentTimeMillis() - startTime
            Log.e(
                "MapLocation",
                "❌ [2/4] FAIL - BALANCED location failed after ${elapsed}ms: ${e.message}"
            )
            tryHighAccuracyLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun tryHighAccuracyLocation() {
        Log.d("MapLocation", "🔍 [3/4] START - Trying HIGH_ACCURACY (GPS)...")
        val startTime = System.currentTimeMillis()
        val cancellationTokenSource = com.google.android.gms.tasks.CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            val elapsed = System.currentTimeMillis() - startTime
            if (location != null) {
                Log.d(
                    "MapLocation",
                    "✅ [3/4] SUCCESS - Got HIGH_ACCURACY in ${elapsed}ms: (${location.latitude}, ${location.longitude})"
                )
                handleLocationResult(location.latitude, location.longitude)
            } else {
                Log.d("MapLocation", "⚠️ [3/4] NULL - HIGH_ACCURACY is null after ${elapsed}ms")
                // Last resort: try LocationManager
                tryLocationManager()
            }
        }.addOnFailureListener { e ->
            val elapsed = System.currentTimeMillis() - startTime
            Log.e(
                "MapLocation",
                "❌ [3/4] FAIL - HIGH_ACCURACY failed after ${elapsed}ms: ${e.message}"
            )
            tryLocationManager()
        }
    }

    @SuppressLint("MissingPermission")
    private fun tryLocationManager() {
        Log.d("MapLocation", "🔍 [4/4] START - Trying LocationManager (fallback)...")
        val startTime = System.currentTimeMillis()

        try {
            val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Check if location is enabled
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.d("MapLocation", "📍 GPS enabled: $isGpsEnabled, Network enabled: $isNetworkEnabled")

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.e("MapLocation", "❌ [4/4] FAIL - No location providers enabled")
                showLocationError("Vui lòng bật định vị trong cài đặt thiết bị")
                return
            }

            // Try network provider first (faster)
            var location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val elapsed1 = System.currentTimeMillis() - startTime

            if (location != null) {
                Log.d(
                    "MapLocation",
                    "✅ [4/4] SUCCESS - Got NETWORK location in ${elapsed1}ms: (${location.latitude}, ${location.longitude})"
                )
            } else {
                Log.d(
                    "MapLocation",
                    "⚠️ NETWORK location is null after ${elapsed1}ms, trying GPS..."
                )
                // If network provider fails, try GPS
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val elapsed2 = System.currentTimeMillis() - startTime

                if (location != null) {
                    Log.d(
                        "MapLocation",
                        "✅ [4/4] SUCCESS - Got GPS location in ${elapsed2}ms: (${location.latitude}, ${location.longitude})"
                    )
                } else {
                    Log.e(
                        "MapLocation",
                        "❌ [4/4] FAIL - Both NETWORK and GPS returned null after ${elapsed2}ms"
                    )
                }
            }

            if (location != null) {
                handleLocationResult(location.latitude, location.longitude)
            } else {
                showLocationError("Không thể lấy vị trí. Vui lòng:\n• Bật định vị\n• Đợi vài giây để GPS khởi động\n• Thử lại")
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            Log.e("MapLocation", "❌ [4/4] EXCEPTION after ${elapsed}ms: ${e.message}")
            showLocationError("Lỗi: ${e.message ?: "Không thể truy cập dịch vụ định vị"}")
        }
    }

    private fun showLocationError(message: String) {
        android.widget.Toast.makeText(
            requireContext(),
            message,
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun handleLocationResult(lat: Double, lon: Double) {
        val geoPoint = GeoPoint(lat, lon)

        // Update ViewModel with current location
        viewModel.currentLocation = geoPoint

        // Center map on current location
        viewBinding.mapView.controller.setCenter(geoPoint)
        viewBinding.mapView.controller.setZoom(viewModel.zoomMap)

        // Show current location marker
        showCurrentLocationMarker(lat, lon)

        // Search nearby pharmacies
        viewModel.searchPharmacyNear(
            latitude = lat.toString(),
            longitude = lon.toString()
        )
    }

    private fun showCurrentLocationMarker(lat: Double, lon: Double) {
        // Remove old current location marker if exists
        viewBinding.mapView.overlays.removeAll { overlay ->
            overlay is Marker && overlay.title == "Vị trí của bạn"
        }

        val currentMarker = Marker(viewBinding.mapView).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Vị trí của bạn"
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location)
        }

        viewBinding.mapView.overlays.add(0, currentMarker)

        // Tự động hiển thị InfoWindow (title)
        currentMarker.showInfoWindow()

        viewBinding.mapView.invalidate()
    }

    private fun navigateToFilterScreen() {
        findNavController().navigate(R.id.action_mainScreen_to_pharmacySearchFragment)
    }
}