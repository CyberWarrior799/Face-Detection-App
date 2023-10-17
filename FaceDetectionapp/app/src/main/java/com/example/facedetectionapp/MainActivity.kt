package com.example.facedetectionapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.mlkit.vision.face.Face
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.facedetectionapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.flow.combineTransform
import java.security.Permission

class MainActivity : AppCompatActivity() {
    val permission = android.Manifest.permission.CAMERA
    lateinit var bind : ActivityMainBinding
    private  var IMAGE_CAPTURE_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.btn.setOnClickListener{
            checkCameraPermissionAndOpenCamera()
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED)
        { ActivityCompat.requestPermissions( this, arrayOf(permission), IMAGE_CAPTURE_CODE)

        }
        else{
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == IMAGE_CAPTURE_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, open the camera.
                openCamera()
            } else {
                // Camera permission denied, show a message or handle as needed.
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result, e.g., get the image from the data Intent
                val data: Intent? = result.data
                // Process the data here
                val bitmap=data?.extras?.get("data") as? Bitmap
                bind.faceImg.setImageBitmap(bitmap)
                if (bitmap != null) {
                    detectFace(bitmap)
                }
            } else {
                // Handle the case where the user canceled the action
            }
        }


    private fun openCamera() {
        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager)!=null) {
            takePictureLauncher.launch(intent)
        }
        else{   Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show() }}


    private fun detectFace(bitmap: Bitmap) {
        // High-accuracy landmark detection and face classification
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f) // Optional: Set the minimum face size
            .enableTracking() // Optional: Enable face tracking for age and gender
            .build()

        val detector = FaceDetection.getClient(highAccuracyOpts)
// Or, to use the default option:
// val detector = FaceDetection.getClient();
        val image = InputImage.fromBitmap(bitmap, 0)

        val result = detector.process(image)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                var result=""
                var i=1

                if (faces.isEmpty())
                {
                    Toast.makeText(this,"No face detected",Toast.LENGTH_SHORT).show()
                }
                else
                {
                    for (face in faces) {

                        result=" Face number: $i" +
                                "\nSmile: ${face.smilingProbability?.times(100)}%" +
                                "\nleft eye: ${face.leftEyeOpenProbability?.times(100)}%" +
                                "\nright eye: ${face.rightEyeOpenProbability?.times(100)}%"

                        i++  }

                            bind.imgInfo.text=result  }  }

            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(this,"Image Detection Failed",Toast.LENGTH_SHORT).show()        } }


   }

