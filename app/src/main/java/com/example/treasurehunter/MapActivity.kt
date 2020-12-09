package com.example.treasurehunter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var latLng: LatLng
    private var stageIndex = 0
    private val stages = arrayListOf<Stage>()

    companion object {
        private const val UPDATE_INTERVAL: Long = 5000
        private const val FASTEST_INTERVAL: Long = 1000
    }

    class Stage(
        val latLng: LatLng,
        val title: String,
        val answer: String
    )

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(map: GoogleMap) {
        mLocationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }

        val locationSettingsRequest =
            LocationSettingsRequest.Builder().run {
                addLocationRequest(mLocationRequest)
                build()
            }

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation

                    //第一關等取得使用者位置後才移動相機
                    if (!::latLng.isInitialized) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        cameraToNearestStation(map, stages[stageIndex].latLng, userLatLng)
                    }

                    latLng = LatLng(location.latitude, location.longitude)
                    Log.e("latLng", "${latLng.latitude}, ${latLng.longitude}")
                }
            },
            Looper.myLooper()
        )
    }

    //回傳權限要求後的結果
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (grantResults.isNotEmpty() && requestCode == 0) {
            for (result in grantResults)
                if (result != PackageManager.PERMISSION_GRANTED)
                    finish()
            loadMap()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        stages.add(Stage(LatLng(25.033611, 121.565000), "台北101", ""))
        stages.add(Stage(LatLng(25.047924, 121.517081), "台北車站", ""))
        stages.add(Stage(LatLng(25.026158, 121.542709), "台北科大", ""))

        loadMap()
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0)
        } else {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false

            startLocationUpdates(map)
            setListener(map)
            setStage(map)
        }
    }

    private fun loadMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment)
                as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setListener(map: GoogleMap) {
        img_location.setOnClickListener {
            if (!::latLng.isInitialized)
                Toast.makeText(this, "Can not get your location", Toast.LENGTH_SHORT).show()
            else
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(latLng.latitude, latLng.longitude), 13f
                    )
                )
        }

        img_search.setOnClickListener {
            val i = Intent(this, ImageActivity::class.java)
            i.putExtra("Stage", stageIndex)
            i.putExtra("Answer", stages[stageIndex].answer)
            startActivity(i)
        }
    }

    private fun setStage(map: GoogleMap) {
        val currentStage = stageIndex + 1
        val totalStage = stages.size

        if (currentStage > totalStage) {
            tv_stage_title.visibility = View.GONE
            tv_stage.text = "Go to get the treasure!"

            val marker = MarkerOptions()
            val location = LatLng(25.033303, 121.535844)
            marker.position(location)
            marker.title("藏寶箱")
            map.addMarker(marker)

            cameraToNearestStation(map, location, latLng)
        } else {
            tv_stage.text = "$currentStage/$totalStage"

            val stage = stages[stageIndex]
            val marker = MarkerOptions()
            marker.position(stage.latLng)
            marker.title(stage.title)
            marker.draggable(true)
            map.addMarker(marker)
        }

//        map.moveCamera(
//            CameraUpdateFactory.newLatLngZoom(
//                LatLng(25.035, 121.54), 13f
//            )
//        )


    }

    private fun cameraToNearestStation(map: GoogleMap, station: LatLng, target: LatLng) {
        val bounds = LatLngBounds.Builder().run {
            include(station)
            include(target)
            build()
        }

        val metrics = resources.displayMetrics
        val cu = CameraUpdateFactory.newLatLngBounds(
            bounds,
            (metrics.widthPixels * 0.8).toInt(),
            (metrics.heightPixels * 0.88).toInt(),
            (metrics.widthPixels * 0.2).toInt()
        )

        map.moveCamera(cu)
        map.moveCamera(CameraUpdateFactory.zoomTo(map.cameraPosition.zoom + 0.5f))
    }
}