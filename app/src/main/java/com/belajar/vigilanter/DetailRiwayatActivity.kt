package com.belajar.vigilanter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.belajar.vigilanter.data.response.HapusLaporResponse
import com.belajar.vigilanter.data.retrofit.ApiConfig
import com.belajar.vigilanter.thumbnail.ThumbnailExtractor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailRiwayatActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var editBtn: ImageView
    private lateinit var editTxt: EditText
    private lateinit var backBtn: ImageView
    private lateinit var judul: TextView
    private lateinit var lokasi: TextView
    private lateinit var koordinat: TextView
    private lateinit var jenis: TextView
    private lateinit var status: TextView
    private lateinit var waktu: TextView
    private lateinit var tanggal: TextView
    private lateinit var deskripsi: EditText
    private lateinit var hapusBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_riwayat)

        thumbnail(intent.getStringExtra("video_url").toString())

        editBtn = findViewById(R.id.edit)
        editTxt = findViewById(R.id.deskripsi)
        backBtn = findViewById(R.id.back)
        judul = findViewById(R.id.judul)
        lokasi = findViewById(R.id.lokasi)
        koordinat = findViewById(R.id.koordinat)
        jenis = findViewById(R.id.jenis)
        status = findViewById(R.id.status)
        waktu = findViewById(R.id.waktu)
        tanggal = findViewById(R.id.tanggal)
        deskripsi = findViewById(R.id.deskripsi)
        hapusBtn = findViewById(R.id.hapusBtn)

        editBtn.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        hapusBtn.setOnClickListener(this)

        // Ambil data dari Intent
        val intent = intent
        val waktuGabung = intent.getStringExtra("waktu")
        val regex = Regex("(.*) \\((.*)\\)")
        val matchResult = waktuGabung?.let { regex.find(it) }

        if (matchResult != null) {
            val (datePart, timePart) = matchResult.destructured
            waktu.text = datePart
            tanggal.text = timePart
        }

        judul.text = intent.getStringExtra("nama_kejahatan")
        deskripsi.setText(intent.getStringExtra("deskripsi"))
        koordinat.text = intent.getStringExtra("koordinat")
        lokasi.text = intent.getStringExtra("tempat")
        jenis.text = intent.getStringExtra("jenis")
        status.text = intent.getStringExtra("status")

        if(intent.getStringExtra("status") != "Diajukan"){
            hapusBtn.text = "Hapus Laporan"
            hapusBtn.setTextColor( ContextCompat.getColor(this, R.color.primary))
            hapusBtn.setBackgroundResource(R.drawable.button_hapus)
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.edit -> {
                if(editTxt.isFocusable){
                    editTxt.isFocusable = false
                    editTxt.isFocusableInTouchMode = false
                }else{
                    editTxt.isFocusable = true
                    editTxt.isFocusableInTouchMode = true
                    editTxt.requestFocus()
                }

            }
            R.id.back -> {
                onBackPressed()
            }
            R.id.hapusBtn -> {
                val id = intent.getIntExtra("id",0)
                var message = ""
                if (intent.getStringExtra("status") == "Diajukan"){
                    message = "batal"
                }else{
                    message = "hapus"
                }

                tampilkanDialog(message, id)
            }
        }
    }

    private fun tampilkanDialog(status: String, id: Int){
        var title = "Hapus Laporan ini"
        var message = "Apakah Anda yakin ingin menghapus laporan?"
        if(status == "batal"){
            title = "Batalkan Laporan ini"
            message = "Apakah Anda yakin ingin membatalkan laporan?"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                run {
                    hapusLaporan(id, status)
                }
            }
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

    private fun hapusLaporan(user_id: Int, status: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().hapusLaporan(user_id, status)
        client.enqueue(object : Callback<HapusLaporResponse> {
            override fun onResponse(
                call: Call<HapusLaporResponse>,
                response: Response<HapusLaporResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Toast.makeText(this@DetailRiwayatActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("ERROR", "onFailure setelah berhasil: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<HapusLaporResponse>, t: Throwable) {
                showLoading(false)
                Log.e("ERROR", "onFailure: ${t.message}")
                Toast.makeText(this@DetailRiwayatActivity, "ERROR respon: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        val pg: ProgressBar = findViewById(R.id.progressBar)
        val bg: FrameLayout = findViewById(R.id.overlayLayout)
        if (isLoading) {
            pg.visibility = View.VISIBLE
            bg.visibility = View.VISIBLE
        } else {
            pg.visibility = View.GONE
            bg.visibility = View.GONE
        }
    }

    private fun thumbnail(video_url: String){
        showLoading(true)
        ThumbnailExtractor.getThumbnail(video_url,
            object : ThumbnailExtractor.ThumbnailCallback {
                override fun onThumbnailReady(thumbnail: Bitmap?) {
                    thumbnail?.let {
                        showLoading(false)
                        val thumbnailImageView: ImageView = findViewById(R.id.thumbnail_image_view)
                        thumbnailImageView.setImageBitmap(it)

                        thumbnailImageView.setOnClickListener {
                            val intent = Intent(this@DetailRiwayatActivity, VideoLaporanActivity::class.java)
                            intent.putExtra("video_url", video_url)
                            startActivity(intent)
                        }
                    }
                }
            }
        )
    }
}