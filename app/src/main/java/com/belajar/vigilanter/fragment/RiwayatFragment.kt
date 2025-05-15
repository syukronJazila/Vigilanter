package com.belajar.vigilanter.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.belajar.vigilanter.DetailRiwayatActivity
import com.belajar.vigilanter.MainActivity
import com.belajar.vigilanter.R
import com.belajar.vigilanter.adapter.Riwayat
import com.belajar.vigilanter.adapter.RiwayatAdapter
import com.belajar.vigilanter.adapter.RiwayatSelesai
import com.belajar.vigilanter.adapter.RiwayatSelesaiAdapter
import com.belajar.vigilanter.data.response.GetLaporResponse
import com.belajar.vigilanter.data.response.HapusLaporResponse
import com.belajar.vigilanter.data.response.Laporan
import com.belajar.vigilanter.data.response.LoginResponse
import com.belajar.vigilanter.data.retrofit.ApiConfig
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatFragment : Fragment(), View.OnClickListener {
    lateinit var sortKirim: ImageView
    private lateinit var riwayatRc: RecyclerView
    private lateinit var riwayatRcS: RecyclerView
    private val list = ArrayList<Riwayat>()
    private val listSelesai = ArrayList<RiwayatSelesai>()
    private lateinit var pg: ProgressBar
    private lateinit var bg: FrameLayout
    private lateinit var noDataDiajukan: TextView
    private lateinit var noDataSelesai: TextView
    private var user_id: Int = 0
    private lateinit var RiwayatSelesaiAdapter: RiwayatSelesaiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riwayat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortKirim = view.findViewById(R.id.sort_kirim)
        sortKirim.setOnClickListener(this)

        pg = view.findViewById(R.id.progressBar)
        bg = view.findViewById(R.id.overlayLayout)
        noDataDiajukan = view.findViewById(R.id.noDataDiajukan)
        noDataSelesai = view.findViewById(R.id.noDataSelesai)

        // Setup Recycle View Riwayat Terkirim
        riwayatRc = view.findViewById(R.id.kirimList)
        riwayatRc.isNestedScrollingEnabled = false
        riwayatRc.setHasFixedSize(false)

        // Setup Recycle View Riwayat Selesai
        riwayatRcS = view.findViewById(R.id.selesaiList)
        riwayatRcS.setHasFixedSize(false)

        // mengambil data
        arguments?.let { bundle ->
            user_id = bundle.getInt("user_id")
        }

        ambilLaporan(user_id)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.sort_kirim -> {
            }
            R.id.back -> {
                parentFragmentManager.popBackStack()
            }
        }
    }

    fun setRecyclerViewHeight(listRiwayat: ArrayList<Riwayat>, recyclerView: RecyclerView) {
        // Fungsi untuk menetapkan tinggi recycle view riwayat terkirim
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = 275 * (listRiwayat.size)
        recyclerView.layoutParams = layoutParams
    }

    fun fragmentDetail(
        n_k: String, deskripsi : String, status : String, waktu: String, lat:Double,
        longi: Double, tempat: String, j_l:String, id: Int, video_url: String
    ){
        list.clear()
        listSelesai.clear()

        val intent = Intent(requireActivity(), DetailRiwayatActivity::class.java)
        intent.putExtra("nama_kejahatan",n_k )
        intent.putExtra("deskripsi", deskripsi)
        intent.putExtra("waktu", waktu)
        intent.putExtra("tempat", tempat)
        intent.putExtra("koordinat", "${lat}, ${longi}")
        intent.putExtra("jenis", j_l)
        intent.putExtra("status", status)
        intent.putExtra("id", id)
        intent.putExtra("video_url", video_url)
        startActivity(intent)
    }


    private fun showLoading(isLoading: Boolean) {
        val navView = requireActivity().findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        if (isLoading) {
            pg.visibility = View.VISIBLE
            bg.visibility = View.VISIBLE
            navView.isClickable = false
        } else {
            pg.visibility = View.GONE
            bg.visibility = View.GONE
            navView.isClickable = true
            navView.show(1)
        }
    }

    private fun ambilLaporan(user_id: Int) {
        showLoading(true)
        val client = ApiConfig.getApiService().getLaporan(user_id)
        client.enqueue(object : Callback<GetLaporResponse> {
            override fun onResponse(
                call: Call<GetLaporResponse>,
                response: Response<GetLaporResponse>
            ) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    setDataDiajukan(responseBody.diajukan)
                    setDataSelesai(responseBody.selesai)
                } else {
                    Log.e("ERROR", "onFailure setelah berhasil: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<GetLaporResponse>, t: Throwable) {
                showLoading(false)
                Log.e("ERROR", "onFailure: ${t.message}")
                Toast.makeText(requireActivity(), "ERROR respon: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataSelesai(selesai: List<Laporan>) {
        if(!selesai.isEmpty()){
            noDataSelesai.visibility = View.GONE
            listSelesai.clear()
            for (laporan in selesai){
                listSelesai.add(RiwayatSelesai(
                    namaKejahatan = laporan.nama_kejahatan,
                    waktu = laporan.waktu,
                    jenisLaporan = laporan.jenis_laporan,
                    deskripsi = laporan.deskripsi,
                    status = laporan.status,
                    latitude = laporan.latitude,
                    longitude = laporan.longitude,
                    lokasi = laporan.tempat,
                    id = laporan.id,
                    video_url = laporan.video_url
                ))
            }

            riwayatRcS.layoutManager = LinearLayoutManager(requireActivity())
            RiwayatSelesaiAdapter = RiwayatSelesaiAdapter(listSelesai)
            riwayatRcS.adapter = RiwayatSelesaiAdapter

            // Notify data change if necessary
            RiwayatSelesaiAdapter.notifyDataSetChanged()

            RiwayatSelesaiAdapter.setOnItemClickCallback(object: RiwayatSelesaiAdapter.OnItemClickCallback{
                override fun onItemClicked(data: RiwayatSelesai) {
                    fragmentDetail(data.namaKejahatan, data.deskripsi, data.status, data.waktu, data.latitude,
                        data.longitude, data.lokasi, data.jenisLaporan, data.id, data.video_url)
                }
            })

            RiwayatSelesaiAdapter.setOnDeleteClickCallback(object : RiwayatSelesaiAdapter.OnDeleteClickCallback {
                override fun onDeleteClicked(data: RiwayatSelesai) {
                    tampilkanDialog(true, data.id, "hapus")
                }
            })

        }else{
            riwayatRcS.visibility = View.GONE
        }
    }

    private fun setDataDiajukan(responseBody: List<Laporan>) {
        if(!responseBody.isEmpty()){
            noDataDiajukan.visibility = View.GONE

            list.clear()
            for (laporan in responseBody){
                list.add(Riwayat(
                    namaKejahatan = laporan.nama_kejahatan,
                    waktu = laporan.waktu,
                    jenisLaporan = laporan.jenis_laporan,
                    deskripsi = laporan.deskripsi,
                    status = laporan.status,
                    latitude = laporan.latitude,
                    longitude = laporan.longitude,
                    lokasi = laporan.tempat,
                    id = laporan.id,
                    video_url = laporan.video_url
                ))
            }

            riwayatRc.layoutManager = LinearLayoutManager(requireActivity())
            riwayatRc.isNestedScrollingEnabled = false
            val riwayatAdapter = RiwayatAdapter(list)
            riwayatRc.adapter = riwayatAdapter
            setRecyclerViewHeight(list,riwayatRc)

            // Notify data change if necessary
            riwayatAdapter.notifyDataSetChanged()

            riwayatAdapter.setOnItemClickCallback(object : RiwayatAdapter.OnItemClickCallback {
                override fun onItemClicked(data: Riwayat) {
                    fragmentDetail(data.namaKejahatan, data.deskripsi, data.status, data.waktu, data.latitude,
                        data.longitude, data.lokasi, data.jenisLaporan, data.id, data.video_url)
                }
            })

            riwayatAdapter.setOnBatalClickCallback(object : RiwayatAdapter.OnBatalClickCallback {
                override fun onBatalClickCallback(data: Riwayat) {
                    tampilkanDialog(false, data.id, "batal")
                }
            })

        }else{
            riwayatRc.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        ambilLaporan(user_id)
    }

    private fun hapusLaporan(id: Int, status: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().hapusLaporan(id, status)
        client.enqueue(object : Callback<HapusLaporResponse> {
            override fun onResponse(
                call: Call<HapusLaporResponse>,
                response: Response<HapusLaporResponse>
            ) {
                if(status == "hapus"){
                    showLoading(false)
                    // Cari index item yang ingin dihapus berdasarkan id
                    val index = listSelesai.indexOfFirst { it.id == id }
                    if (index != -1) {
                        listSelesai.removeAt(index)
                        // Beritahu adapter bahwa item telah dihapus
                        RiwayatSelesaiAdapter.notifyItemRemoved(index)
                    }
                }else{
                    // Buat Handler untuk thread utama
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        ambilLaporan(user_id)
                    }, 1000) // Delay dalam milidetik
                }

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Toast.makeText(requireActivity(), responseBody.message, Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ERROR", "onFailure setelah berhasil: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<HapusLaporResponse>, t: Throwable) {
                showLoading(false)
                Log.e("ERROR", "onFailure: ${t.message}")
                Toast.makeText(requireActivity(), "ERROR respon: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun tampilkanDialog(isHapus: Boolean, id: Int, status: String){
        var title = "Hapus Laporan ini"
        var message = "Apakah Anda yakin ingin menghapus laporan?"
        if(!isHapus){
            title = "Batalkan Laporan ini"
            message = "Apakah Anda yakin ingin membatalkan laporan?"
        }
        val dialog = AlertDialog.Builder(requireActivity())
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
        positiveButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.yellow))
        negativeButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.opacitiy_white))
        val window = dialog.window
        window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireActivity(), R.color.primary)))

    }

}
