package com.example.tallermovil_ii

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationRequest
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tallermovil_ii.databinding.ActivityMapBinding
import com.example.tallermovil_ii.model.DataBase
import com.example.tallermovil_ii.model.Location
import com.example.tallermovil_ii.permission.Permission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {

    lateinit var binding: ActivityMapBinding
    val TAG = "Update";

    //MAPA
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var map : MapView? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private var currentLocationmarker: Marker? = null
    private var bumpLocationMarker: Marker? = null

    //AUTH
    private lateinit var auth: FirebaseAuth

    //DATABASE
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_map)

        //INITIALIZE
        auth = Firebase.auth

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
                    currentLocationmarker?.let { map!!.overlays.add(it) }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.change -> {

                val firebaseAuth = FirebaseAuth.getInstance()
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val uid = currentUser.uid
                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.getReference(DataBase.PATH_USER).child(uid)

                    userRef.child("status").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val status = dataSnapshot.getValue(Int::class.java)
                                var newStatus = if(status == DataBase.ACTIVE) DataBase.INACTIVE else DataBase.ACTIVE

                                Log.i(TAG, "Estado del usuario: $status")

                                userRef.child("status").setValue(newStatus)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Estado del usuario actualizado exitosamente a: $newStatus")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error al actualizar el estado del usuario: ${e.message}")
                                    }
                            } else {
                                Log.w(TAG, "Status not found")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(TAG, "Error al obtener el estado del usuario: ${databaseError.message}")
                        }
                    })
                } else {
                    Log.w(TAG, "No user signed in")
                }

                true
            }
            R.id.users -> {
                //lanzar activity usuarios lista
                true
            }
            R.id.logout -> {
                //logout
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUser() {
        /*myRef = database.getReference(DataBase.PATH_USER)
        val userRef = myRef.child(auth.currentUser!!.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    val myUser = singleSnapshot.getValue(User::class.java)
                    Log.i(TAG, "Encontró usuario: " + myUser?.name)

                    userRef.child("status").setValue(nuevoNombre)
                        .addOnSuccessListener {
                            Log.d(TAG, "Nombre del usuario actualizado exitosamente a: $nuevoNombre")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al actualizar el nombre del usuario: ${e.message}")
                        }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "error en la consulta", databaseError.toException())
            }
        })*/
    }

}