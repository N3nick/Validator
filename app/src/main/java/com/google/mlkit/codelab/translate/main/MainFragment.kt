package com.google.mlkit.codelab.translate.main

import MySingleton
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.mlkit.codelab.translate.R
import com.google.mlkit.codelab.translate.util.DetectConnection
import com.google.mlkit.codelab.translate.util.Loading
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import org.xmlpull.v1.XmlPullParserException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException


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
            //detectTextFromImage()
            verifyText()
        }
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
        } else if (word.startsWith("#")) {
            progressBar.startLoading(requireContext())
            // Instantiate the RequestQueue.
            //var uniqueID = UUID.fromString("313701fc-c222-488d-b9c9-432237413155")
            val uniqueID = "a37c582b5b6c6b3267049f33cb674015"
            val bASE_URL = "https://transport.palmkash.com/ticket-boarding"
            val url = bASE_URL + "?device_id=" + uniqueID + "&ticket_number=" + word
            var prev = "null"

            if (prev != word) {
                // Request a string response from the provided URL.
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        // Display the first 500 characters of the response string.
                        progressBar.endLoading()
                        val JSONObj = response.getString("message")
                        Toast.makeText(
                            requireContext(),
                            "Response: %s".format(JSONObj),
                            Toast.LENGTH_SHORT
                        ).show()
                        //  detectTv.text = "Response: %s".format(JSONObj)
                    },
                    { error ->
                        //  result.value = getVolleyError(error)
                        progressBar.endLoading()
                        Toast.makeText(
                            requireContext(),
                            getVolleyError(error),
                            Toast.LENGTH_SHORT
                        ).show()
                    })

                // Add the request to the RequestQueue.

                MySingleton.getInstance(requireContext())
                    .addToRequestQueue(jsonObjectRequest)
                //  result.value = word + " id:" + uniqueID
            }

        } else {
            Toast.makeText(
                requireContext(),
                "Invalid Ticket",
                Toast.LENGTH_SHORT
            ).show()
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

    fun getVolleyError(error: VolleyError): String {
        var errorMsg = ""
        if (error is NoConnectionError) {
            "Your device is not connected to internet.please try again with active internet connection"
        } else if (error is NetworkError || error.cause is ConnectException) {
            errorMsg =
                "Your device is not connected to internet.please try again with active internet connection"
        } else if (error.cause is MalformedURLException) {
            errorMsg = "That was a bad request please try again…"
        } else if (error is ParseError || error.cause is IllegalStateException || error.cause is JSONException || error.cause is XmlPullParserException) {
            errorMsg = "There was an error parsing data…"
        } else if (error.cause is OutOfMemoryError) {
            errorMsg = "Device out of memory"
        } else if (error is AuthFailureError) {
            errorMsg = "Failed to authenticate user at the server, please contact support"
        } else if (error is ServerError || error.cause is ServerError) {
            errorMsg = "Internal server error occurred please try again...."
        } else if (error is TimeoutError || error.cause is SocketTimeoutException || error.cause is ConnectTimeoutException || error.cause is SocketException || (error.cause!!.message != null && error.cause!!.message!!.contains(
                "Your connection has timed out, please try again"
            ))
        ) {
            errorMsg = "Your connection has timed out, please try again"
        } else {
            errorMsg = "An unknown error occurred during the operation, please try again"
        }
        return errorMsg
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            captureImageView.setImageBitmap(imageBitmap)
            detectTextFromImage()
        }
    }


}
