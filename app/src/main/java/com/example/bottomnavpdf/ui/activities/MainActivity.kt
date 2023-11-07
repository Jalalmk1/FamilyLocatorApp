package com.example.bottomnavpdf.ui.activities


import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.CompoundButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.bottomnavpdf.BuildConfig
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.adapter.AdapterFavouriteFiles
import com.example.bottomnavpdf.adapter.AdapterRecentFiles
import com.example.bottomnavpdf.adapter.adapter_AllFiles
import com.example.bottomnavpdf.adapter.adapter_AllFiles.Companion.isfromlongclick
import com.example.bottomnavpdf.adapter.adapter_AllFiles.Companion.selectedpos
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivityMainBinding
import com.example.bottomnavpdf.databinding.LanguageDialogBinding
import com.example.bottomnavpdf.`interface`.Main_selectedItems
import com.example.bottomnavpdf.ui.favourite.FavouriteFragment
import com.example.bottomnavpdf.ui.home.HomeFragment
import com.example.bottomnavpdf.ui.recent.RecentFragment
import com.example.bottomnavpdf.ui.tool.ToolsFragment
import com.example.bottomnavpdf.utils.Constants
import com.usman.smartads.AdManager
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivityMainBinding
    var resultLauncher: ActivityResultLauncher<Intent>? = null
    var homefragment: Fragment? = null
    var recentfragment: Fragment? = null
    var favrtfragment: Fragment? = null
    var toolsfrag: Fragment? = null
    var currentFrag: String? = "home"
    var selectedadapter: String? = null
    var selectall_clicked = false

    companion object {
        var selectedFileList: ArrayList<FileItems> = ArrayList()
        var positionofadapter: ArrayList<Int> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE

        setContentView(binding.root)
        val anim = ScaleAnimation(
            1f, 0.9f, 1f, 0.9f,
            Animation.RESTART, 0.5f, Animation.RESTART, 0.5f
        )
        anim.duration = 150
        anim.repeatCount = 0

        homefragment = HomeFragment(object : Main_selectedItems {
            override fun main_onItemselected(position: Int) {

                binding.drawercontentlayout.totalselectedtv.text =
                    selectedFileList.size.toString() + " ${getString(R.string.selected)}"
                if (selectedFileList.size == 0) {
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                    binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.foregrounddisable
                        )
                    )
                } else {
                    binding.drawercontentlayout.layoutbottomselected.foreground = null
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
                }
                currentFrag = "home"
                selectedadapter = "home"
                binding.drawercontentlayout.toolbarmain.visibility = View.GONE
                binding.drawercontentlayout.toolbarselected.visibility = View.VISIBLE
                binding.drawercontentlayout.layoutbottomtext.visibility = View.GONE
                binding.drawercontentlayout.layoutbottomselected.visibility = View.VISIBLE
            }
        })

        recentfragment = RecentFragment(object : Main_selectedItems {
            override fun main_onItemselected(position: Int) {
                binding.drawercontentlayout.totalselectedtv.text =
                    selectedFileList.size.toString() + " ${getString(R.string.selected)}"
                if (selectedFileList.size == 0) {
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                    binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.foregrounddisable
                        )
                    )

                } else {
                    binding.drawercontentlayout.layoutbottomselected.foreground = null
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
                }
                currentFrag = "recent"
                selectedadapter = "recent"
                binding.drawercontentlayout.toolbarmain.visibility = View.GONE
                binding.drawercontentlayout.toolbarselected.visibility = View.VISIBLE
                binding.drawercontentlayout.layoutbottomtext.visibility = View.GONE
                binding.drawercontentlayout.layoutbottomselected.visibility = View.VISIBLE
            }
        })
        favrtfragment = FavouriteFragment(object : Main_selectedItems {
            override fun main_onItemselected(position: Int) {
                binding.drawercontentlayout.totalselectedtv.text =
                    selectedFileList.size.toString() + " ${getString(R.string.selected)}"
                if (selectedFileList.size == 0) {
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                    binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.foregrounddisable
                        )
                    )
                } else {
                    binding.drawercontentlayout.layoutbottomselected.foreground = null
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
                }
                currentFrag = "fvrt"
                selectedadapter = "fvrt"
                binding.drawercontentlayout.toolbarmain.visibility = View.GONE
                binding.drawercontentlayout.toolbarselected.visibility = View.VISIBLE
                binding.drawercontentlayout.layoutbottomtext.visibility = View.GONE
                binding.drawercontentlayout.layoutbottomselected.visibility = View.VISIBLE
            }
        })
        toolsfrag = ToolsFragment(this@MainActivity)

        if (savedInstanceState == null) {
            addFragment(homefragment as HomeFragment)
        }

        drawerLayout = binding.drawerLayout
        binding.drawercontentlayout.ivOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
            handleClicksOnContentDrawer()
        }
        binding.drawercontentlayout.bottomTvHome.setOnClickListener {
            if (currentFrag != "home") {
                AdManager.showInterstitialAd(this@MainActivity, "DASHBOARD_BOTTOM_HOME") {
                    setDefaultColors()
                    binding.drawercontentlayout.bottomTvHome.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.homefilled
                        ),
                        null,
                        null
                    )
                    binding.drawercontentlayout.bottomTvHome.startAnimation(anim)
                    if ((homefragment as HomeFragment).isAdded || (homefragment as HomeFragment).isDetached) {
                        Log.d("FRAGMENTSTUFF", "home already added")
                        addFragment(homefragment as HomeFragment)
                    } else {
                        Log.d("FRAGMENTSTUFF", "home addedd")
                        addFragment(homefragment as HomeFragment)
                    }
                    binding.drawercontentlayout.PdfReaderTitle.text =
                        "${getString(R.string.pdf_reader)}"
                    currentFrag = "home"
                }
            }
        }
        binding.drawercontentlayout.bottomTvRecent.setOnClickListener {
            if (currentFrag != "recent") {
                AdManager.showInterstitialAd(this@MainActivity, "DASHBOARD_BOTTOM_RECENT") {
                    binding.drawercontentlayout.PdfReaderTitle.text =
                        getString(R.string.reent_files)
                    setDefaultColors()
                    binding.drawercontentlayout.bottomTvRecent.setCompoundDrawablesWithIntrinsicBounds(
                        null, ContextCompat.getDrawable(
                            this,
                            R.drawable.recentsfilled
                        ), null, null
                    )
                    binding.drawercontentlayout.bottomTvRecent.startAnimation(anim)
                    addFragment(recentfragment as RecentFragment)
                    currentFrag = "recent"
                }
            }
        }
        binding.drawercontentlayout.bottomTvFav.setOnClickListener {
            if (currentFrag != "fvrt") {
                AdManager.showInterstitialAd(this@MainActivity, "DASHBOARD_BOTTOM_FAVRT") {
                    binding.drawercontentlayout.PdfReaderTitle.text =
                        getString(com.example.bottomnavpdf.R.string.favourite)
                    setDefaultColors()
                    binding.drawercontentlayout.bottomTvFav.setCompoundDrawablesWithIntrinsicBounds(
                        null, ContextCompat.getDrawable(
                            this,
                            com.example.bottomnavpdf.R.drawable.favoritesfilled
                        ), null, null
                    )
                    binding.drawercontentlayout.bottomTvFav.startAnimation(anim)
                    addFragment(favrtfragment as FavouriteFragment)
                    currentFrag = "fvrt"
                }
            }
        }
        binding.drawercontentlayout.bottomTvTools.setOnClickListener {
            if (currentFrag != "tools") {
                AdManager.showInterstitialAd(this@MainActivity, "DASHBOARD_BOTTOM_TOOLS") {
                    binding.drawercontentlayout.PdfReaderTitle.text =
                        getString(com.example.bottomnavpdf.R.string.tools)
                    setDefaultColors()
                    binding.drawercontentlayout.bottomTvTools.setCompoundDrawablesWithIntrinsicBounds(
                        null, ContextCompat.getDrawable(
                            this,
                            R.drawable.toolsfilled
                        ), null, null
                    )
                    binding.drawercontentlayout.bottomTvTools.startAnimation(anim)
                    addFragment(toolsfrag as ToolsFragment)
                    binding.drawercontentlayout.dashboardSearch.visibility = View.GONE
                    binding.drawercontentlayout.dashboardCheckbox.visibility = View.GONE
                    currentFrag = "tools"
                }
            }
        }
        binding.drawercontentlayout.dashboardSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        binding.drawercontentlayout.dashboardCheckbox.setOnClickListener {
            if (!currentFrag.isNullOrEmpty() && currentFrag.equals("home")) {
                (homefragment as HomeFragment).sortFiles()
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("recent")) {
                (recentfragment as RecentFragment).sort()
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("fvrt")) {
                (favrtfragment as FavouriteFragment).sort()
            }
        }

        binding.drawercontentlayout.userSelected.setOnClickListener {

            Log.e("isLONG","USER CALEDD")

            binding.drawercontentlayout.userSelected.setImageResource(R.drawable.selectall_icon2)
            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)


            selectedFileList.clear()
            positionofadapter.clear()

            if (!currentFrag.isNullOrEmpty() && currentFrag.equals("home")) {
                selectedadapter = "home"
                isfromlongclick = true
//                selectedFileList.addAll(HomeFragment.originalFileList)
                adapter_AllFiles.isselectall = "no"
                selectedpos = -1
                (homefragment as HomeFragment).onSelecteditemCall(false)
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("recent")) {
                selectedadapter = "recent"
                AdapterRecentFiles.isfromlongclick = true
//                selectedFileList.addAll(RecentFragment.filesList)
                AdapterRecentFiles.isselectall = "no"
                AdapterRecentFiles.selectedpos = -1
                (recentfragment as RecentFragment).onSelecteditemCall(false)
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("fvrt")) {
                selectedadapter = "fvrt"
                AdapterFavouriteFiles.isfromlongclick = true
//                selectedFileList.addAll(FavouriteFragment.filesList)
                AdapterFavouriteFiles.isselectall = "no"
                AdapterFavouriteFiles.selectedpos = -1
                (favrtfragment as FavouriteFragment).onSelecteditemCall(false)
            }
            if (selectedFileList.size == 0) {
                binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.foregrounddisable
                    )
                )
            } else {
                binding.drawercontentlayout.layoutbottomselected.foreground = null
                binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
            }


            binding.drawercontentlayout.totalselectedtv.text =
                selectedFileList.size.toString() + " ${getString(R.string.selected)}"
            selectall_clicked = true
