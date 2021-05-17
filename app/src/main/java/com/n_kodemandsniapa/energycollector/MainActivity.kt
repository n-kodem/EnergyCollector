package com.n_kodemandsniapa.energycollector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    val FILENAME = "save.txt"


    // Geolocation
    // Items to save -----
    // Distance
    var distanceWalked:Double = 0.0 // meters

    var distanceToWalk:Double = 1000.0 // meters

    var startPoint = Location("locationA")

    var endPoint = Location("locationA")

    var isOverloaded = false
    // -----
    lateinit var mainHandler: Handler

    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            getLastLocation()

            val distance = startPoint.distanceTo(endPoint).toDouble()
            val results = FloatArray(1)
            Log.d("Lats", "${startPoint.latitude} ${endPoint.latitude}")
            Log.d("LONGS","${startPoint.longitude} ${endPoint.latitude}")
            Location.distanceBetween(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.latitude, results)

            Log.d("distance","${distance}")
            Log.d("NEXTdistance", results[0].toString())
            if(distance<176){
                distanceWalked+=distance
            }
            if (distanceToWalk<distanceWalked){
                distanceWalked = 0.0
                isOverloaded=true
            }
            else{
                isOverloaded=false
            }

            val parts  = LoadData().split(":")

            val new_content = distanceWalked.toString()+":"+endPoint.latitude.toString()+":"+endPoint.longitude.toString()+":"+isOverloaded.toString()
            Log.d("wszystko","$parts")
            SaveData(new_content)

            // Next calling this in 30s
            mainHandler.postDelayed(this, 30000)
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
        fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(this)
        // First Time Location Loading
        loadLocFirstTime()

        // Creating/Downloading file data
        val SAVEFILE = File(filesDir, FILENAME)
        if(!SAVEFILE.exists())
        {
            SAVEFILE.createNewFile()
            val new_content = distanceWalked.toString()+":"+endPoint.latitude.toString()+":"+endPoint.longitude.toString()+":"+isOverloaded.toString()

            openFileOutput(SAVEFILE.name, Context.MODE_PRIVATE).use {
                it.write(new_content.toByteArray())
            }
        }
        else{
            val parts = LoadData().split(":")
            distanceWalked = parts[0].toDouble()
            endPoint.latitude = parts[1].toDouble()
            endPoint.longitude = parts[2].toDouble()
            isOverloaded = parts[3].toBoolean()
        }

        // Creating mainLoop
        this.mainHandler = Handler(Looper.getMainLooper())

        // OnClick listener to show authors names
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Autorzy: Nikodem Reszka i Hubert Wasilewski", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Get Location obj
    @SuppressLint("SetTextI18n", "MissingPermission")
    private fun  getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    val location = task.result
                    if(location == null){
                        Toast.makeText(this,"Location Null",Toast.LENGTH_SHORT).show()
                        NewLocationData()
                    }else{
                        // StartPoints
                        startPoint.latitude = endPoint.latitude
                        startPoint.longitude = endPoint.longitude

                        // EndPoints
                        Log.d("LASTLOC","LAT ${endPoint.latitude} LONG ${endPoint.longitude}")
                        endPoint.latitude = location.latitude
                        endPoint.longitude = location.longitude
                        Log.d("NEWLOC","LAT ${endPoint.latitude} LONG ${endPoint.longitude}")
                        // Saving new data to file
                        //val new_content = parts[0] + ":" + endPoint.latitude + ":"+  endPoint.longitude+":"+parts[3]
                        val new_content = distanceWalked.toString() + ":" + endPoint.latitude + ":"+  endPoint.longitude+":"+isOverloaded.toString()

                        SaveData(new_content)

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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper())
    }
    private val locationCallback = object :LocationCallback(){
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation=p0.lastLocation
            //locationView.text = "Your Current Coordinates are: \nLat:" + lastLocation.latitude + "Long:"+ lastLocation.longitude
        }
    }
    private fun CheckPermission():Boolean{
        if((ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)==PackageManager.PERMISSION_GRANTED)&&
                (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)&&
                (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)){
            return true
        }
        return false
    }
    private fun RequestPermission(){
        //this function will allows us to tell the user to request the necessary permission if they are not
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET),
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

    // Timer Func
    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTextTask)
    }
    fun SaveData(data:String){
        val SAVEFILE = File(filesDir, FILENAME)
        FileOutputStream(SAVEFILE).write(
                data.toByteArray()
        )
    }
    fun LoadData(): String {
        var data = openFileInput(FILENAME).bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                "$some$text"
            }
        }
        return data
    }
    @SuppressLint("MissingPermission")
    fun loadLocFirstTime(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    val location = task.result
                    if(location == null){
                        Toast.makeText(this,"Location Null",Toast.LENGTH_SHORT).show()
                        NewLocationData()
                    }else{
                        // StartPoints
                        startPoint.latitude = location.latitude
                        startPoint.longitude = location.longitude

                        // EndPoints
                        endPoint.latitude = location.latitude
                        endPoint.longitude = location.longitude

                        // Saving data to file
                        val new_content = distanceWalked.toString() + ":" + endPoint.latitude + ":"+  endPoint.longitude+":"+isOverloaded.toString()

                        SaveData(new_content)

                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }
}