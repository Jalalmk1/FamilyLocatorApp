package com.example.bottomnavpdf.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.example.bottomnavpdf.BuildConfig
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.databinding.ActivityPremiumBinding
import com.example.bottomnavpdf.utils.Variables
import com.usman.smartads.AdManager

class premiumActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    lateinit var binding: ActivityPremiumBinding
    var bp: BillingProcessor? = null
    companion object{
        var isfrom_Splash=false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bp = BillingProcessor(this, getString(R.string.billing_id), this)
        bp!!.initialize()
        binding.premiumPrivacy.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.privacy_policy_link))
                )
            )
        }
        binding.premiumBuy.setOnClickListener {
            beComeVIP()
        }
        binding.continuewithads.setOnClickListener {
            premiumCross_click()
        }
        binding.premiumcross.setOnClickListener {
            premiumCross_click()
        }
    }

    private fun beComeVIP() {
        val billingProcessor = bp
        if (billingProcessor == null || billingProcessor.isPurchased(BuildConfig.APPLICATION_ID)) {
            Toast.makeText(this, "Already purchased!", Toast.LENGTH_SHORT).show()
            return
        }
        val access = bp
        access!!.purchase(this, BuildConfig.APPLICATION_ID)
    }
    fun premiumCross_click(){
        AdManager.showInterstitialAd(this@premiumActivity,"PREMIUM_INTER_PLACEMENT"){
            if (isfrom_Splash){
                val intent = Intent(this@premiumActivity, MainActivity::class.java)
                startActivity(intent)
                isfrom_Splash=false
                finish()
            }
            else {
                finish()
            }
        }
    }

    override fun onPurchaseHistoryRestored() {
        Variables.isPremium = bp!!.isPurchased(BuildConfig.APPLICATION_ID)
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {}
    override fun onBillingInitialized() {
        val loadOwnedPurchasesFromGoogle = bp!!.loadOwnedPurchasesFromGoogle()
        if (loadOwnedPurchasesFromGoogle) {
            Variables.isPremium = bp!!.isPurchased(BuildConfig.APPLICATION_ID)
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
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