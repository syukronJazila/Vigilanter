package com.belajar.vigilanter

import com.belajar.vigilanter.R
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.resolutionselector.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.belajar.vigilanter.camerausingcamerax.appSettingOpen
import com.belajar.vigilanter.camerausingcamerax.gone
import com.belajar.vigilanter.camerausingcamerax.visible
import com.belajar.vigilanter.camerausingcamerax.warningPermissionDialog
import com.belajar.vigilanter.data.response.videoResponse
import com.belajar.vigilanter.data.retrofit.ApiConfig
import com.belajar.vigilanter.data.retrofit.ApiService
import com.belajar.vigilanter.databinding.ActivityCameraBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Callback

class CameraActivity : AppCompatActivity() {

    private val mainBinding: ActivityCameraBinding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private lateinit var videoCapture: VideoCapture<Recorder>
    private var recording: Recording? = null

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private var orientationEventListener: OrientationEventListener? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_16_9

    private var userId: Int = 0
    private var road: String = "null"
    private var koordinat: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        userId = intent.getIntExtra("user_id", 0)
        road = intent.getStringExtra("lokasi").toString()
        koordinat = intent.getStringExtra("koordinat").toString()


        if (checkMultiplePermission()) {
            startCamera()
        }

        mainBinding.flipCameraIB.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUserCases()
        }
        mainBinding.aspectRatioTxt.setOnClickListener {
            if (aspectRatio == AspectRatio.RATIO_16_9) {
                aspectRatio = AspectRatio.RATIO_4_3
                setAspectRatio("H,4:3")
                mainBinding.aspectRatioTxt.text = "4:3"
            } else {
                aspectRatio = AspectRatio.RATIO_16_9
                setAspectRatio("H,0:0")
                mainBinding.aspectRatioTxt.text = "16:9"
            }
            bindCameraUserCases()
        }

        mainBinding.captureIB.setImageResource(R.drawable.ic_start)

        mainBinding.captureIB.setOnClickListener {
            captureVideo()
        }
        mainBinding.flashToggleIB.setOnClickListener {
            setFlashIcon(camera)
        }
    }


    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(this))
    }


    private fun bindCameraUserCases() {
        val previewView = mainBinding.previewView
//        val display = previewView.display ?: return // Cek null
//
//        val rotation = display.rotation

        val rotation = mainBinding.previewView.display?.rotation ?: Surface.ROTATION_0

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.SD,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            )
            .setAspectRatio(aspectRatio)
            .build()

        videoCapture = VideoCapture.withOutput(recorder).apply {
            targetRotation = rotation
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val myRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture.targetRotation = myRotation
                videoCapture.targetRotation = myRotation
            }
        }
        orientationEventListener?.enable()

        try {
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, videoCapture
            )
            setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setUpZoomTapToFocus(){
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener(){
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio  ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this,listener)

        mainBinding.previewView.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN){
                val factory = mainBinding.previewView.meteringPointFactory
                val point = factory.createPoint(event.x,event.y)
                val action = FocusMeteringAction.Builder(point,FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2,TimeUnit.SECONDS)
                    .build()

                val x = event.x
                val y = event.y

                val focusCircle = RectF(x-50,y-50, x+50,y+50)

                mainBinding.focusCircleView.focusCircle = focusCircle
                mainBinding.focusCircleView.invalidate()

                camera.cameraControl.startFocusAndMetering(action)

                view.performClick()
            }
            true
        }
    }

    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                mainBinding.flashToggleIB.setImageResource(R.drawable.flash_on)
            } else {
                camera.cameraControl.enableTorch(false)
                mainBinding.flashToggleIB.setImageResource(R.drawable.flash_off)
            }
        } else {
            Toast.makeText(
                this,
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
            mainBinding.flashToggleIB.isEnabled = false
        }
    }

    private fun setAspectRatio(ratio: String) {
        mainBinding.previewView.layoutParams = mainBinding.previewView.layoutParams.apply {
            if (this is ConstraintLayout.LayoutParams) {
                dimensionRatio = ratio
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun captureVideo() {
        mainBinding.captureIB.isEnabled = false
        mainBinding.flashToggleIB.gone()
        mainBinding.flipCameraIB.gone()
        mainBinding.aspectRatioTxt.gone()

        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            stopRecording()
            recording = null
            return
        }

        startRecording()
        val fileName = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/CameraX")
                val message = MediaStore.Images.Media.RELATIVE_PATH + "Movies/CameraX"
//                Toast.makeText(this@CameraActivity, message, Toast.LENGTH_LONG).show()
                Log.d("berhasil", message)

            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        Log.d("tesURI", MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString())
        Log.d("tesURI", mediaStoreOutputOptions.toString())

        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (ActivityCompat.checkSelfPermission(this@CameraActivity, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        mainBinding.captureIB.setImageResource(R.drawable.ic_stop)
                        mainBinding.captureIB.isEnabled = true
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val uri = recordEvent.outputResults.outputUri
                            Log.d("tesURI2", "Video saved: ${uri.path}")

                            val realPath = getRealPathFromUri(this, uri)
                            Log.d("tesURI2", "Real Path: $realPath")

                            val intent = Intent(this, LaporanActivity::class.java)
                            intent.putExtra("user_id",userId )
                            intent.putExtra("lokasi", road )
                            intent.putExtra("videoPath", realPath)
                            intent.putExtra("koordinat", koordinat)
                            startActivity(intent)
                            finish()
                        } else {
                            recording?.close()
                            recording = null
                            Log.d("error", "Error: ${recordEvent.error}")
                        }
                        mainBinding.captureIB.setImageResource(R.drawable.ic_start)
                        mainBinding.captureIB.isEnabled = true

                        mainBinding.flashToggleIB.visible()
                        mainBinding.flipCameraIB.visible()
                        mainBinding.aspectRatioTxt.visible()
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        if (recording != null){
            recording?.stop()
            captureVideo()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimer = object : Runnable{
        override fun run() {
            val currentTime = SystemClock.elapsedRealtime() - mainBinding.recodingTimerC.base
            val timeString = currentTime.toFormattedTime()
            mainBinding.recodingTimerC.text = timeString
            handler.postDelayed(this,1000)
        }
    }

    private fun Long.toFormattedTime():String{
        val seconds = ((this / 1000) % 60).toInt()
        val minutes = ((this / (1000 * 60)) % 60).toInt()
        val hours = ((this / (1000 * 60 * 60)) % 24).toInt()

        return if (hours >0){
            String.format("%02d:%02d:%02d",hours,minutes,seconds)
        }else{
            String.format("%02d:%02d",minutes,seconds)
        }
    }

    private fun startRecording(){
        mainBinding.recodingTimerC.visible()
        mainBinding.recodingTimerC.base = SystemClock.elapsedRealtime()
        mainBinding.recodingTimerC.start()
        handler.post(updateTimer)
    }

    private fun stopRecording(){
        mainBinding.recodingTimerC.gone()
        mainBinding.recodingTimerC.stop()
        handler.removeCallbacks(updateTimer)

        recording?.stop()  // Move stop logic here if recording is ongoing
        recording = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider.unbindAll()
        orientationEventListener?.disable()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("LENS_FACING", lensFacing)
        outState.putInt("ASPECT_RATIO", aspectRatio)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        lensFacing = savedInstanceState.getInt("LENS_FACING", CameraSelector.LENS_FACING_BACK)
        aspectRatio = savedInstanceState.getInt("ASPECT_RATIO", AspectRatio.RATIO_16_9)
        bindCameraUserCases()
    }

}
