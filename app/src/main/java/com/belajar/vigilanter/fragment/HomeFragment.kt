package com.belajar.vigilanter.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle

import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.belajar.vigilanter.CameraActivity
import com.belajar.vigilanter.LaporanActivity
import com.belajar.vigilanter.Login
import com.belajar.vigilanter.PratinjauVideoActivity
import com.belajar.vigilanter.R
import com.belajar.vigilanter.data.ViewModel.LocationViewModel

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.log


class HomeFragment : Fragment(), View.OnClickListener {
    lateinit var panggilBtn: ImageView
    lateinit var laporBtn: ImageView
    lateinit var notifBtn: ImageView
    private var isBtnLaporPressed: Boolean = false
    private val CAMERA_CODE: Int = 100
    private val VIDEO_CODE: Int = 101
    private var videoPath: Uri? = null
    private var nama: String? = null
    private var userId: Int = 0
    private var road: String = "null"
    private lateinit var namaUser: TextView
    private lateinit var namaUser2: TextView
    private lateinit var lokasi: TextView
    private lateinit var pg: ProgressBar
    private lateinit var bg: FrameLayout
    private lateinit var log_out: ImageView

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var  locationProviderClient: FusedLocationProviderClient

    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        namaUser = view.findViewById(R.id.namaUser)
        namaUser2 = view.findViewById(R.id.namaUser2)
        lokasi = view.findViewById(R.id.lokasi)
        pg = view.findViewById(R.id.progressBar)
        bg = view.findViewById(R.id.overlayLayout)
        log_out =  view.findViewById(R.id.logout)

        log_out.setOnClickListener {
            val intent = Intent(requireActivity(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // mengambil data
        arguments?.let { bundle ->
            nama = bundle.getString("username")
            userId = bundle.getInt("user_id")
            lokasi.text = bundle.getString("lokasi")
            namaUser.text = nama
            namaUser2.text = "Halo $nama"
        }

        panggilBtn = view.findViewById(R.id.panggil)
        panggilBtn.setOnClickListener(this)

        notifBtn = view.findViewById(R.id.notif)
        notifBtn.setOnClickListener(this)

        laporBtn = view.findViewById(R.id.lapor)
        laporBtn.setOnClickListener(this)

//        if (isCameraPresence()) {
//            Log.i("VDT", "Camera terdeteksi")
//            getCameraPermission()
//        } else {
//            Log.i("VDT", "Camera tidak terdeteksi")
//        }

        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

//        showLoading(true)
        // Gunakan data dari ViewModel
        if (locationViewModel.streetName == null) {
            getLocation()
        }else{
            road = locationViewModel.streetName.toString()
            lokasi.text = road
        }


    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.panggil -> {
                val panggilanFragment = PanggilanFragment()
                val fragmentManager = parentFragmentManager
                fragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragmentContainer, panggilanFragment, PanggilanFragment::class.java.simpleName)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.notif -> {
                val notifFragment = NotificationFragment()
                val fragmentManager = parentFragmentManager
                fragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragmentContainer, notifFragment, NotificationFragment::class.java.simpleName)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.lapor -> {
                val intent = Intent(requireActivity(), CameraActivity::class.java)
                intent.putExtra("user_id",userId )
                intent.putExtra("lokasi", road )
                intent.putExtra("koordinat","${latitude}, ${longitude}")
                startActivity(intent)
            }
        }
    }

    fun isCameraPresence(): Boolean {
        return requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun getCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_CODE)
        }else{
            if(isBtnLaporPressed){
//                recordVideo()
            }
        }
        isBtnLaporPressed = false
    }

    private fun recordVideo(){
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == VIDEO_CODE){
            videoPath = data?.data
            if(resultCode == AppCompatActivity.RESULT_OK){
                Log.i("VDT", "path Video = "+videoPath)

                val intent = Intent(requireActivity(), LaporanActivity::class.java)
                intent.putExtra("user_id",userId )
                intent.putExtra("lokasi", road )
                startActivity(intent)
            }else if (resultCode == AppCompatActivity.RESULT_CANCELED){
                Log.i("VDT", "Video Canceled")
            }else{
                Log.i("VDT", "Video has got some error")
            }
        }
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

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
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
                    getAddressFromCoordinates(latitude, longitude)
                } else {
                    Toast.makeText(activity, "Lokasi tidak aktif!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressFromCoordinates(lat: Double, lng: Double) {
        showLoading(true)
        val client = OkHttpClient()

        val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Geocoding", "Error: ${e.message}")
                requireActivity().runOnUiThread {
                    showLoading(false)
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    showLoading(false)
                }

                if (!response.isSuccessful) {
                    Log.e("Geocoding", "Error: ${response.message}")
                    return
                }

                val jsonResponse = response.body?.string()
                try {
                    val jsonObject = JSONObject(jsonResponse)
                    val address = jsonObject.getJSONObject("address")
                    road = address.optString("road", "Unknown road")
                    Log.d("Geocoding", "Road: $road")
                    locationViewModel.streetName = road

                    activity?.runOnUiThread {
                        lokasi.text = road
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    @SuppressLint("ResourceType")
    private fun showLoading(isLoading: Boolean) {
        val navView = requireActivity().findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        if (isLoading) {
            pg.visibility = View.VISIBLE
            bg.visibility = View.VISIBLE
            navView.isClickable = false
            laporBtn.isClickable = false
            panggilBtn.isClickable = false
        } else {
            pg.visibility = View.GONE
            bg.visibility = View.GONE
            navView.isClickable = true
            laporBtn.isClickable = true
            panggilBtn.isClickable = true
            navView.show(2)
        }
    }

}