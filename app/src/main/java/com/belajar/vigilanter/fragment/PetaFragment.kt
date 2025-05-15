package com.belajar.vigilanter.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.belajar.vigilanter.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PetaFragment : Fragment(), OnMapReadyCallback {
    private lateinit var Map: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var koordinat: TextView
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var  locationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_peta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        koordinat = view.findViewById(R.id.koordinat)

        //set Map
        mapView = view.findViewById(R.id.mMap)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle =
                savedInstanceState.getBundle("MapViewBundleKey")
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(activity, "Izin lokasi tidak di aktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                getLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.Map = googleMap
        getLocation()
    }

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // get Permission
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 10
            )
        } else {
            // get Location
            locationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude

                    val lat = location.latitude.toString()
                    val longi = location.longitude.toString()
                    koordinat.text = "$lat, $longi"

                    Log.d("koordinat String", "$lat $longi")

                    val userLocation = LatLng(latitude, longitude)
                    Map.addMarker(MarkerOptions().position(userLocation).title("Lokasi Pengguna"))

                    val schoolLocation = LatLng(3.604839, 98.643812)
                    Map.addMarker(
                        MarkerOptions()
                            .position(schoolLocation).title("Marker in Medan")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )

                    val zoomLevel = 16.0f
                    Map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel))
                } else {
                    Toast.makeText(activity, "Lokasi tidak aktif!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


}