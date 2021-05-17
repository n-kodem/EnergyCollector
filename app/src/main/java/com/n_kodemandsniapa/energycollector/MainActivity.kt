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
    // -----
    lateinit var mainHandler: Handler

    private val updateTextTask = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            getLastLocation()


            val SAVEFILE = File(filesDir, FILENAME);


            val distance = startPoint.distanceTo(endPoint).toDouble()


            Log.d("LONGS","${startPoint.longitude} ${endPoint.latitude}")
            Log.d("Lats", "${startPoint.latitude} ${endPoint.latitude}")
            Log.d("kurwa","${distance}")
            if(distance<176){
                distanceWalked+=distance
            }

            var content = openFileInput(FILENAME).bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }
            val parts  = content.split(":")

            val new_content = distanceWalked.toString() + ":" + parts[1] + ":"+ parts[2]

            FileOutputStream(SAVEFILE).write(
                    new_content.toByteArray()
            )
            mainHandler.postDelayed(this, 30000)
        }
    }



    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var PERMISSION_ID = 1000

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar))
        // Timer
        if(CheckPermission()){
            RequestPermission()
        }
        fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(this)

        val SAVEFILE = File(filesDir, FILENAME)

        getLastLocation()
        getLastLocation()

        if(!SAVEFILE.exists())
        {
            SAVEFILE.createNewFile()
            val new_content = distanceWalked.toString()+":"+endPoint.latitude.toString()+":"+endPoint.longitude.toString()

            openFileOutput(SAVEFILE.name, Context.MODE_PRIVATE).use {
                it.write(new_content.toByteArray())
            }
        }
        else{

            var content = openFileInput(FILENAME).bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }

            val parts = content.split(":")

            distanceWalked = parts[0].toDouble()
            endPoint.latitude = parts[1].toDouble()
            endPoint.longitude = parts[2].toDouble()
        }

        this.mainHandler = Handler(Looper.getMainLooper())

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Autorzy: Nikodem Reszka i Hubert Wasilewski", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
        }
        //setContentView(R.layout.fragment_first);
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

    @SuppressLint("SetTextI18n", "MissingPermission")
    private fun  getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    var location = task.result
                    val SAVEFILE = File(filesDir, FILENAME);
                    if(location == null){
                        Toast.makeText(this,"Location Null",Toast.LENGTH_SHORT).show()
                        NewLocationData()
                    }else{
                        // Values Update
                        var content = openFileInput(FILENAME).bufferedReader().useLines { lines ->
                            lines.fold("") { some, text ->
                                "$some\n$text"
                            }
                        }
                        val parts  = content.split(":")



                        startPoint.latitude = endPoint.latitude
                        startPoint.longitude = endPoint.longitude


                        endPoint.latitude = location.latitude
                        endPoint.longitude = location.longitude

                        val new_content = parts[0] + ":" + endPoint.latitude + ":"+  endPoint.longitude

                        FileOutputStream(SAVEFILE).write(
                                new_content.toByteArray()
                        )

                        // Logs and location display
                        Log.d("Debug:" ,"Your Location: long"+ location.longitude +"lat"+ location.latitude)

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
}