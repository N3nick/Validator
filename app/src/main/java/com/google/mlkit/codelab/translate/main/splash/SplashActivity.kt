package com.google.mlkit.codelab.translate.main.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.mlkit.codelab.translate.MainActivityNav
import com.google.mlkit.codelab.translate.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splash_text.animate().translationY(-1600f).setDuration(1000).setStartDelay(4000)
        lottie_animation.animate().translationY(1400f).setDuration(1000).setStartDelay(4000).withEndAction {
            val i = Intent(this, MainActivityNav::class.java)
            startActivity(i)
            finish()
            // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}