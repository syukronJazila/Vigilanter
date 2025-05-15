package com.belajar.vigilanter.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.belajar.vigilanter.R

data class RiwayatSelesai(
    val namaKejahatan: String,
    val waktu: String,
    val jenisLaporan: String,
    val deskripsi: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val lokasi: String,
    val id : Int,
    val video_url: String
)

class RiwayatSelesaiAdapter(private val listRiwayatSelesai: ArrayList<RiwayatSelesai>) : RecyclerView.Adapter<RiwayatSelesaiAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback
    private lateinit var onDeleteClickCallback: OnDeleteClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    fun setOnDeleteClickCallback(onDeleteClickCallback: OnDeleteClickCallback) {
        this.onDeleteClickCallback = onDeleteClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.riwayat_selesai_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (namaKejahatan, waktu, jenisLaporan, deskripsi, status, latitude, longitude, lokasi) = listRiwayatSelesai[position]
        holder.judul.text = namaKejahatan
        holder.lokasi.text = lokasi
        holder.waktu.text = waktu
        if(status == "Diterima"){
            holder.statusTv.text = "Diterima"
            holder.statusTv.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }else if (status == "Ditolak"){
            holder.statusTv.text = "Ditolak"
            holder.statusTv.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        }else{
            holder.statusTv.text = "Dibatalkan"
            holder.statusTv.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white_25op))
        }

        holder.hapusBtn.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onDeleteClickCallback.onDeleteClicked(listRiwayatSelesai[position])
            }
        }

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION && position < listRiwayatSelesai.size) {
                onItemClickCallback.onItemClicked(listRiwayatSelesai[position])
            }
        }

    }


    override fun getItemCount(): Int = listRiwayatSelesai.size

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.kejahatan)
        val lokasi: TextView = itemView.findViewById(R.id.lokasi)
        val waktu: TextView = itemView.findViewById(R.id.waktu)
        val statusTv: TextView= itemView.findViewById(R.id.status)
        val hapusBtn: AppCompatButton= itemView.findViewById(R.id.hapusBtn)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: RiwayatSelesai)
    }

    interface OnDeleteClickCallback {
        fun onDeleteClicked(data: RiwayatSelesai)
    }
}