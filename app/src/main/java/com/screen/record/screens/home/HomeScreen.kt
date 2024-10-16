package com.screen.record.screens.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.screen.record.R
import com.screen.record.ScreenRecordService
import com.screen.record.utils.prefs
import tena.health.care.models.RecordedVideo
import java.io.File

class HomeScreen : Fragment() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    var btnStart: CardView? = null
    var btnStop: CardView? = null
    var btnUpload: CardView? = null
    var btnLogOut: CardView? = null
    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnStart = view.findViewById(R.id.btnStart)
        btnStart?.setOnClickListener {
            mediaProjectionManager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE)
        }

        btnStop = view.findViewById(R.id.btnStop)
        btnStop?.setOnClickListener {
            context?.stopService(Intent(context, ScreenRecordService::class.java))
        }

        btnUpload = view.findViewById(R.id.btnUpload)
        btnUpload?.setOnClickListener {
            uploadVideoToFirebase("/storage/emulated/0/Android/data/com.screen.record/files/ScreenRecord.mp4")
        }

        btnLogOut = view.findViewById(R.id.btnLogOut)
        btnLogOut?.setOnClickListener {

        }

        progressBar = view.findViewById(R.id.circularProgressBar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 100)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE && resultCode == RESULT_OK) {

            val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            //context?.startService(serviceIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }
        }
    }

    private fun uploadVideoToFirebase(videopath: String) {
        progressBar.visibility = View.VISIBLE
        val videoUri = Uri.fromFile(File(videopath))
        val storageReference = FirebaseStorage.getInstance().reference
        val fileName = "ScreenRecord${System.currentTimeMillis()}.mp4"
        val fileReference = storageReference.child("recordedVideos/$fileName")
        //prefs.put("VideoName","$fileName")
        val uploadTask = fileReference.putFile(videoUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                storeRecordedVideo(RecordedVideo(
                    "$fileName",
                    "6A Section",
                    "English",
                    "${System.currentTimeMillis()}",
                    uri.toString(),
                    "Teacher1"
                ))
            }
        }.addOnFailureListener {
            // Handle failed upload
            Snackbar.make(requireView(), "Error uploading video!", Snackbar.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
        }.addOnProgressListener { taskSnapshot ->
            // You can display the progress of the upload here
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            Log.d("Firebase Storage", "Upload is $progress% done")
        }
    }

    fun storeRecordedVideo(recordedVideo: RecordedVideo) {
        val db = FirebaseFirestore.getInstance()
        db.collection("videos")
            .document(recordedVideo.videoName)
            .set(recordedVideo)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Video Uploaded Successfully!", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                Snackbar.make(requireView(), "Error uploading video!", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }

    companion object {
        const val REQUEST_CODE_SCREEN_CAPTURE = 1001
        const val DISPLAY_WIDTH = 1080
        const val DISPLAY_HEIGHT = 1920
    }
}