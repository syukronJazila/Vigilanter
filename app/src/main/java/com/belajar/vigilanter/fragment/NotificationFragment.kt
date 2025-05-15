package com.belajar.vigilanter.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belajar.vigilanter.R
import com.belajar.vigilanter.adapter.Notifikasi
import com.belajar.vigilanter.adapter.NotifikasiAdapter

class NotificationFragment : Fragment(), View.OnClickListener {
    private lateinit var notifRc: RecyclerView
    private val listNotif = ArrayList<Notifikasi>()
    private lateinit var backBtn: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backBtn = view.findViewById(R.id.back)
        backBtn.setOnClickListener(this)

        notifRc = view.findViewById(R.id.notifList)
        listNotif.add(Notifikasi("Terjadi Begal di sekotar Anda", "Hati-hati ketika melewati Simpang Tiga Kampus USU. Tekan notifikasi untuk melihat detail dan tindakan pencegahan.", "21/6/2024 (10:01:10 WIB)"))
        listNotif.add(Notifikasi("Terjadi kejahatan disekita Anda", "Hati-hati ketika melewati jalan gak tau, Surabaya. Tekan notifikasi untuk melihat detail dan tindakan pencegahan.", "18/8/2024 (02:00:11 WIB)"))

        notifRc.layoutManager = LinearLayoutManager(requireActivity())
        val NotifikasiAdapter = NotifikasiAdapter(listNotif)
        notifRc.adapter = NotifikasiAdapter
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back -> {
                parentFragmentManager.popBackStack()
            }
        }
    }

}