//            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon2)
        }
        binding.drawercontentlayout.selectallfilesBtn.setOnClickListener {

            binding.drawercontentlayout.userSelected.setImageResource(R.drawable.selectall_icon)
            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon2)

            selectedFileList.clear()
            positionofadapter.clear()


            if (!currentFrag.isNullOrEmpty() && currentFrag.equals("home")) {
                selectedadapter = "home"
                isfromlongclick = true
                selectedFileList.addAll(HomeFragment.originalFileList)
                adapter_AllFiles.isselectall = "yes"
                selectedpos = -1
                (homefragment as HomeFragment).onSelecteditemCall(true)
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("recent")) {
                selectedadapter = "recent"
                AdapterRecentFiles.isfromlongclick = true
                selectedFileList.addAll(RecentFragment.filesList)
                AdapterRecentFiles.isselectall = "yes"
                AdapterRecentFiles.selectedpos = -1
                (recentfragment as RecentFragment).onSelecteditemCall(true)
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("fvrt")) {
                selectedadapter = "fvrt"
                AdapterFavouriteFiles.isfromlongclick = true
                selectedFileList.addAll(FavouriteFragment.filesList)
                AdapterFavouriteFiles.isselectall = "yes"
                AdapterFavouriteFiles.selectedpos = -1
                (favrtfragment as FavouriteFragment).onSelecteditemCall(true)
            }
            if (selectedFileList.size == 0) {
                binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.foregrounddisable
                    )
                )
            } else {
                binding.drawercontentlayout.layoutbottomselected.foreground = null
                binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
            }
            binding.drawercontentlayout.totalselectedtv.text =
                selectedFileList.size.toString() + " ${getString(R.string.selected)}"
            selectall_clicked = true

