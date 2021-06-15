package com.google.mlkit.codelab.translate.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class DetectConnection {
    public fun isInternetAvailable(context: Context) : Boolean{
        val info : NetworkInfo =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        return info.isConnected
    }
}