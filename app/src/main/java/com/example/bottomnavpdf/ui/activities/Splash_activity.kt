package com.example.bottomnavpdf.ui.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.databinding.ActivitySplashBinding
import com.example.bottomnavpdf.utils.Constants
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.usman.smartads.AdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class splash_activity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    var firstLogin = true
    var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences(Constants.pref_name, MODE_PRIVATE)

        /*
        Handler(Looper.getMainLooper()).postDelayed({
                 binding.lottieAnim.visibility = View.GONE
                 binding.layoutbottom.visibility = View.VISIBLE
             }, 7000)

             */

//        binding.layoutbottom.visibility = View.VISIBLE

        val ss = SpannableString(getString(R.string.privacysplashtext))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.privacy_policy_link))
                    )
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 53, 68, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(StyleSpan(Typeface.BOLD), 11, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(StyleSpan(Typeface.BOLD), 55, 70, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(UnderlineSpan(), 55, 70, 0)

        binding.splshPrivacy.text = ss
        binding.splshPrivacy.movementMethod = LinkMovementMethod.getInstance()
        binding.splshPrivacy.setLinkTextColor(getColor(R.color.black))
        binding.splshPrivacy.highlightColor = getColor(R.color.transparent)


        binding.splashcontinue.setOnClickListener {
            AdManager.showInterstitialAd(this@splash_activity, "SPLASH_INTER_PLACEMENT") {
                if (firstLogin) {
                    val intent = Intent(this@splash_activity, permission_activity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            val intent = Intent(this@splash_activity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val intent = Intent(this@splash_activity, permission_activity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        if (checkReadWritePermission()) {
//                               premiumActivity.isfrom_Splash =true
//                               val intent = Intent(this@splash_activity, premiumActivity::class.java)
                            val intent = Intent(this@splash_activity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val intent =
                                Intent(this@splash_activity, permission_activity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO){
            delay(5000)
            withContext(Dispatchers.Main){
                binding.lottieAnim.visibility = View.GONE
                binding.splashcontinue.visibility = View.VISIBLE
                firstLogin = sharedPreferences!!.getBoolean(Constants.first_login, true)
                if (firstLogin) {
                    binding.splshPrivacy.visibility = View.VISIBLE
                } else {
                    binding.splshPrivacy.visibility = View.GONE
                }
            }
        }

        AdManager.init(
            application,
            this@splash_activity,
            FirebaseRemoteConfig.getInstance(),
            "SPLASH_FIRST_PLACEMENT"
        ) {
/*
            AdManager.showNativeAd(
                this@splash_activity,
                binding.framenativead,
                "SPLASH_NATIVE_PLACEMENT",
                false
            )*/
        }
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

}