package com.example.tallermovil_ii

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationRequest
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tallermovil_ii.databinding.ActivityMapBinding
import com.example.tallermovil_ii.model.Location
import com.example.tallermovil_ii.permission.Permission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.config.Configuration.*

class MapActivity : AppCompatActivity() {

    lateinit var binding: ActivityMapBinding

    private var map : MapView? = null



    //MAPA
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private var currentLocationmarker: Marker? = null
    private var bumpLocationMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_map)

        permisoUbicacion()


        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        initialize()

    }


    override fun onResume() {
        super.onResume()
        map!!.onResume()




        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                Log.i("LOCATION", "onSuccess location")
                if (location != null) {
                    Log.i("LOCATION", "Longitud: " + location.longitude)
                    Log.i("LOCATION", "Latitud: " + location.latitude)
                    val mapController = map!!.controller
                    mapController.setZoom(15)
                    val startPoint = GeoPoint(location.latitude, location.longitude);
                    mapController.setCenter(startPoint);
                    currentLocationmarker = createMarkerRetMark(startPoint, "null", null, R.drawable.baseline_location_on_25)
                } else {
                    Log.i("LOCATION", "FAIL location")
                }
            }
        }

        var bumpLocations: List<Location> = readJsonLocations(this, "locations.json")
        if(bumpLocations.isNotEmpty()){
            bumpLocations.forEachIndexed { index, bumpLocation ->
                val (x, y) = bumpLocation
                bumpLocationMarker = createMarkerRetMark(GeoPoint(x, y), bumpLocation.name, null, R.drawable.baseline_location_on_24)
                bumpLocationMarker?.let { map!!.overlays.add(it) }
            }
        }

    }

    override fun onPause() {
        super.onPause()

        map!!.onPause()
    }

    private fun initialize(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        map = findViewById<MapView>(R.id.map)
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setMultiTouchControls(true)
    }







    fun readJsonLocations(context: Context, fileName: String): List<Location> {
        val inputStream = context.assets.open(fileName)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)
        val locationsObject = json.getJSONObject("locations")
        val locations = mutableListOf<Location>()

        // Loop through each location in the JSON object
        locationsObject.keys().forEach { key ->
            val locationObject = locationsObject.getJSONObject(key)
            val latitude = locationObject.getDouble("latitude")
            val longitude = locationObject.getDouble("longitude")
            val name = locationObject.getString("name")
            locations.add(Location(latitude, longitude, name))
        }

        return locations
    }

    private fun createMarkerRetMark(p: GeoPoint, title: String?, desc: String?, iconID: Int): Marker? {
        var marker: Marker? = null
        if (map != null) {
            marker = Marker(map)
            title?.let { marker.title = it }
            desc?.let { marker.subDescription = it }
            if (iconID != 0) {
                val myIcon = resources.getDrawable(iconID, this.theme)
                marker.icon = myIcon
            }
            marker.position = p
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        return marker
    }

    fun permisoUbicacion(){

        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Requiere acceso a ubicación", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Permission.MY_PERMISSION_REQUEST_LOCATION)
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Permission.MY_PERMISSION_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Permission.MY_PERMISSION_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de ubicación negado", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

}