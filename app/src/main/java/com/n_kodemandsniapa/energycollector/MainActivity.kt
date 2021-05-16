package com.n_kodemandsniapa.energycollector

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_first.*
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
            val actualMeasure = measure(prevLatitude,prevLongtitude,lastestLatitude,lastestLongtitude)
            if(actualMeasure<176){
                distanceWalked+=actualMeasure
            }
            textProgress.text="ADFGWREGAG"
            if(distanceToWalk<distanceWalked){
                textProgress.text = "100%"
            }
            else{
                textProgress.text ="${(distanceWalked/distanceToWalk*100).roundToInt()}%"
            }
            mainHandler.postDelayed(this, 60000)
        }
    }

    

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var PERMISSION_ID = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        // Timer
        if(CheckPermission()){
            RequestPermission()
        }
        this.fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(this)
        onTick()
        textProgress.text="XD"
        this.mainHandler = Handler(Looper.getMainLooper())

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Autorzy: Nikodem Reszka i Hubert Wasilewski", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
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
                        this.locationView.text = "You Current Location is : Long: "+ location.longitude + " , Lat: " + location.latitude + "\n" + getCityName(location.latitude,location.longitude)
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
        val geoCoder = Geocoder(this, Locale.getDefault())
        val Adress = geoCoder.getFromLocation(lat,long,3)

        val cityName:String = Adress[0].locality
        val countryName = Adress[0].countryName
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