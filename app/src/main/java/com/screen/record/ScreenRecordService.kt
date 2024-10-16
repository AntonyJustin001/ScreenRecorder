package com.screen.record

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.screen.record.utils.prefs

class ScreenRecordService : Service() {
    companion object {
        const val CHANNEL_ID = "ScreenRecordChannel"
        const val NOTIFICATION_ID = 1
    }

    private var mediaProjection: MediaProjection? = null
    private lateinit var mediaRecorder: MediaRecorder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = intent?.getParcelableExtra<Intent>("data")

        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
        }

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)

        mediaProjection?.let {
            startScreenRecording(it)
        }

        Log.e("Test","ScreenRecordService - onStartCommand")

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Screen Record Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Recording")
            .setContentText("Screen recording is active.")
            .setSmallIcon(R.drawable.app_logo)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaProjection?.stop()
    }

    private fun startScreenRecording(mediaProjection: MediaProjection) {
        // Step 1: Initialize MediaRecorder
        val mediaRecorder = MediaRecorder()

        Log.e("Test","ScreenRecordService - startScreenRecording")

        mediaRecorder.apply {
            //Log.e("Test","ScreenRecordService - mediaRecorder.apply")
            // Set the audio source to capture microphone input
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            // Set the video source to capture from a surface (which will be provided by MediaProjection)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            // Set the output format to MP4
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            // Set the output file location
            setOutputFile(getExternalFilesDir(null)?.absolutePath + "/ScreenRecord.mp4")
            // Set the video size (width and height)
            setVideoSize(1280, 720)
            // Set the video encoder
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            // Set the audio encoder
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            // Set the bit rate for video encoding
            setVideoEncodingBitRate(3 * 1000 * 1000) // 1 Mbps
            // Set the frame rate
            setVideoFrameRate(30)
            // Prepare the MediaRecorder
            prepare()
        }
        // Step 2: Create a Surface from the MediaRecorder
        val surface = mediaRecorder.surface
        // Step 3: Create a Virtual Display for screen capturing
        val virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenRecord",
            1280, 720, resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface, null, null
        )
        // Step 4: Start the MediaRecorder
        mediaRecorder.start()
        // Store references to the MediaRecorder and VirtualDisplay if you need to stop them later
        this.mediaRecorder = mediaRecorder
        //this.virtualDisplay = virtualDisplay
    }


    private fun stopScreenRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaProjection?.stop()
    }
}