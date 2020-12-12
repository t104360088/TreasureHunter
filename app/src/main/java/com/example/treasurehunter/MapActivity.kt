package com.example.treasurehunter

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var userLatLng: LatLng
    private var stageIndex = 0
    private val stages = arrayListOf<Stage>()
    private var distance = 0

    companion object {
        private const val UPDATE_INTERVAL: Long = 5000
        private const val FASTEST_INTERVAL: Long = 1000
        private const val DISTANCE_LIMITE = 50
    }

    class Stage(
        val latLng: LatLng,
        val title: String,
        val hint: String,
        val answer: String
    )

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == 0) {
            data?.let {
                stageIndex = it.getIntExtra("NextStageIndex", 0)
                mMap.clear()
                setStage()
                doDistance()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        stages.add(Stage(LatLng(25.043800, 121.534060), "第六教學大樓", "It's stupid but can fly", "Bird"))
        stages.add(Stage(LatLng(25.043390, 121.533200), "土木館", "Travel a thousand miles a day", "Vehicle"))
        stages.add(Stage(LatLng(25.043540, 121.535800), "中正館", "Related to December", "Christmas"))

        loadMap()
        countdown()
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
            mMap = map
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false

            startLocationUpdates()
            setListener()
            setStage()
        }
    }

    private fun countdown() {
        view_anim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                cl_anim?.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })

        view_anim.playAnimation()
    }

    private fun loadMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment)
                as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
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
                    if (!::userLatLng.isInitialized) {
                        userLatLng = LatLng(location.latitude, location.longitude)
                        moveCameraToBetween(stages[stageIndex].latLng)
                    }

                    userLatLng = LatLng(location.latitude, location.longitude)
                    doDistance()
                    Log.e("latLng", "${userLatLng.latitude}, ${userLatLng.longitude}")
                }
            },
            Looper.myLooper()
        )
    }

    private fun setListener() {
        img_location.setOnClickListener {
            if (!::userLatLng.isInitialized)
                Toast.makeText(this, "Can not get your location", Toast.LENGTH_SHORT).show()
            else
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(userLatLng.latitude, userLatLng.longitude), 18f
                    )
                )
        }

        img_search.setOnClickListener {
            val currentStage = stageIndex + 1
            val totalStage = stages.size

            if (currentStage <= totalStage) {
                if (distance <= DISTANCE_LIMITE) {
                    val i = Intent(this, ImageActivity::class.java)
                    i.putExtra("StageIndex", stageIndex)
                    i.putExtra("Hint", stages[stageIndex].hint)
                    i.putExtra("Answer", stages[stageIndex].answer)
                    startActivityForResult(i, 100)
                } else {
                    val msg = "The prompt will be unlocked within $DISTANCE_LIMITE meters from the stage, your current distance is $distance meters"
                    DialogManager.instance.showMessage(this, msg)
                }
            }
        }
    }

    private fun setStage() {
        val currentStage = stageIndex + 1
        val totalStage = stages.size

        if (currentStage > totalStage) { //取得寶藏
            tv_stage_title.visibility = View.GONE
            tv_stage.visibility = View.GONE
            tv_msg.visibility = View.VISIBLE
            img_search.visibility = View.GONE
            tv_search.visibility = View.GONE

            val marker = MarkerOptions()
            val location = LatLng(25.043239, 121.534574)
            marker.position(location)
            marker.title("Treasure")

            val origin = BitmapFactory.decodeResource(this.resources, R.drawable.marker_treasure)
            val scaled = Bitmap.createScaledBitmap(origin, 100, 100, false)

            marker.icon(BitmapDescriptorFactory.fromBitmap(scaled))
            mMap.addMarker(marker)

            moveCameraToBetween(location)
        } else {
            tv_stage.text = "$currentStage/$totalStage"

            val stage = stages[stageIndex]
            val marker = MarkerOptions()
            marker.position(stage.latLng)
            marker.title(stage.title)

            val origin = BitmapFactory.decodeResource(this.resources, R.drawable.marker_stage)
            val scaled = Bitmap.createScaledBitmap(origin, 100, 100, false)

            marker.icon(BitmapDescriptorFactory.fromBitmap(scaled))
            mMap.addMarker(marker)

            //除第一關外更新相機位置
            if (stageIndex != 0)
                moveCameraToBetween(stage.latLng)
        }
    }

    private fun moveCameraToBetween(location: LatLng) {
        val bounds = LatLngBounds.Builder().run {
            include(location)
            include(userLatLng)
            build()
        }

        val metrics = resources.displayMetrics
        val cu = CameraUpdateFactory.newLatLngBounds(
            bounds,
            (metrics.widthPixels * 0.8).toInt(),
            (metrics.heightPixels * 0.88).toInt(),
            (metrics.widthPixels * 0.2).toInt()
        )

        mMap.moveCamera(cu)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition.zoom - 0.5f))
    }

    private fun doDistance() {
        val currentStage = stageIndex + 1
        val totalStage = stages.size

        if (currentStage <= totalStage) {
            val destination = stages[stageIndex].latLng
            val result = FloatArray(1)
            Location.distanceBetween(userLatLng.latitude, userLatLng.longitude, destination.latitude, destination.longitude, result)
            distance = result[0].toInt()
            img_search.alphaAnimation(distance <= DISTANCE_LIMITE)
        } else
            img_search.alphaAnimation(false)
    }

    private fun View?.alphaAnimation(show: Boolean, alphaStart: Float = 1.0f,
                             alphaEnd: Float = 0.1f, duration: Long = 1000) {
        this?.clearAnimation()
        if (show) {
            val alpha = AlphaAnimation(alphaStart, alphaEnd)
            alpha.duration = duration
            alpha.repeatCount = Animation.INFINITE
            alpha.repeatMode = Animation.REVERSE
            this?.animation = alpha
            alpha.start()
        }
    }
}