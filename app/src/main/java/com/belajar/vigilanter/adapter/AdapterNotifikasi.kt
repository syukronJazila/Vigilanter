package com.belajar.vigilanter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.belajar.vigilanter.R

data class Notifikasi(
    val judul: String,
    val pesan: String,
    val lokasi: String
)

class NotifikasiAdapter(private val listHero: ArrayList<Notifikasi>) : RecyclerView.Adapter<NotifikasiAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.notification_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (judul, pesan, lokasi) = listHero[position]
        holder.judul.text = judul
        holder.pesan.text = pesan
        holder.waktu.text = lokasi
        holder.closeBtn.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Implementasi Hapus Notif", Toast.LENGTH_SHORT).show()
        }
//        holder.itemView.setOnClickListener {
//            onItemClickCallback.onItemClicked(listHero[holder.adapterPosition])
//        }

    }

    override fun getItemCount(): Int = listHero.size

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.judul)
        val pesan: TextView = itemView.findViewById(R.id.pesan)
        val waktu: TextView = itemView.findViewById(R.id.waktu)
        val closeBtn: ImageView = itemView.findViewById(R.id.close)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Riwayat)
    }
}
