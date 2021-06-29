package com.google.mlkit.codelab.translate.main

import MySingleton
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.mlkit.codelab.translate.R
import com.google.mlkit.codelab.translate.util.DetectConnection
import com.google.mlkit.codelab.translate.util.Loading
import com.theartofdev.edmodo.cropper.CropImage
import com.thecode.aestheticdialogs.*
import org.json.JSONObject
import java.io.UnsupportedEncodingException


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
    val progressBar = Loading()
    var word = ""
    var uniqueID : String? = ""
    var bASE_URL : String? = ""

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>


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

        loadSharedPreferences()

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract){
            it?.let {
                imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), it)
                captureImageView.setImageURI(it)
                detectTextFromImage()
            }
        }

        captureImageButton.setOnClickListener {

            cropActivityResultLauncher.launch(null)


        }

        detectTextButton.setOnClickListener {
            //detectTextFromImage()
            verifyText()
        }
    }

    private fun loadSharedPreferences() {
        val sharedPreferences : SharedPreferences = requireActivity().getSharedPreferences(
            "sharedPrefs",
            Context.MODE_PRIVATE
        )
        bASE_URL = sharedPreferences.getString(
            "URL_KEY",
            "https://demo.ticketano.com/ticket-boarding"
        )
        uniqueID = sharedPreferences.getString("DEVICE_KEY", "1aac75011bf30e06fa9e06c973a28234")
    }

    private fun verifyText() {
        //check if any word has been detected
        if (!DetectConnection().isInternetAvailable(requireContext())) {
            //check for internet connection
            Toast.makeText(
                requireContext(),
                getString(R.string.check_connection),
                Toast.LENGTH_SHORT
            ).show()
        }else if (word.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No word has been detected, Please Ensure to take a clear Image",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            progressBar.startLoading(requireContext())
           /* val uniqueID = "1aac75011bf30e06fa9e06c973a28234"
            val bASE_URL = "https://demo.ticketano.com/ticket-boarding"*/
            val url = bASE_URL + "?device_id=" + uniqueID + "&ticket_number=" + word
            var prev = "null"

            if (prev != word) {
                // Request a string response from the provided URL.
                val stringRequest = StringRequest(
                    Request.Method.GET, url, {
                        progressBar.endLoading()
                        val responseResult = it.toString()
                        try {
                            val responseObject = JSONObject(responseResult)
                            AestheticDialog.Builder(
                                requireActivity(),
                                DialogStyle.FLAT,
                                DialogType.SUCCESS
                            ).apply {
                                setTitle("Success")
                                setMessage(responseObject.getString("message"))
                                setCancelable(false)
                                setDarkMode(true)
                                setGravity(Gravity.CENTER)
                                setAnimation(DialogAnimation.SHRINK)

                            }.setOnClickListener(object : OnDialogClickListener {
                                override fun onClick(dialog: AestheticDialog.Builder) {
                                    dialog.dismiss()
                                }
                            }).show()

                        } catch (e: Exception) {
                            Log.d("ERROR1", e.message.toString())
                        }

                    }, {

                        progressBar.endLoading()

                        var body: String?
                        //get status code here
                        val statusCode = it.networkResponse.statusCode.toString()
                        Log.d("ERROR12", statusCode)
                        if (it.networkResponse.data != null) {
                            try {
                                body = String(it.networkResponse.data, charset("UTF-8"))

                                //converting response to json object
                                val obj = JSONObject(body)
                                AestheticDialog.Builder(
                                    requireActivity(),
                                    DialogStyle.FLAT,
                                    DialogType.ERROR
                                ).apply {
                                    setTitle("Error")
                                    setMessage(obj.getString("message"))
                                    setCancelable(false)
                                    setDarkMode(true)
                                    setGravity(Gravity.CENTER)
                                    setAnimation(DialogAnimation.SHRINK)

                                }.setOnClickListener(object : OnDialogClickListener {
                                    override fun onClick(dialog: AestheticDialog.Builder) {
                                        dialog.dismiss()
                                    }
                                }).show()
                            } catch (e: UnsupportedEncodingException) {
                                Log.d("ERROR1", e.message.toString())
                            }
                        }
                    }

                )
                MySingleton.getInstance(requireContext())
                    .addToRequestQueue(stringRequest)
            }
        }
    }

    private fun detectTextFromImage() {
        if (!DetectConnection().isInternetAvailable(requireContext())) {
            //check for internet connection
            Toast.makeText(
                requireContext(),
                getString(R.string.check_connection),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            progressBar.startLoading(requireContext())
            FirebaseApp.initializeApp(requireContext())
            val image = FirebaseVisionImage.fromBitmap(imageBitmap)
            // val detector = FirebaseApp.initializeApp(requireContext())?.let { FirebaseVision.getInstance(it).onDeviceTextRecognizer }
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            detector.processImage(image).addOnSuccessListener {
                displayDetectedText(it)

            }.addOnFailureListener {
                progressBar.endLoading()
                Toast.makeText(requireContext(), "Error :" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun displayDetectedText(result: FirebaseVisionText?) {
        if (result!!.textBlocks.size == 0) {
            progressBar.endLoading()
            Toast.makeText(requireContext(), "No text was detected", Toast.LENGTH_SHORT).show()
        } else {
            for (block in result.textBlocks) {
                progressBar.endLoading()
                detectTv.text = block.text
                /* val blockText = block.text
               val blockConfidence = block.confidence
                val blockLanguages = block.recognizedLanguages
                val blockCornerPoints = block.cornerPoints
                val blockFrame = block.boundingBox*/
                for (line in block.lines) {
                    /* val lineText = line.text
                     val lineConfidence = line.confidence
                     val lineLanguages = line.recognizedLanguages
                     val lineCornerPoints = line.cornerPoints
                     val  lineFrame = line.boundingBox*/
                    for (element in line.elements) {
                        /*  val elementConfidence = element.confidence
                          val elementLanguages = element.recognizedLanguages
                          val elementCornerPoints = element.cornerPoints
                          val elementFrame = element.boundingBox*/
                        word = element.text
                    }
                }
            }
        }
    }





}