//            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon2)

    /*        if (selectall_clicked) {
                if (!currentFrag.isNullOrEmpty() && currentFrag.equals("home")) {
                    selectedadapter = "home"
                    isfromlongclick = false
                    adapter_AllFiles.isselectall = null
                    selectedpos = -1
                    (homefragment as HomeFragment).onSelecteditemCall(true)
                } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("recent")) {
                    selectedadapter = "recent"
                    AdapterRecentFiles.isfromlongclick = false
                    AdapterRecentFiles.isselectall = null
                    AdapterRecentFiles.selectedpos = -1
                    (recentfragment as RecentFragment).onSelecteditemCall(true)
                } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("fvrt")) {
                    selectedadapter = "fvrt"
                    AdapterFavouriteFiles.isfromlongclick = false
                    AdapterFavouriteFiles.isselectall = null
                    AdapterFavouriteFiles.selectedpos = -1
                    (favrtfragment as FavouriteFragment).onSelecteditemCall(true)
                }
                selectedFileList.clear()
                positionofadapter.clear()
                binding.drawercontentlayout.totalselectedtv.text =
                    selectedFileList.size.toString() + " ${getString(R.string.selected)}"
                if (selectedFileList.size == 0) {
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                    binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.foregrounddisable
                        )
                    )

                } else {
                    binding.drawercontentlayout.layoutbottomselected.foreground = null
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
                }
                selectall_clicked = false
                binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)
            }
            else {
                selectedFileList.clear()
                positionofadapter.clear()
                if (!currentFrag.isNullOrEmpty() && currentFrag.equals("home")) {
                    selectedadapter = "home"
                    isfromlongclick = false
                    selectedFileList.addAll(HomeFragment.originalFileList)
                    adapter_AllFiles.isselectall = "yes"
                    selectedpos = -1
                    (homefragment as HomeFragment).onSelecteditemCall(true)
                } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("recent")) {
                    selectedadapter = "recent"
                    AdapterRecentFiles.isfromlongclick = false
                    selectedFileList.addAll(RecentFragment.filesList)
                    AdapterRecentFiles.isselectall = "yes"
                    AdapterRecentFiles.selectedpos = -1
                    (recentfragment as RecentFragment).onSelecteditemCall(true)
                } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("fvrt")) {
                    selectedadapter = "fvrt"
                    AdapterFavouriteFiles.isfromlongclick = false
                    selectedFileList.addAll(FavouriteFragment.filesList)
                    AdapterFavouriteFiles.isselectall = "yes"
                    AdapterFavouriteFiles.selectedpos = -1
                    (favrtfragment as FavouriteFragment).onSelecteditemCall(true)
                }
                if (selectedFileList.size == 0) {
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = false
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = false
                    binding.drawercontentlayout.layoutbottomselected.foreground = ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.foregrounddisable
                        )
                    )
                } else {
                    binding.drawercontentlayout.layoutbottomselected.foreground = null
                    binding.drawercontentlayout.bottomSelectedDelete.isEnabled = true
                    binding.drawercontentlayout.bottomSelectedShare.isEnabled = true
                }
                binding.drawercontentlayout.totalselectedtv.text =
                    selectedFileList.size.toString() + " ${getString(R.string.selected)}"
                selectall_clicked = true
                binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon2)
            }*/
        }

        binding.drawercontentlayout.appCompatImageView.setOnClickListener {
            selectedadapter = null
            if (!currentFrag.isNullOrEmpty() && currentFrag.equals("home")) {
                adapter_AllFiles.isselectall = null
                (homefragment as HomeFragment).onSelecteditemCall(false)
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("recent")) {
                AdapterRecentFiles.isselectall = null
                (recentfragment as RecentFragment).onSelecteditemCall(false)
            } else if (!currentFrag.isNullOrEmpty() && currentFrag.equals("fvrt")) {
                AdapterFavouriteFiles.isselectall = null
                (favrtfragment as FavouriteFragment).onSelecteditemCall(false)
            }
            binding.drawercontentlayout.toolbarmain.visibility = View.VISIBLE
            binding.drawercontentlayout.toolbarselected.visibility = View.GONE
            binding.drawercontentlayout.layoutbottomtext.visibility = View.VISIBLE
            binding.drawercontentlayout.layoutbottomselected.visibility = View.GONE
            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)

        }
        binding.drawercontentlayout.bottomSelectedShare.setOnClickListener {
            if (selectedFileList.isEmpty()) {
                Toast.makeText(this, "Please select file", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND_MULTIPLE
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share files.")
                    intent.type = "application/pdf"
                    val files: ArrayList<Uri> = ArrayList()
                    for (path in selectedFileList) {
                        val uri: Uri = FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            File(path.pdfFilePath)
                        )
                        files.add(uri)
                    }
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                    startActivity(intent)
                } catch (_: Exception) {
                }
            }
        }
        binding.drawercontentlayout.bottomSelectedDelete.setOnClickListener {

            if (selectedFileList.isEmpty()) {
                Toast.makeText(this, "Please select file", Toast.LENGTH_SHORT).show()
            } else {
                val dialog = Dialog(this@MainActivity)
                dialog.setContentView(R.layout.deleteitem_dailog)
                dialog.setCancelable(true)
                dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val btdelete = dialog.findViewById<AppCompatButton>(R.id.deletebtn)
                val btncancel = dialog.findViewById<AppCompatButton>(R.id.cancel_button)
                btncancel.setOnClickListener {
                    dialog.dismiss()
                }
                btdelete.setOnClickListener {
                    for (path in selectedFileList) {
                        val files = File(path.pdfFilePath)
                        if (files.exists()) {
                            Log.d("TOTALSELECTED", "Successfully Deleted!\t" + files.absolutePath)
                            files.delete()
                        }
                    }
                    if (selectedFileList.size == positionofadapter.size) {
                        positionofadapter.sortedDescending().forEach {
                            if (currentFrag.equals("home")) {
                                Log.d("PPPPPP", it.toString())
                                (homefragment as HomeFragment).itemremovedat(it)
                            }
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        (homefragment as HomeFragment).onSelecteditemCall(false)
                        binding.drawercontentlayout.toolbarmain.visibility = View.VISIBLE
                        binding.drawercontentlayout.toolbarselected.visibility = View.GONE
                        binding.drawercontentlayout.layoutbottomtext.visibility = View.VISIBLE
                        binding.drawercontentlayout.layoutbottomselected.visibility = View.GONE
                        selectedadapter = null
                        binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)

                    }, 1000)
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
        binding.drawercontentlayout.allowBtnPermission.setOnClickListener {
            AllowButton()
        }
//         Initialize result launcher
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                Toast.makeText(this,"main", Toast.LENGTH_SHORT).show()

                val sUri = result.data!!.data
                viewerPdf.uriBybrowse = sUri
                val intent = Intent(this, viewerPdf::class.java)
                startActivity(intent)
            } catch (_: Exception) {
            }
        }

    }

    private fun AllowButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                /////////
                removelayout_AND_proceed()
            } else {
                requestExternalStorageManager()
            }
        } else {
            if (checkReadWritePermission()) {
                ////////
                removelayout_AND_proceed()
            } else {
                requestStoragePermission()
            }
        }
    }

    fun removelayout_AND_proceed() {
        binding.drawercontentlayout.framelayoutPermission.visibility=View.GONE
        addFragment(homefragment as HomeFragment)
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

    private var resultLauncher22 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    /////////
                    removelayout_AND_proceed()
                } else {
                    Toast.makeText(this@MainActivity,resources.getString(R.string.permission_deny_message),Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun openActivityForResult(mIntent: Intent) {
        resultLauncher22.launch(mIntent)
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
                    //////////
                    removelayout_AND_proceed()
                } else {
                    Toast.makeText(this@MainActivity,resources.getString(R.string.permission_deny_message),Toast.LENGTH_SHORT).show()
                }
            }
        } catch (_: Exception) {
        }

    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            Constants.STORAGE_PERMISSION
        )
    }

    private fun addFragment(fragment: Fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                binding.drawercontentlayout.framelayoutPermission.visibility=View.VISIBLE
                return
            }
        } else {
            if (!checkReadWritePermission()) {
                binding.drawercontentlayout.framelayoutPermission.visibility=View.VISIBLE
                return
            }
        }


