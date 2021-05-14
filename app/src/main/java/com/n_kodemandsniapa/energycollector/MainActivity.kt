package com.n_kodemandsniapa.energycollector

//import com.google.android.gms.location.R


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    // Geolocation
    var prevLatitude:Double = 0.0
    var prevLongtitude:Double = 0.0
    // Items to save -----
    var lastestLatitude:Double = 0.0
    var lastestLongtitude:Double = 0.0
    // Distance
    var distanceWalked:Double = 0.0 // meters
    // -----
    var distanceToWalk:Double = 1000.0 // meters
    lateinit var mainHandler: Handler
    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            onTick()
            if(prevLatitude != 0.0 || prevLongtitude != 0.0){
                val actualMeasure = measure(prevLatitude,prevLongtitude,lastestLatitude,lastestLongtitude)
                if(actualMeasure<176){
                    distanceWalked+=actualMeasure
                }
            }
            if(distanceToWalk<distanceWalked){
                TextProgress.text = "100%"
            }
            else{
                TextProgress.text ="${(distanceWalked/distanceToWalk*100).roundToInt()}%"
            }
            mainHandler.postDelayed(this, 60000)
        }
    }

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    var PERMISSION_ID = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        // Timer
        if(CheckPermission()){
            RequestPermission()
        }
        onTick()
        mainHandler = Handler(Looper.getMainLooper())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
    }
    @SuppressLint("SetTextI18n", "MissingPermission")
    private fun  getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    var location:Location?= task.result
                    if(location == null){
                        Toast.makeText(this,"Location Null",Toast.LENGTH_SHORT).show()
                        NewLocationData()
                    }else{
                        // Values Update
                        prevLatitude=lastestLatitude
                        prevLongtitude=lastestLongtitude
                        lastestLongtitude = location.longitude
                        lastestLatitude = location.latitude
                        // Logs and location display
                        Log.d("Debug:" ,"Your Location:"+ location.longitude)
                        locationView.text = "You Current Location is : Long: "+ location.longitude + " , Lat: " + location.latitude + "\n" + getCityName(location.latitude,location.longitude)
                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }
    @SuppressLint("MissingPermission")
    private fun NewLocationData(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
    }
    private val locationCallback = object :LocationCallback(){
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation=p0.lastLocation
            locationView.text = "Your Current Coordinates are: \nLat:" + lastLocation.latitude + "Long:"+ lastLocation.longitude
        }
    }
    private fun CheckPermission():Boolean{
        if((ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)==PackageManager.PERMISSION_GRANTED)&&
            (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)&&
            (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)){
            return true
        }
        return false
    }
    private fun RequestPermission(){
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not guaranted
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.INTERNET),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled():Boolean{
        var locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun getCityName(lat: Double,long: Double):String{
        var cityName:String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat,long,3)

        cityName = Adress[0].locality
        countryName = Adress[0].countryName
        Log.d("Debug:", "Your City: $cityName ; your Country $countryName")
        return cityName
    }
    // Timer Func
    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTextTask)
    }
    // On timer tick
    fun onTick() {
        getLastLocation()
    }
    fun measure(lat1:Double,lon1:Double,lat2:Double,lon2:Double):Double{
        val rad = 6378.137
        val dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180
        val dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180
        val a = sin(dLat/2) * sin(dLat/2) + cos(lat1 * Math.PI / 180) * cos(lat2 * Math.PI / 180) * sin(dLon/2) * sin(dLon/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        val d = rad * c
        return d * 1000
    }
}