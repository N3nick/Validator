package com.google.mlkit.codelab.translate.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.mlkit.codelab.translate.R


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        private const val TAG = "MainFragment"
    }

    private lateinit var captureImageButton: Button
    private lateinit var detectTextButton: Button
    private lateinit var detectTv: TextView
    private lateinit var captureImageView: ImageView
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageBitmap: Bitmap
    private lateinit var container: ViewGroup


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view as RelativeLayout
        captureImageButton = container.findViewById(R.id.take_imageButton)
        detectTextButton = container.findViewById(R.id.display_text_button)
        detectTv = container.findViewById(R.id.text_display)
        captureImageView = container.findViewById(R.id.image_view)

        captureImageButton.setOnClickListener {
            //for capturing the image
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Log.d(TAG, "Error: " + e.localizedMessage)
            }
        }

        detectTextButton.setOnClickListener {
            detectTextFromImage()
        }
    }

    private fun detectTextFromImage() {
        FirebaseApp.initializeApp(requireContext())
        val image = FirebaseVisionImage.fromBitmap(imageBitmap)
       // val detector = FirebaseApp.initializeApp(requireContext())?.let { FirebaseVision.getInstance(it).onDeviceTextRecognizer }
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(image).addOnSuccessListener {
            displayDetectedText(it)

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error :" + it.message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun displayDetectedText(result: FirebaseVisionText?) {
        val resultText = result!!.text
        if (result.textBlocks.size == 0) {
            Toast.makeText(requireContext(), "No text was detected", Toast.LENGTH_SHORT).show()
        } else {
            for (block in result.textBlocks) {
                val blockText = block.text
                detectTv.text = blockText
                /*val blockConfidence = block.confidence
                val blockLanguages = block.recognizedLanguages
                val blockCornerPoints = block.cornerPoints
                val blockFrame = block.boundingBox
                for (line in block.lines){
                    val lineText = line.text
                    val lineConfidence = line.confidence
                    val lineLanguages = line.recognizedLanguages
                    val lineCornerPoints = line.cornerPoints
                    val  lineFrame = line.boundingBox
                    for (element in line.elements){
                        val elementText = element.text
                        val elementConfidence = element.confidence
                        val elementLanguages = element.recognizedLanguages
                        val elementCornerPoints = element.cornerPoints
                        val elementFrame = element.boundingBox
                    }
                }*/
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            captureImageView.setImageBitmap(imageBitmap)
        }
    }


}