//        if (!fragment.isAdded) {
//            Log.d("FRAGMENTSTUFF", "add frag....")
//            supportFragmentManager.beginTransaction()
//                .add(
//                    R.id.fragment_container_view,
//                    fragment
//                )
//                .commit()
//        }
//        else {
//            Log.d("FRAGMENTSTUFF", "showed frag....")
//            if (fragment.javaClass.name.equals(HomeFragment::class.java.name)) {
//                supportFragmentManager.beginTransaction()
//                    .remove(recentfragment as RecentFragment)
//                    .remove(favrtfragment as FavouriteFragment)
//                    .remove(toolsfrag as ToolsFragment)
//                    .show(homefragment as HomeFragment).commit()
//                Handler(Looper.getMainLooper()).postDelayed(Runnable {
//                    if (!(homefragment as HomeFragment).isHidden) {
//                        (homefragment as HomeFragment).updateitems()
//                    }
//                }, 400)
//            } else {
                Log.d("FRAGMENTSTUFF", "replaced frag....")
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container_view,
                        fragment
                    ).commit()
//            }
//        }
    }

    private fun handleClicksOnContentDrawer() {
        binding.LLNight.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.LLlanguage.setOnClickListener {
            var selectedlang = getSharedPreferences(
                Constants.pref_name,
                MODE_PRIVATE
            ).getString(Constants.SELECTED_LANGUAGE, "en")
            drawerLayout.closeDrawer(GravityCompat.START)
            val binding11: LanguageDialogBinding = LanguageDialogBinding.inflate(layoutInflater)
            setDefault(binding11, selectedlang!!)
            val dialog = Dialog(this@MainActivity)
            dialog.setCancelable(true)
            dialog.window?.setBackgroundDrawableResource(com.example.bottomnavpdf.R.color.transparent)
            dialog.setContentView(binding11.root)


            binding11.langDialogOk.setOnClickListener {
                updateLanguage(selectedlang!!)
                dialog.dismiss()
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }

            binding11.btLangEnglish.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "en"
                binding11.btLangEnglishCheck.visibility = View.VISIBLE
            }
            binding11.btLangSpanish.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "es"
                binding11.btLangSpanishCheck.visibility = View.VISIBLE
            }
            binding11.btLangHindi.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "hi"
                binding11.btLangHindiCheck.visibility = View.VISIBLE
            }
            binding11.btLangGerman.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "de"
                binding11.btLangGermanCheck.visibility = View.VISIBLE
            }
            binding11.btLangTurkey.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "tr"
                binding11.btLangTurkeyCheck.visibility = View.VISIBLE
            }
            binding11.btLangFranch.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "fr"
                binding11.btLangFranchCheck.visibility = View.VISIBLE
            }
            binding11.btLangJapan.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "ja"
                binding11.btLangJapanCheck.visibility = View.VISIBLE
            }
            binding11.btLangArabic.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "ar"
                binding11.btLangArabicCheck.visibility = View.VISIBLE
            }
            binding11.btLangChina.setOnClickListener {
                Setdefaultvalues(binding11)
                selectedlang = "zh"
                binding11.btLangChinaCheck.visibility = View.VISIBLE
            }
            dialog.show()
        }
        binding.nightSwitchBtn.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.nightSwitchBtn.setBackgroundResource((com.example.bottomnavpdf.R.drawable.bg_switch_on))
            } else {
                binding.nightSwitchBtn.setBackgroundResource((com.example.bottomnavpdf.R.drawable.bg_switch_off))
            }
        })


        binding.LLPremium.setOnClickListener {
//            startActivity(Intent(this, premiumActivity::class.java))
//            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "This feature is coming soon!", Toast.LENGTH_SHORT).show()
        }
        binding.LLPrivacy.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(com.example.bottomnavpdf.R.string.privacy_policy_link))
                )
            )
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.LLbrowsepdf.setOnClickListener {
            selectPDF()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.screenonSwitchBtn.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                binding.screenonSwitchBtn.setBackgroundResource((com.example.bottomnavpdf.R.drawable.bg_switch_on))
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                binding.screenonSwitchBtn.setBackgroundResource((com.example.bottomnavpdf.R.drawable.bg_switch_off))
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        binding.LLRateUs.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(com.example.bottomnavpdf.R.layout.rateus_dialog)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(com.example.bottomnavpdf.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val btnrate =
                dialog.findViewById<AppCompatButton>(com.example.bottomnavpdf.R.id.rateusDialog_ratebtn)
            val rattting =
                dialog.findViewById<RatingBar>(com.example.bottomnavpdf.R.id.rateusDialog_rating)
            btnrate.setOnClickListener {
                if (rattting.rating > 0 && rattting.rating > 2) {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                        )
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Thanks for your rating!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            dialog.show()
        }
        binding.LLFeedback.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(getString(com.example.bottomnavpdf.R.string.my_gmail))
            )
            intent.putExtra(Intent.EXTRA_SUBJECT, "Application FeedBack")
            startActivity(Intent.createChooser(intent, "Email via..."))
        }
        binding.LLExit.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            onBackPressed()
        }
        binding.LLshareapp.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(com.example.bottomnavpdf.R.string.app_name)
                )
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage += "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (_: Exception) {
            }
        }
    }

    private fun selectPDF() {
        // Initialize intent
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        resultLauncher!!.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

 /*       val dashboard_checkbox = findViewById<AppCompatImageView>(R.id.dashboard_checkbox)
        val sort = findViewById<AppCompatImageView>(R.id.dashboard_sort)

        sort.visibility = View.VISIBLE
        dashboard_checkbox.visibility = View.INVISIBLE*/



        binding.drawercontentlayout.userSelected.setImageResource(R.drawable.selectall_icon2)
        binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)
        isfromlongclick = false
        AdapterFavouriteFiles.isfromlongclick = false
        AdapterRecentFiles.isfromlongclick = false


        if (!selectedadapter.isNullOrEmpty() && selectedadapter.equals("home")) {
            (homefragment as HomeFragment).onSelecteditemCall(false)
            binding.drawercontentlayout.toolbarmain.visibility = View.VISIBLE
            binding.drawercontentlayout.toolbarselected.visibility = View.GONE
            binding.drawercontentlayout.layoutbottomtext.visibility = View.VISIBLE
            binding.drawercontentlayout.layoutbottomselected.visibility = View.GONE
            selectedadapter = null
            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)
            return
        } else if (!selectedadapter.isNullOrEmpty() && selectedadapter.equals("recent")) {
            (recentfragment as RecentFragment).onSelecteditemCall(false)
            binding.drawercontentlayout.toolbarmain.visibility = View.VISIBLE
            binding.drawercontentlayout.toolbarselected.visibility = View.GONE
            binding.drawercontentlayout.layoutbottomtext.visibility = View.VISIBLE
            binding.drawercontentlayout.layoutbottomselected.visibility = View.GONE
            selectedadapter = null
            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)
            return
        } else if (!selectedadapter.isNullOrEmpty() && selectedadapter.equals("fvrt")) {
            (favrtfragment as FavouriteFragment).onSelecteditemCall(false)
            binding.drawercontentlayout.toolbarmain.visibility = View.VISIBLE
            binding.drawercontentlayout.toolbarselected.visibility = View.GONE
            binding.drawercontentlayout.layoutbottomtext.visibility = View.VISIBLE
            binding.drawercontentlayout.layoutbottomselected.visibility = View.GONE
            selectedadapter = null
            binding.drawercontentlayout.selectallfilesBtn.setImageResource(R.drawable.selectall_icon)
            return
        }

        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(com.example.bottomnavpdf.R.layout.exit_dialog)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(com.example.bottomnavpdf.R.color.transparent)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val ratting =
            dialog.findViewById<RatingBar>(com.example.bottomnavpdf.R.id.exitDialog_rating)
        val btnrate =
            dialog.findViewById<AppCompatButton>(com.example.bottomnavpdf.R.id.exitDialog_rateus)
        val btnexit =
            dialog.findViewById<AppCompatButton>(com.example.bottomnavpdf.R.id.exitDialog_exit)
        val btncross =
            dialog.findViewById<AppCompatImageView>(com.example.bottomnavpdf.R.id.exitDialog_cross)
        btnrate.setOnClickListener {
            if (ratting.rating > 0 && ratting.rating > 2) {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://play.google.com/store/apps/details?id="
                                    + BuildConfig.APPLICATION_ID
                        )
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Thanks for your rating!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        btnexit.setOnClickListener {
            finishAffinity()
        }
        btncross.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun setDefaultColors() {
        binding.drawercontentlayout.bottomTvHome.setCompoundDrawablesWithIntrinsicBounds(
            null, ContextCompat.getDrawable(
                this,
                com.example.bottomnavpdf.R.drawable.home_icon
            ), null, null
        )
        binding.drawercontentlayout.bottomTvRecent.setCompoundDrawablesWithIntrinsicBounds(
            null, ContextCompat.getDrawable(
                this,
                com.example.bottomnavpdf.R.drawable.recent_icon
            ), null, null
        )
        binding.drawercontentlayout.bottomTvFav.setCompoundDrawablesWithIntrinsicBounds(
            null, ContextCompat.getDrawable(
                this,
                com.example.bottomnavpdf.R.drawable.favourite_icon
            ), null, null
        )
        binding.drawercontentlayout.bottomTvTools.setCompoundDrawablesWithIntrinsicBounds(
            null, ContextCompat.getDrawable(
                this,
                com.example.bottomnavpdf.R.drawable.tools_icon
            ), null, null
        )
        binding.drawercontentlayout.dashboardSearch.visibility = View.VISIBLE
        binding.drawercontentlayout.dashboardCheckbox.visibility = View.VISIBLE
    }

    fun Setdefaultvalues(binding: LanguageDialogBinding) {
        binding.btLangEnglishCheck.visibility = View.GONE
        binding.btLangSpanishCheck.visibility = View.GONE
        binding.btLangHindiCheck.visibility = View.GONE
        binding.btLangGermanCheck.visibility = View.GONE
        binding.btLangTurkeyCheck.visibility = View.GONE
        binding.btLangFranchCheck.visibility = View.GONE
        binding.btLangJapanCheck.visibility = View.GONE
        binding.btLangArabicCheck.visibility = View.GONE
        binding.btLangChinaCheck.visibility = View.GONE

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

    fun setDefault(binding: LanguageDialogBinding, code: String) {
        when (code) {
            "es" -> {
                Setdefaultvalues(binding)
                binding.btLangSpanishCheck.visibility = View.VISIBLE
            }

            "hi" -> {
                Setdefaultvalues(binding)
                binding.btLangHindiCheck.visibility = View.VISIBLE
            }

            "de" -> {
                Setdefaultvalues(binding)
                binding.btLangGermanCheck.visibility = View.VISIBLE
            }

            "tr" -> {
                Setdefaultvalues(binding)
                binding.btLangTurkeyCheck.visibility = View.VISIBLE
            }

            "fr" -> {
                Setdefaultvalues(binding)
                binding.btLangFranchCheck.visibility = View.VISIBLE
            }

            "ja" -> {
                Setdefaultvalues(binding)
                binding.btLangJapanCheck.visibility = View.VISIBLE
            }

            "ar" -> {
                Setdefaultvalues(binding)
                binding.btLangArabicCheck.visibility = View.VISIBLE
            }

            "zh" -> {
                Setdefaultvalues(binding)
                binding.btLangChinaCheck.visibility = View.VISIBLE
            }

            else -> {
                Setdefaultvalues(binding)
                binding.btLangEnglishCheck.visibility = View.VISIBLE
            }
        }
    }
}