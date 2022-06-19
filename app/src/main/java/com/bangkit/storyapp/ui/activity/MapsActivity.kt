
package com.bangkit.storyapp.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.model.UserModel
import com.bangkit.storyapp.data.remote.response.ResultResponse
import com.bangkit.storyapp.databinding.ActivityMapsBinding
import com.bangkit.storyapp.helper.Helper
import com.bangkit.storyapp.ui.view_model.MapsViewModel
import com.bangkit.storyapp.ui.view_model.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var userModel: UserModel? = null

    private val mapsViewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userModel = intent.getParcelableExtra(EXTRA_USER)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.layoutMaps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style
            )
        )

        gMap.uiSettings.isZoomControlsEnabled = true
        gMap.uiSettings.isIndoorLevelPickerEnabled = true
        gMap.uiSettings.isCompassEnabled = true
        gMap.uiSettings.isMapToolbarEnabled = true

        showData()
        getMyLocation()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getMyLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showData() {
        val boundsBuilder = LatLngBounds.Builder()
        userModel?.let { it ->
            mapsViewModel.getStories(it.token).observe(this) {
                if (it != null) {
                    when (it) {
                        is ResultResponse.Loading -> {
                            binding.progressBarMaps.visibility = View.VISIBLE
                        }
                        is ResultResponse.Success -> {
                            binding.progressBarMaps.visibility = View.GONE
                            it.data.forEachIndexed { _, element ->
                                val lastLatLng = LatLng(element.lat, element.lon)

                                gMap.addMarker(MarkerOptions().position(lastLatLng).title(element.name))
                                boundsBuilder.include(lastLatLng)
                                val bounds: LatLngBounds = boundsBuilder.build()
                                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
                            }
                        }
                        is ResultResponse.Error -> {
                            binding.progressBarMaps.visibility = View.GONE
                            Helper.showToast(this, getString(R.string.error))
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_USER = "user"
    }
}