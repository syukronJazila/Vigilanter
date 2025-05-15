package com.belajar.vigilanter

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.ui.PlayerView

class VideoLaporanActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    var context: Context = this
    var defaultRequestProperties: Map<String, String> = HashMap()
    var video_url: String = ""
    var userAgent: String = ""
    private var trackSelector: DefaultTrackSelector? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE) //load in fullscreen
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) // load and hide status bar at top
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContentView(R.layout.activity_video_laporan)
        video_url = intent.getStringExtra("video_url").toString()

        playerView = findViewById(R.id.PlayerView)
        val httpDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setKeepPostFor302Redirects(true)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
            .setDefaultRequestProperties(defaultRequestProperties)
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaItem = MediaItem.Builder()
            .setUri(video_url)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()

        val progressiveMediaSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        InitlizeWithExo(progressiveMediaSource)
    }

    @OptIn(UnstableApi::class)
    private fun InitlizeWithExo(progressiveMediaSource: MediaSource) {
        val videoselector: ExoTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(this, videoselector)
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector!!)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
        playerView.player = exoPlayer
        playerView.keepScreenOn = true // keep screen on == consume user battery;
        exoPlayer.setMediaSource(progressiveMediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        exoPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.play()
    }
}