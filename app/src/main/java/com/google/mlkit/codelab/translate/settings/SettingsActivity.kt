package com.google.mlkit.codelab.translate.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.mlkit.codelab.translate.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    lateinit var  urlEditText : EditText
    lateinit var deviceEditText: EditText
    lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = Html.fromHtml("<font color='#ffffff'>Settings</font>")
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_button)



        urlEditText = findViewById(R.id.urlEt)
        deviceEditText = findViewById(R.id.deviceIdEt)
        saveButton = findViewById(R.id.save_button)

        //load shared prefs data
        loadData()

        saveButton.setOnClickListener {
            saveData()
        }

    }

    private fun loadData() {
        val sharedPreferences : SharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val urlString = sharedPreferences.getString("URL_KEY", "https://demo.ticketano.com/ticket-boarding")
        val deviceId = sharedPreferences.getString("DEVICE_KEY", "1aac75011bf30e06fa9e06c973a28234")
        urlEditText.setText(urlString)
        deviceEditText.setText(deviceId)

    }

    private fun saveData() {
        if (urlEditText.text.toString().trim().length > 0 && deviceEditText.text.toString().trim().length > 0){
            val sharedPreferences : SharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.apply{
                putString("URL_KEY", urlEditText.text?.toString())
                putString("DEVICE_KEY", deviceEditText.text?.toString() )
            }.apply()
        }else {
            Toast.makeText(this, "Ensure to fill in all necessary views", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)

    }
}