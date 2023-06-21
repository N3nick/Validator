package com.google.mlkit.codelab.translate.util


import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import com.google.mlkit.codelab.translate.R

class Loading {

    var loadingDialog: ProgressDialog? = null
    var handler = Handler()

    fun startLoading(context: Context?) {
        handler.post {
            if (loadingDialog == null) {
                loadingDialog = ProgressDialog(context, R.style.ProgressTheme)
                loadingDialog!!.setCancelable(false)
                loadingDialog!!.setProgressStyle(android.R.style.Widget_ProgressBar_Small)
                loadingDialog!!.show()
            }
        }
    }

    fun endLoading() {
        handler.post {
            if (loadingDialog != null) {
                loadingDialog!!.dismiss()
                loadingDialog = null
            }
        }
    }

}