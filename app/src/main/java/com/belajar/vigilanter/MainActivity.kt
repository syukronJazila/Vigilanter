package com.belajar.vigilanter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.belajar.vigilanter.fragment.HomeFragment
import com.belajar.vigilanter.fragment.PetaFragment
import com.belajar.vigilanter.fragment.RiwayatFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var name:String? = null
    private var userId: Int = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var  locationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        name = intent.getStringExtra("username")
        userId = intent.getIntExtra("user_id", 0)

        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        bottomNavigation.add(
            CurvedBottomNavigation.Model(1, "Riwayat", R.drawable.riwayat_ic3)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(2, "Beranda", R.drawable.home_ic2)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(3, "Peta", R.drawable.peta_ic2)
        )

        bottomNavigation.setOnClickMenuListener {
            if(bottomNavigation.isClickable){
                when(it.id){
                    1 -> {
                        replaceFragment(RiwayatFragment())
                    }
                    2 -> {
                        replaceFragment(HomeFragment())
                    }
                    3 -> {
                        replaceFragment(PetaFragment())
                    }
                }
            }
        }

        // default navigation
        replaceFragment(HomeFragment())
        bottomNavigation.show(2)

    }

    private fun replaceFragment(fragment: Fragment) {
        // Untuk menghilangkan fragment di backstack (biar bottom navigation dengan fragment sinkron)
        val fragmentManager = this.supportFragmentManager
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Membuat instance Fragment
        val newFragment = fragment // Ganti dengan fragment yang sesuai

        // Membuat Bundle untuk menyimpan data
        val bundle = Bundle()
        bundle.putString("username",name )
        bundle.putInt("user_id", userId)

        // Mengatur argumen fragment
        fragment.arguments = bundle

        val transaction = fragmentManager.beginTransaction()
        transaction.commit()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, newFragment)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10) {
            // Cek apakah izin diberikan
            val fineLocationGranted = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
            val coarseLocationGranted = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED

            if (fineLocationGranted || coarseLocationGranted) {

            } else {
                // Jika izin lokasi tidak diberikan, tampilkan pesan
                Toast.makeText(this, "Izin lokasi tidak diaktifkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
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
                } else {
                    Toast.makeText(this, "Lokasi tidak aktif!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}