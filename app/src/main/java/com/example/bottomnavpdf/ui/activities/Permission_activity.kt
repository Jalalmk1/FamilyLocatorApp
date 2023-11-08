package com.example.bottomnavpdf.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnDialogPermissionClickListener
import com.example.bottomnavpdf.databinding.ActivityPermissionBinding
import com.example.bottomnavpdf.databinding.ActivitySplashBinding
import com.example.bottomnavpdf.utils.Constants
import com.example.bottomnavpdf.utils.DialogUtils
import com.usman.smartads.AdManager

class permission_activity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionBinding
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPermissionBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE
        setContentView(binding.root)
        AllowButton()

      /*  AdManager.showNativeAd(
            this@permission_activity,
            bindingframenativead,
            "PERMISSION_NATIVE_PLACEMENT",
            true
        )*/
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

    private fun requestStoragePermission() {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_PERMISSION)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestExternalStorageManager() {
        try {
            val mIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            mIntent.addCategory("android.intent.category.DEFAULT")
            mIntent.data = Uri.parse(String.format("package:%s", packageName))
            openActivityForResult(mIntent)
        } catch (e: Exception) {
            val mIntent = Intent()
            mIntent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            openActivityForResult(mIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        try {
            if (Constants.STORAGE_PERMISSION == requestCode) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this@permission_activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    showMessage(resources.getString(R.string.permission_deny_message))
                    val intent = Intent(this@permission_activity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@permission_activity,resources.getString(R.string.permission_deny_message),Toast.LENGTH_SHORT).show()
            val intent = Intent(this@permission_activity, MainActivity::class.java)
            startActivity(intent)
        }

    }


    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                val sharedPreferences = getSharedPreferences(Constants.pref_name, MODE_PRIVATE)
                val firstLogin = sharedPreferences!!.getBoolean(Constants.first_login, true)

                if(firstLogin){
                    val intent = Intent(this@permission_activity, language_Activity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(this@permission_activity, MainActivity::class.java)
                    startActivity(intent)
                }

            }
        }

    private fun openActivityForResult(mIntent: Intent) {
        resultLauncher.launch(mIntent)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showPermissionDialog() {


        requestExternalStorageManager()

     /*   DialogUtils.permissionDialog(
            this,
            object : OnDialogPermissionClickListener {
                override fun onDiscardClick() {
                    Toast.makeText(this@permission_activity,resources.getString(R.string.permission_deny_message),Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@permission_activity, MainActivity::class.java)
                    startActivity(intent)
                }
                @RequiresApi(Build.VERSION_CODES.R)
                override fun onProceedClick() {
                    requestExternalStorageManager()
                }
            })*/
    }


    private fun AllowButton() {
        binding.allowBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
//                    premiumActivity.isfrom_Splash =true
//                    val intent = Intent(this@permission_activity, premiumActivity::class.java)
                    val intent = Intent(this@permission_activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    showPermissionDialog()
                }
            } else {
                if (checkReadWritePermission()) {
//                    premiumActivity.isfrom_Splash =true
//                    val intent = Intent(this@permission_activity, premiumActivity::class.java)
                    val intent = Intent(this@permission_activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    requestStoragePermission()
                }
            }
        }
    }

}