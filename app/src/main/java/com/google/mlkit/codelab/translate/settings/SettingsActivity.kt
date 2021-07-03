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
import com.google.mlkit.codelab.translate.util.PrefManager
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
        urlEditText.setText(PrefManager.getInstance(applicationContext).urlKey)
        deviceEditText.setText(PrefManager.getInstance(applicationContext).deviceId)

    }

    private fun saveData() {
        if (urlEditText.text.toString().trim().length > 0 && deviceEditText.text.toString().trim().length > 0){
            PrefManager.getInstance(applicationContext).urlKey = urlEditText.text?.toString()
            PrefManager.getInstance(applicationContext).deviceId = deviceEditText.text?.toString()

            Toast.makeText(this, "Changes have been successfully saved", Toast.LENGTH_SHORT).show()

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