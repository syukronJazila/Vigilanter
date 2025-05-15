package com.belajar.vigilanter.adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.belajar.vigilanter.R

data class Riwayat(
    val namaKejahatan: String,
    val waktu: String,
    val jenisLaporan: String,
    val deskripsi: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val lokasi: String,
    val id: Int,
    val video_url: String,
)

class RiwayatAdapter(private val listRiwayat: ArrayList<Riwayat>) : RecyclerView.Adapter<RiwayatAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback
    private lateinit var onBatalClickCallback: OnBatalClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    fun setOnBatalClickCallback(onBatalClickCallback: OnBatalClickCallback) {
        this.onBatalClickCallback = onBatalClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.riwayat_terkirim_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val (namaKejahatan, waktu, jenisLaporan, deskripsi, status, latitude, longitude, lokasi) = listRiwayat[position]
        holder.judul.text = namaKejahatan
        holder.lokasi.text = lokasi
        holder.waktu.text = waktu


        holder.batalBtn.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onBatalClickCallback.onBatalClickCallback(listRiwayat[position])
            }
        }

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION && position < listRiwayat.size) {
                onItemClickCallback.onItemClicked(listRiwayat[position])
            }
        }
    }

    override fun getItemCount(): Int = listRiwayat.size

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.kejahatan)
        val lokasi: TextView = itemView.findViewById(R.id.lokasi)
        val waktu: TextView = itemView.findViewById(R.id.waktu)
        val batalBtn: AppCompatButton = itemView.findViewById(R.id.batalBtn)
        val cardView: CardView = itemView.findViewById(R.id.mainCard)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Riwayat)
    }

    interface OnBatalClickCallback {
        fun onBatalClickCallback(data: Riwayat)
    }
}