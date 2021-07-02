package com.google.mlkit.codelab.translate.main.barcode

import MySingleton
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.budiyev.android.codescanner.*
import com.google.mlkit.codelab.translate.R
import com.google.mlkit.codelab.translate.util.DetectConnection
import com.google.mlkit.codelab.translate.util.Loading
import kotlinx.android.synthetic.main.fragment_barcode.*
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class BarcodeFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var container: ViewGroup
    private lateinit var bar : View
    var word = ""
    val progressBar = Loading()
    var uniqueID : String? = ""
    var bASE_URL : String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_barcode, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view as RelativeLayout
        bar = container.findViewById(R.id.bar)
        startAnimation()
        initiateCodeScanner()

        loadSharedPreferences()


    }

    private fun loadSharedPreferences() {
        val sharedPreferences : SharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        bASE_URL = sharedPreferences.getString("URL_KEY", "https://demo.ticketano.com/ticket-boarding")
        uniqueID = sharedPreferences.getString("DEVICE_KEY", "1aac75011bf30e06fa9e06c973a28234")
    }

    private fun startAnimation() {
        val animation : Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scan_animation)
        animation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                bar.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }

        })
        bar.startAnimation(animation)
    }

    private fun doTicketVerification() {
        //check if any word has been detected
        //stop the codeScanner
        codeScanner.releaseResources()
        //remove the scanning animation
        bar.visibility = View.GONE
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
                "No BarCode has been detected, Please Scan Again",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            progressBar.startLoading(requireContext())
            codeScanner.releaseResources()

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
                            openSuccessDialog(responseObject)

                        }catch (e : Exception){
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
                                openErrorDialog(obj)
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

    private fun openSuccessDialog(responseObject: JSONObject) {
        val  builder = AlertDialog.Builder(requireContext())
        val layoutView = layoutInflater.inflate(R.layout.success_dialog, null)
        val dialogButton = layoutView.findViewById<AppCompatButton>(R.id.button_ok)
        val messageView = layoutView.findViewById<TextView>(R.id.dialog_success_text)
        messageView.text = responseObject.getString("message")
        builder.setView(layoutView)
        val alertDialog = builder.create()
        alertDialog.show()

        dialogButton.setOnClickListener {
            alertDialog.dismiss()
            //stop the codeScanner
            codeScanner.startPreview()
            //remove the scanning animation
            bar.visibility = View.VISIBLE
        }
    }

    private fun openErrorDialog(responseObject: JSONObject) {
        val  builder = AlertDialog.Builder(requireContext())
        val layoutView = layoutInflater.inflate(R.layout.error_dialog, null)
        val dialogButton = layoutView.findViewById<AppCompatButton>(R.id.errorButton)
        val messageView = layoutView.findViewById<TextView>(R.id.dialog_error_text)
        messageView.text = responseObject.getString("message")
        builder.setView(layoutView)
        val alertDialog = builder.create()
        alertDialog.show()

        dialogButton.setOnClickListener {
            alertDialog.dismiss()
            //stop the codeScanner
            codeScanner.startPreview()
            //remove the scanning animation
            bar.visibility = View.VISIBLE
        }
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
                    word = it.text
                    doTicketVerification()
                }
            }

            errorCallback = ErrorCallback {
                activity?.runOnUiThread {
                   Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
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


}