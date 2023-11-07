package com.example.bottomnavpdf.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.utils.Constants
import java.util.*

class presplash_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presplash)
        try {
            val language = getSharedPreferences(Constants.pref_name, MODE_PRIVATE).getString(Constants.SELECTED_LANGUAGE, null)
            Log.d("LANGUAGEEXCEP", "default lang\t$language")
            if (language != null) {
                val locale = Locale(language)
                val resources = resources
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        } catch (e: Exception) {
            Log.d("LANGUAGEEXCEP", e.message!!)
        }
        Handler(Looper.getMainLooper()).postDelayed({
        startActivity(Intent(this,splash_activity::class.java))
            finish()
        }, 5000)
    }
}