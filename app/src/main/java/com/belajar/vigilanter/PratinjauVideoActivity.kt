package com.belajar.vigilanter

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity


class PratinjauVideoActivity : AppCompatActivity() {
    private lateinit var back: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pratinjau_video)

        back = findViewById(R.id.back)
        val videoPath = intent.getStringExtra("videoPath")
        val lokasi = intent.getStringExtra("lokasi")
        val userId = intent.getIntExtra("user_id", 0)
        playVideo(videoPath.toString())

        back.setOnClickListener {
            onBackPressed()
        }

    }

    private fun playVideo(videoUrl: String) {
        val videoView = findViewById<VideoView>(R.id.videoView)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(Uri.parse(videoUrl))
        videoView.requestFocus()
        videoView.start()
    }

    override fun onBackPressed() {
        val videoView = findViewById<VideoView>(R.id.videoView)

        if (videoView.isPlaying) {
            videoView.stopPlayback()
        }

        // Panggil metode onBackPressed() superclass untuk melanjutkan aksi back seperti biasa
        super.onBackPressed()
    }
}