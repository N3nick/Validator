package com.google.mlkit.codelab.translate.main.barcode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class BarcodeViewModel(application: Application) : AndroidViewModel(application) {
    val barcodeResult = MutableLiveData<String>()
}