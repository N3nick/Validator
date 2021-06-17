package com.google.mlkit.codelab.translate.main.barcode

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.mlkit.codelab.translate.R
import com.google.mlkit.codelab.translate.analyzer.BarcodeAnalyzer
import com.google.mlkit.codelab.translate.util.DetectConnection
import com.google.mlkit.codelab.translate.util.Loading
import com.google.mlkit.codelab.translate.util.ScopedExecutor
import kotlinx.android.synthetic.main.fragment_barcode.*
import kotlinx.android.synthetic.main.fragment_barcode.view.*
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import org.xmlpull.v1.XmlPullParserException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

class BarcodeFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var container: ViewGroup
    private lateinit var textView: TextView
    private lateinit var scanButton: Button
    private lateinit var verifyButton: Button
    var word = ""
    val progressBar = Loading()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_barcode, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view as RelativeLayout
        textView = container.findViewById(R.id.tv_textView)
        verifyButton = container.findViewById(R.id.verify_btn)
        initiateCodeScanner()


        verifyButton.setOnClickListener {
            doTicketVerification()
        }


    }

    private fun doTicketVerification() {
        //check if any word has been detected
        if (!DetectConnection().isInternetAvailable(requireContext())) {
            //check for internet connection
            Toast.makeText(
                requireContext(),
                getString(R.string.check_connection),
                Toast.LENGTH_SHORT
            ).show()
        } else if (word.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No word has been detected, Please Ensure to take a clear Image",
                Toast.LENGTH_SHORT
            ).show()
        } else {
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

        } /*else {
            Toast.makeText(
                requireContext(),
                "Invalid Ticket",
                Toast.LENGTH_SHORT
            ).show()
        }*/
    }

    private fun initiateCodeScanner() {
        codeScanner = CodeScanner(requireContext(), scanner_view)
        codeScanner.apply {
            //set to use back camera
            camera = CodeScanner.CAMERA_BACK
            //set to scan all barcode formats
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                activity?.runOnUiThread {
                    textView.text = it.text
                    word = it.text
                }
            }

            errorCallback = ErrorCallback {
                activity?.runOnUiThread {
                    Log.e("Main", "codeScanner: ${it.message}")
                }
            }
        }
        codeScanner.startPreview()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
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


}