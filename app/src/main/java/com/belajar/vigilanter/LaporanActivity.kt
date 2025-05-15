package com.belajar.vigilanter

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.belajar.vigilanter.data.response.LaporResponse
import com.belajar.vigilanter.data.retrofit.ApiConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.belajar.vigilanter.data.ViewModel.LaporanViewModel
import com.belajar.vigilanter.data.ViewModel.LocationViewModel
import com.belajar.vigilanter.data.response.LaporanVideoResponse
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LaporanActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var backBtn: ImageView
    private lateinit var adapterItems: ArrayAdapter<String>
    private var jenisLaporan: String = ""
    private var namaKejahatan: String = ""
    private lateinit var namaKejahatanEditText: EditText
    private lateinit var karakterTextView: TextView
    private lateinit var deskripsi: EditText
    private lateinit var kirimBtn: AppCompatButton
    private lateinit var pg: ProgressBar
    private lateinit var bg: FrameLayout
    private lateinit var pratinjau: TextView
    private lateinit var lokasiTv: TextView
    private var userId: Int = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var lokasi: String = ""
    private var videoPath: String = ""
    private lateinit var locationProviderClient: FusedLocationProviderClient

    private lateinit var laporanViewModel: LaporanViewModel

    var arrayJenisLaporan: Array<String> = arrayOf(
        "Laporkan Kejadian", "Tambahan Bukti"
    )

    var arrayNamaKejahatan: Array<String> = arrayOf(
        "Begal", "Jambret", "Pencopetan", "Perampokan", "Penganiayaan", "Pemalakan",
        "Perusakan Kendaraan", "Pengrusakan Fasilitas Umum", "Lainnya"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        laporanViewModel = ViewModelProvider(this).get(LaporanViewModel::class.java)
        setContentView(R.layout.laporan_activity)

        lokasiTv = findViewById(R.id.lokasi)

        // Mendapatkan data dari intent
        userId = intent.getIntExtra("user_id", 0)
        lokasi = intent.getStringExtra("lokasi").toString()
        videoPath = intent.getStringExtra("videoPath").toString()
        val koordinat = intent.getStringExtra("koordinat").toString()

        lokasiTv.text = "Lokasi anda: ${lokasi} (${koordinat}) "

        // Initialize views
        backBtn = findViewById(R.id.back)
        backBtn.setOnClickListener(this)

        // Text pratinjau video bisa diklik
        pratinjau = findViewById(R.id.pratinjau)
        pratinjau.setOnClickListener(this)

        // Setup location provider
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        deskripsi = findViewById(R.id.deskripsi)
        pg = findViewById(R.id.progressBar)
        bg = findViewById(R.id.overlayLayout)
        kirimBtn = findViewById(R.id.kirim)
        kirimBtn.setOnClickListener {
            cekInputan()
        }

        tampilkanDropdown()
        tampilkanDropdownKejahatan()

        getLocation()

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                tampilkanDialog()
            }
        })
    }

    private fun cekInputan() {
        if (jenisLaporan == "" || namaKejahatan == "" || deskripsi.text.isEmpty() || videoPath == ""
            || lokasi == "" || userId == 0) {
            Toast.makeText(this, "Data tidak boleh kosong!", Toast.LENGTH_SHORT).show()
        } else if (latitude == 0.0 || longitude == 0.0) {
            getLocation()
        } else {
            val waktu = getFormattedDateTime()
            lapor(userId, jenisLaporan, namaKejahatan, deskripsi.text.toString(), "Diajukan", waktu, latitude, longitude, lokasi, videoPath)
        }
    }

    private fun lapor(
        uId: Int,
        j_l: String,
        n_k: String,
        deskripsi: String,
        status: String,
        waktu: String,
        lat: Double,
        longi: Double,
        tempat: String?,
        realPath: String,
    ) {
        showLoading(true)

        // Membuat RequestBody dari string
        val userId = RequestBody.create(MultipartBody.FORM, uId.toString())
        val jenisLaporan = RequestBody.create(MultipartBody.FORM, j_l)
        val namaKejahatan = RequestBody.create(MultipartBody.FORM, n_k)
        val deskripsi = RequestBody.create(MultipartBody.FORM, deskripsi)
        val status = RequestBody.create(MultipartBody.FORM, status)
        val waktu = RequestBody.create(MultipartBody.FORM, waktu)
        val latitude = RequestBody.create(MultipartBody.FORM, lat.toString())
        val longitude = RequestBody.create(MultipartBody.FORM, longi.toString())
        val tempat = tempat?.let { RequestBody.create(MultipartBody.FORM, it) }
        val realPathBody = realPath.let { RequestBody.create(MultipartBody.FORM, it) }

        val file = File(realPath)
        val requestFile = file.asRequestBody("video/mp4".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("video", file.name, requestFile)

        // Memanggil API
        val client = ApiConfig.getApiService().submitLaporanWithVideo(
            userId, jenisLaporan, namaKejahatan, deskripsi, status, waktu, latitude, longitude, tempat, realPathBody, body
        )
        client.enqueue(object : Callback<LaporanVideoResponse> {
            override fun onResponse(call: Call<LaporanVideoResponse>, response: Response<LaporanVideoResponse>) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Toast.makeText(this@LaporanActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                    Log.d("laporanUploaded", responseBody.filePath)
                    finish()
                } else {
                    Log.e("ERROR", "onFailure: ${response.message()}")
                    Toast.makeText(this@LaporanActivity, "Gagal di respon true", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LaporanVideoResponse>, t: Throwable) {
                showLoading(false)
                Log.e("ERROR", "onFailure: ${t.message}")
                Toast.makeText(this@LaporanActivity, "ERROR respon: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun tampilkanDropdown() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.jenisLaporan)
        val textInputLayout: TextInputLayout = findViewById(R.id.textInputLayout)
        adapterItems = ArrayAdapter(this, R.layout.list_item, arrayJenisLaporan)
        autoCompleteTextView.setAdapter(adapterItems)

        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                jenisLaporan = parent.getItemAtPosition(position).toString()
                textInputLayout.hint = null
            }
    }

    private fun tampilkanDropdownKejahatan() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.namaKejahatan)
        val textInputLayout: TextInputLayout = findViewById(R.id.textInputLayout2)
        adapterItems = ArrayAdapter(this, R.layout.list_item, arrayNamaKejahatan)
        autoCompleteTextView.setAdapter(adapterItems)

        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                namaKejahatan = parent.getItemAtPosition(position).toString()
                textInputLayout.hint = null
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> tampilkanDialog()
            R.id.pratinjau -> {
                val intent = Intent(this, PratinjauVideoActivity::class.java)
                intent.putExtra("videoPath", videoPath)
                intent.putExtra("lokasi",lokasi)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
        }
    }

    private fun tampilkanDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Hapus Laporan ini")
            .setMessage("Apakah Anda yakin ingin membatalkan laporan?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ -> finish() }
            .setNegativeButton(android.R.string.no, null)
            .create()
        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.yellow))
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.opacitiy_white))

        val window = dialog.window
        window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            pg.visibility = View.VISIBLE
            bg.visibility = View.VISIBLE
            val jenisLaporan: AutoCompleteTextView = findViewById(R.id.jenisLaporan)
            jenisLaporan.setOnTouchListener { _, _ -> true }

            val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.namaKejahatan)
            autoCompleteTextView.setOnTouchListener { _, _ -> true }

            kirimBtn.isClickable = false
            deskripsi.isFocusable = false
            deskripsi.isFocusableInTouchMode = false
            pratinjau.isClickable = false
        } else {
            pg.visibility = View.GONE
            bg.visibility = View.GONE
            tampilkanDropdown()
            tampilkanDropdownKejahatan()
            kirimBtn.isClickable = true
            deskripsi.isFocusable = true
            deskripsi.isFocusableInTouchMode = true
            pratinjau.isClickable = false
        }
    }

    private fun getFormattedDateTime(): String {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val day = dayFormat.format(calendar.time)
        val time = timeFormat.format(calendar.time)
        return "$day ($time WIB)"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10) {
            val fineLocationGranted = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
            val coarseLocationGranted = grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED

            if (fineLocationGranted || coarseLocationGranted) {
                getLocation()
            } else {
                Toast.makeText(this, "Izin lokasi tidak diaktifkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 10
            )
        } else {
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
