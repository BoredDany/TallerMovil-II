package com.example.tallermovil_ii

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tallermovil_ii.model.Location
import org.json.JSONObject
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

class MapActivity : AppCompatActivity() {

    private lateinit var map : MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()

        map.onPause()
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
}