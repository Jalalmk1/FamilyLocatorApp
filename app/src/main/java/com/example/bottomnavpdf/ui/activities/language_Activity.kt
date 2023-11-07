package com.example.bottomnavpdf.ui.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.databinding.ActivityLanguageBinding
import com.example.bottomnavpdf.utils.Constants
import com.usman.smartads.AdManager
import java.util.*

class language_Activity : AppCompatActivity() {
    var selectedlang: String = "en"
    lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE
        setContentView(binding.root)
        binding.onBackPressed.setOnClickListener {
            onBackPressed()
        }
        binding.languageforwardbtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
//                    isfrom_Splash = true
//                    val intent = Intent(this@language_Activity, premiumActivity::class.java)
                    val intent = Intent(this@language_Activity, MainActivity::class.java)
                    continueforward(intent)
                } else {
                    val intent = Intent(this@language_Activity, permission_activity::class.java)
                    continueforward(intent)
                }
            } else {
                if (checkReadWritePermission()) {
//                    isfrom_Splash = true
//                    val intent = Intent(this@language_Activity, premiumActivity::class.java)
                    val intent = Intent(this@language_Activity, MainActivity::class.java)
                    continueforward(intent)
                } else {
                    val intent = Intent(this@language_Activity, permission_activity::class.java)
                    continueforward(intent)
                }
            }
            updateLanguage(selectedlang)
            val myEdit: SharedPreferences.Editor =
                getSharedPreferences(Constants.pref_name, MODE_PRIVATE).edit()
            myEdit.putBoolean(Constants.first_login, false)
            myEdit.apply()
        }
        binding.btLangDefault.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "en"
            binding.btLangDefaultCheck.visibility = View.VISIBLE
            binding.btLangDefault.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangEnglish.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "en"
            binding.btLangEnglishCheck.visibility = View.VISIBLE
            binding.btLangDefault.setBackgroundResource(R.drawable.btn_curve)
            binding.btLangEnglish.setBackgroundResource(R.drawable.btn_curve)

        }
        binding.btLangSpanish.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "es"
            binding.btLangSpanishCheck.visibility = View.VISIBLE
            binding.btLangSpanish.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangHindi.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "hi"
            binding.btLangHindiCheck.visibility = View.VISIBLE
            binding.btLangHindi.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangGerman.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "de"
            binding.btLangGermanCheck.visibility = View.VISIBLE
            binding.btLangGerman.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangTurkey.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "tr"
            binding.btLangTurkeyCheck.visibility = View.VISIBLE
            binding.btLangTurkey.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangFranch.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "fr"
            binding.btLangFranchCheck.visibility = View.VISIBLE
            binding.btLangFranch.setBackgroundResource(R.drawable.btn_curve)
        }

        binding.btLangJapan.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "ja"
            binding.btLangJapanCheck.visibility = View.VISIBLE
            binding.btLangJapan.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangArabic.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "ar"
            binding.btLangArabicCheck.visibility = View.VISIBLE
            binding.btLangArabic.setBackgroundResource(R.drawable.btn_curve)
        }
        binding.btLangChina.setOnClickListener {
            Setdefaultvalues()
            selectedlang = "zh"
            binding.btLangChinaCheck.visibility = View.VISIBLE
            binding.btLangChina.setBackgroundResource(R.drawable.btn_curve)
        }
//ADS
        AdManager.showNativeAd(
            this@language_Activity,
            binding.framenativead,
            "LANGUAGE_NATIVE_PLACEMENT",
            false
        )
    }

    fun Setdefaultvalues() {
        binding.btLangDefaultCheck.visibility = View.GONE
        binding.btLangEnglishCheck.visibility = View.GONE
        binding.btLangSpanishCheck.visibility = View.GONE
        binding.btLangHindiCheck.visibility = View.GONE
        binding.btLangGermanCheck.visibility = View.GONE
        binding.btLangTurkeyCheck.visibility = View.GONE
        binding.btLangFranchCheck.visibility = View.GONE
        binding.btLangJapanCheck.visibility = View.GONE
        binding.btLangArabicCheck.visibility = View.GONE
        binding.btLangChinaCheck.visibility = View.GONE
        binding.btLangDefault.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangEnglish.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangSpanish.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangHindi.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangGerman.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangTurkey.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangJapan.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangArabic.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangChina.setBackgroundColor(getColor(R.color.transparent))
        binding.btLangFranch.setBackgroundColor(getColor(R.color.transparent))

    }

    private fun checkReadWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun updateLanguage(code: String) {
        try {
            val locale = Locale(code)
            val resources = resources
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            val sharedPreferences = getSharedPreferences(Constants.pref_name, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(Constants.SELECTED_LANGUAGE, code)
            editor.apply()
        } catch (_: Exception) {
        }
    }

    fun continueforward(intent: Intent) {
        AdManager.showInterstitialAd(this@language_Activity, "LANGUAGE_INTER_PLACEMENT") {
            startActivity(intent)
            finish()
        }
    }
}