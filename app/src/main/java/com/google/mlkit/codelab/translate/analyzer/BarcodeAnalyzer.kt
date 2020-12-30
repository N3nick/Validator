package com.google.mlkit.codelab.translate.analyzer

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val barcodeResult: MutableLiveData<String>
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE)
        .build()


    private val detector = BarcodeScanning.getClient(options)

    init {
        lifecycle.addObserver(detector)
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizeBarcodes(image).addOnCompleteListener {
            imageProxy.close()
        }
    }

    private fun recognizeBarcodes(
        image: InputImage
    ): Task<MutableList<Barcode>> {
        // Pass image to an ML Kit Vision API
        return detector.process(image)
            .addOnSuccessListener { list ->
                val barcode = list.firstOrNull()
                barcodeResult.value = barcode?.displayValue
                Log.d(TAG, "Barcode scanning success ${barcodeResult.value}")
            }
            .addOnFailureListener {
                val message = getErrorMessage(it)
                barcodeResult.value = message
                Log.e(TAG, "Barcode scanning failure", it)
                message?.let {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

    companion object {
        private const val TAG = "BarcodeAnalyzer"
    }
}