package com.example.safeaid.screens.map.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<MapState, MapEvent>() {
    var currentLocation = GeoPoint(20.980983103228652, 105.788156785282)
    var targetLocation = GeoPoint(0, 0)
    var zoomMap: Double = 16.0
    var isPredicted: Boolean = false
    var directionToLocation: PharmacyResponse? = null

    private val _mapState = MutableStateFlow<MutableList<GeoPoint>>(mutableListOf())
    val mapState: StateFlow<MutableList<GeoPoint>> = _mapState
    private val _filterState = MutableStateFlow<String>("")
    val filterState: StateFlow<String> = _filterState

    fun filter(query: String) {
        _filterState.value = query
    }

    fun searchPharmacyNear(
        latitude: String,
        longitude: String,
        radiusKm: String = "10",
        limit: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getPharmaciesNearBy(
                        latitude = latitude,
                        longitude = longitude,
                        radiusKm = radiusKm,
                        limit = null
                    )
                },
                callback = { result ->
                    result.doIfSuccess {
                        updateState(DataResult.Success(MapState.PharmaciesNearBy(data = it)))
                    }
                    result.doIfFailure {
                    }
                }
            )
        }
    }

    fun getRoute(current: GeoPoint, dest: GeoPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val apiKey =
                "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImIwZjFlMGE3M2ExMDQ5OGQ4NTZmMDE2M2Q1ODJjMjExIiwiaCI6Im11cm11cjY0In0="
            val url =
                "https://api.openrouteservice.org/v2/directions/foot-walking" +
                        "?api_key=$apiKey" +
                        "&start=${current.longitude},${current.latitude}" +
                        "&end=${dest.longitude},${dest.latitude}"

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val coordinates = json
                    .getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                val routePoints = mutableListOf<GeoPoint>()
                for (i in 0 until coordinates.length()) {
                    val lon = coordinates.getJSONArray(i).getDouble(0)
                    val lat = coordinates.getJSONArray(i).getDouble(1)
                    routePoints.add(GeoPoint(lat, lon))
                }

                _mapState.value = routePoints
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onTriggerEvent(event: MapEvent) {

    }
}

sealed class MapState {
    class PharmaciesNearBy(val data: List<PharmacyResponse>) : MapState()
}

sealed class MapEvent {}