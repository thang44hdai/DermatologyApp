package com.example.safeaid.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
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
        // Animate to pharmacy location
        val point = GeoPoint(pharmacy.latitude, pharmacy.longitude)
        viewBinding.mapView.controller.animateTo(point)
        viewModel.targetLocation = point

        val bottomSheet = PharmacyBottomSheet.newInstance(pharmacy)
        bottomSheet.setOnClick(object : PharmacyBottomSheet.OnClickPharmacyBottomSheet {
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
                if (viewModel.directionToLocation != null) {
                    val target = viewModel.directionToLocation
                    val current = viewModel.currentLocation
                    val dest = GeoPoint(target?.latitude ?: 0.0, target?.longitude ?: 0.0)
                    viewModel.getRoute(current, dest)
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

    private fun navigateToFilterScreen() {
        findNavController().navigate(R.id.action_mainScreen_to_pharmacySearchFragment)
    }

    private fun mockDataMap() {
        val mockPharmacies = listOf(
            PharmacyResponse(
                id = "1",
                name = "Nhà Thuốc Minh Tâm",
                address = "123 Nguyễn Trãi, Quận 1, TP.HCM",
                distanceKm = "0.5",
                latitude = 10.762622,
                longitude = 106.660172,
                openTime = "08:00",
                closeTime = "22:00",
                phone = "0901 234 567",
                ratings = "4.5",
                images = listOf(
                    "https://example.com/pharmacy1/img1.jpg",
                    "https://example.com/pharmacy1/img2.jpg"
                ),
                logoUrl = "https://example.com/pharmacy1/logo.png"
            ),
            PharmacyResponse(
                id = "2",
                name = "Nhà Thuốc Long Châu",
                address = "45 Lê Duẩn, Quận 1, TP.HCM",
                distanceKm = "1.2",
                latitude = 10.781111,
                longitude = 106.699722,
                openTime = "07:30",
                closeTime = "23:00",
                phone = "028 3911 2233",
                ratings = "4.7",
                images = listOf(
                    "https://example.com/pharmacy2/img1.jpg"
                ),
                logoUrl = "https://example.com/pharmacy2/logo.png"
            ),
            PharmacyResponse(
                id = "3",
                name = "Pharmacity Pasteur",
                address = "85 Pasteur, Quận 3, TP.HCM",
                distanceKm = "0.9",
                latitude = 10.779783,
                longitude = 106.696564,
                openTime = "07:00",
                closeTime = "23:00",
                phone = "1800 6821",
                ratings = "4.4",
                images = listOf(),
                logoUrl = "https://example.com/pharmacy3/logo.png"
            ),
            PharmacyResponse(
                id = "4",
                name = "Nhà Thuốc Eco Pharma",
                address = "12 Nguyễn Văn Cừ, Quận 5, TP.HCM",
                distanceKm = "2.3",
                latitude = 10.753468,
                longitude = 106.666992,
                openTime = "08:00",
                closeTime = "21:00",
                phone = "0902 888 111",
                ratings = "4.2",
                images = listOf(
                    "https://example.com/pharmacy4/img1.jpg"
                ),
                logoUrl = "https://example.com/pharmacy4/logo.png"
            ),
            PharmacyResponse(
                id = "5",
                name = "Nhà Thuốc Hồng Phát",
                address = "200 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM",
                distanceKm = "3.1",
                latitude = 10.802165,
                longitude = 106.711105,
                openTime = "08:30",
                closeTime = "21:30",
                phone = "0933 456 789",
                ratings = "4.0",
                images = listOf(),
                logoUrl = null
            ),
            PharmacyResponse(
                id = "6",
                name = "Pharmacity Đinh Tiên Hoàng",
                address = "250 Đinh Tiên Hoàng, Quận 1, TP.HCM",
                distanceKm = "1.8",
                latitude = 10.799000,
                longitude = 106.700900,
                openTime = "07:00",
                closeTime = "23:00",
                phone = "1800 6821",
                ratings = "4.6",
                images = listOf(),
                logoUrl = "https://example.com/pharmacy6/logo.png"
            ),
            PharmacyResponse(
                id = "7",
                name = "Nhà Thuốc Việt",
                address = "55 Hai Bà Trưng, Quận 1, TP.HCM",
                distanceKm = "0.7",
                latitude = 10.779210,
                longitude = 106.699559,
                openTime = "08:00",
                closeTime = "22:00",
                phone = "0909 123 456",
                ratings = "4.3",
                images = listOf(),
                logoUrl = null
            ),
            PharmacyResponse(
                id = "8",
                name = "Nhà Thuốc An Khang",
                address = "23 Nguyễn Thị Minh Khai, Quận 1, TP.HCM",
                distanceKm = "1.5",
                latitude = 10.782620,
                longitude = 106.695700,
                openTime = "07:30",
                closeTime = "22:00",
                phone = "028 7777 8888",
                ratings = "4.5",
                images = listOf(),
                logoUrl = "https://example.com/pharmacy8/logo.png"
            ),
            PharmacyResponse(
                id = "9",
                name = "Nhà Thuốc Trung Sơn",
                address = "10 Cống Quỳnh, Quận 1, TP.HCM",
                distanceKm = "1.1",
                latitude = 10.768720,
                longitude = 106.689900,
                openTime = "08:00",
                closeTime = "23:00",
                phone = "028 3456 7890",
                ratings = "4.1",
                images = listOf(),
                logoUrl = "https://example.com/pharmacy9/logo.png"
            ),
            PharmacyResponse(
                id = "10",
                name = "Pharmacity Nguyễn Thị Thập",
                address = "450 Nguyễn Thị Thập, Quận 7, TP.HCM",
                distanceKm = "5.0",
                latitude = 10.737590,
                longitude = 106.721940,
                openTime = "07:00",
                closeTime = "23:00",
                phone = "1800 6821",
                ratings = "4.6",
                images = listOf(),
                logoUrl = null
            )
        )
        viewModel.updateState(DataResult.Success(MapState.PharmaciesNearBy(mockPharmacies)))
    }
}