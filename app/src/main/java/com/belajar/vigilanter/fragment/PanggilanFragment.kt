package com.belajar.vigilanter.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.belajar.vigilanter.R

class PanggilanFragment : Fragment(), View.OnClickListener {
    lateinit var card_112: CardView
    lateinit var card_110: CardView
    lateinit var card_118: CardView
    lateinit var card_113: CardView
    lateinit var backBtn: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_panggilan, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        card_112 = view.findViewById(R.id.card_112)
        card_110 = view.findViewById(R.id.card_110)
        card_118 = view.findViewById(R.id.card_118)
        card_113 = view.findViewById(R.id.card_113)
        backBtn = view.findViewById(R.id.back)

        card_112.setOnClickListener(this)
        card_110.setOnClickListener(this)
        card_118.setOnClickListener(this)
        card_113.setOnClickListener(this)
        backBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.card_112 -> {
                val phoneNumber = "112"
                val dialPhoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialPhoneIntent)
            }
            R.id.card_110 -> {
                val phoneNumber = "110"
                val dialPhoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialPhoneIntent)
            }
            R.id.card_118 -> {
                val phoneNumber = "118"
                val dialPhoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialPhoneIntent)
            }
            R.id.card_113 -> {
                val phoneNumber = "113"
                val dialPhoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialPhoneIntent)
            }
            R.id.back -> {
                parentFragmentManager.popBackStack()
            }
        }
    }



}