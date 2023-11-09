package com.example.bottomnavpdf.ui.activities


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.audiofx.BassBoost
import android.media.audiofx.BassBoost.OnParameterChangeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import com.bumptech.glide.Glide

import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivityViewerPdfBinding
import com.example.bottomnavpdf.ui.home.HomeFragment
import com.example.bottomnavpdf.utils.FvrtFilesBook
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


class viewerPdf : AppCompatActivity() {
    var isopenfrom_explicitintent = false

    var sharedPreferences: SharedPreferences? = null
    var isVerticalMode = true
    var isNightMode = false
    var isBookMark = false
    var isPageByPage = false

    companion object {
        var uriBybrowse: Uri? = null
        var isenablednight = false
        var isenabledpagebypage = false
        var isenableswipehorizontal = false
    }

    private lateinit var binding: ActivityViewerPdfBinding
    private lateinit var pdfView: PDFView
    private lateinit var filepath: String
    private var fileName: String? = null
    private var fileDate: String? = null
    var bitmap: Bitmap? = null
    var blurredBitmap: Bitmap? = null
    private lateinit var configurator: Configurator

    fun getDate(millis: Long): String {
        val lastModDate = Date(millis)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return dateFormat.format(lastModDate)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_viewer_pdf
        )

        sharedPreferences = getSharedPreferences("viewer", MODE_PRIVATE)

        isVerticalMode = sharedPreferences?.getBoolean("isVerticalMode", true)!!
        isNightMode = sharedPreferences?.getBoolean("nightMode", false)!!
        isBookMark = sharedPreferences?.getBoolean("isBookMark", false)!!
        isPageByPage = sharedPreferences?.getBoolean("isPageByPage", false)!!



        val intent = intent
        pdfView = binding.pdfView
        val extras = intent.extras
        filepath = extras?.getString("key").toString()

        val file: File = File(filepath)

        binding.pdfName.text = file.name

        lifecycleScope.launch(Dispatchers.IO) {
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(MediaStore.Files.FileColumns.DATE_ADDED)
            val selection = MediaStore.Files.FileColumns.DATA + "=?"
            val selectionArgs = arrayOf<String>(filepath)
            try {
                var date: String? = null
                getContentResolver().query(uri, projection, selection, selectionArgs, null)
                    .use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            date =
                                getDate(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000)
                            fileDate = date

                            withContext(Dispatchers.Main) {
                                binding.date.text = fileDate
                            }
                        }
                    }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            fileName = file.name
        }


        val myfile = File(filepath)
        if (myfile.exists()) {
            configurator = pdfView.fromFile(myfile)

            configurator
                .enableAntialiasing(true)
                .swipeHorizontal(!isVerticalMode)
                .onPageChange { page, pageCount ->
//                    lifecycleScope.launch(Dispatchers.IO){
//                        delay(300)
//                        setBgBlur()
//                    }
                }

            if(isPageByPage) {
                configurator.pageFling(isPageByPage)
                    .pageSnap(true)
                    .autoSpacing(true)
            }

            configurator.nightMode(isNightMode)
                .load()

        }
        else {
            if (uriBybrowse != null) {

                configurator = pdfView.fromUri(uriBybrowse)
                configurator
                    .enableAntialiasing(true)
                    .swipeHorizontal(!isVerticalMode)

                if(isPageByPage) {
                    configurator.pageFling(isPageByPage)
                        .pageSnap(true)
                        .autoSpacing(true)
                }

                configurator.nightMode(isNightMode)
                    .load()

            } else if (intent.data != null) {
                val data = intent.data

                configurator = pdfView.fromUri(data)

                configurator
                    .enableAntialiasing(true)
                    .swipeHorizontal(!isVerticalMode)

                if(isPageByPage) {
                    configurator.pageFling(isPageByPage)
                        .pageSnap(true)
                        .autoSpacing(true)
                }

                configurator.nightMode(isNightMode)
                    .load()

                isopenfrom_explicitintent = true
                uriBybrowse = data
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_page_number -> {
                    PageNumber()
                    true
                }

                R.id.navigation_viewmode -> {
                    ViewMood(myfile)
                    true
                }

                R.id.navigation_share_preview -> {
                    sharePdf(myfile)
                    true
                }

                else -> false
            }
        }

        binding.editBottomNavViewer.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_edit_copy -> {
                    // save the changes made to the PDF file

                    true
                }

                R.id.navigation_edit_break -> {
                    // delete the PDF file

                    true
                }

                R.id.navigation_edit_underlined -> {
                    // delete the PDF file

                    true
                }

                R.id.navigation_edit_cut -> {
                    // delete the PDF file

                    true
                }

                R.id.navigation_edit_write -> {
                    // edit the PDF file

                    true
                }

                else -> false
            }
        }

//        binding.container.outlineProvider = object : ViewOutlineProvider() {
//            override fun getOutline(p0: View?, p1: Outline?) {
//                p1?.setRect(0, -10, p0!!.getWidth(), p0!!.getHeight())
//            }
//
//        }

        val id = FvrtFilesBook.allfvrtFileList?.let { it.size + 1 } ?: 1
        val fileItems = FileItems(
            id = id,
            pdfFilePath = file.absolutePath,
            fileName = file.name,
            pdfRootPath = filepath,
            dateCreatedName = fileDate.toString(),
            dateModifiedName = fileDate.toString(),
            sizeName = "dummy_value",
            originalDateCreated = 1211,
            originalDateModified = 1211,
            originalSize = file.length()
        )

        binding.popupImgBtn.setOnClickListener {

            setBgBlur()

            val bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_pdf_show, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            val pageByPage = bottomSheetView.findViewById<TextView>(R.id.pageByPageRow)
            val nightSwitchBtn =
                bottomSheetView.findViewById<SwitchCompat>(R.id.screenon_switch_btn)

            val rename = bottomSheetView.findViewById<TextView>(R.id.rename_row)
            val goto = bottomSheetView.findViewById<TextView>(R.id.gotoPageRow)
            val bookMark = bottomSheetView.findViewById<TextView>(R.id.bookMarkRow)
            val scrollRow = bottomSheetView.findViewById<TextView>(R.id.scrollRow)
            val shareRow = bottomSheetView.findViewById<TextView>(R.id.shareRow)
            val thumbNail = bottomSheetView.findViewById<ImageView>(R.id.imageView3)


            Glide.with(this).load(bitmap).into(thumbNail)

            if(isPageByPage){
                pageByPage.text = "Page by page"
            }else{
                pageByPage.text = "Continuous"
            }

            if (isVerticalMode) {
                scrollRow.text = "Vertical View"
            } else {
                scrollRow.text = "Horizontal View"
            }

            nightSwitchBtn.isChecked = isNightMode

            if (isNightMode) {
                nightSwitchBtn.setBackgroundResource((R.drawable.bg_switch_on))
//                scrollRow.text  = "Vertical View"
            } else {
//                scrollRow.text  = "Horizontal View"
                nightSwitchBtn.setBackgroundResource((R.drawable.bg_switch_off))
            }

            if (isBookMark) {

            } else {

            }


            nightSwitchBtn.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {

                    setNightBottom()
                    nightSwitchBtn.setBackgroundResource((R.drawable.bg_switch_on))
                    val edit = sharedPreferences?.edit()
                    edit?.putBoolean("nightMode", true)?.apply()
                    isNightMode = true

                    reload()

                    lifecycleScope.launch(Dispatchers.IO){
                        delay(500)
                        setBgBlur()
                    }

                } else {
                    setLightBottom()
                    nightSwitchBtn.setBackgroundResource((R.drawable.bg_switch_off))
                    val edit = sharedPreferences?.edit()
                    edit?.putBoolean("nightMode", false)?.apply()
                    isNightMode = false

                    reload()

                    lifecycleScope.launch(Dispatchers.IO){
                        delay(500)
                        setBgBlur()
                    }
                }
            }

            var isPageByPageClick = !isPageByPage

            pageByPage.setOnClickListener {


                if(isPageByPageClick){
                    isPageByPageClick = false
                    sharedPreferences?.edit()?.putBoolean("isPageByPage",true)?.apply()
                    isPageByPage = true
                }else{
                    isPageByPageClick = true
                    sharedPreferences?.edit()?.putBoolean("isPageByPage",false)?.apply()
                    isPageByPage = false
                }

                if(isPageByPage){
                    pageByPage.text = "Page by page"
                }else{
                    pageByPage.text = "Continuous"
                }

                reload()
            }

            rename.setOnClickListener {

            }
            goto.setOnClickListener {
                PageNumber()
            }





            bookMark.setOnClickListener {

                if (!FvrtFilesBook.allfvrtFileList?.contains(fileItems)!!) {
                    FvrtFilesBook.allfvrtFileList?.add(fileItems)
                    savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, true)
                    sharedPreferences?.edit()?.putBoolean("isBookMark", true)?.apply()
                    isBookMark = true
                } else {
                    FvrtFilesBook.allfvrtFileList?.remove(fileItems)
                    savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, false)
                    sharedPreferences?.edit()?.putBoolean("isBookMark", false)?.apply()
                    isBookMark = false
                    Log.i("menu", "onMenuItemClick: file already in recent")
                }
            }

            var scrollClick = !isVerticalMode

            scrollRow.setOnClickListener {
                val edit = sharedPreferences?.edit()
                val type = sharedPreferences?.getBoolean("isVerticalMode", true)

                if (scrollClick) {
                    scrollClick = false
                    scrollRow.text = "Vertical View"
                    edit?.putBoolean("isVerticalMode", true)?.apply()
                    isVerticalMode = true

                    reload()

                } else {
                    scrollClick = true

                    scrollRow.text = "Horizontal View"
                    edit?.putBoolean("isVerticalMode", false)?.apply()
                    isVerticalMode = false

                    reload()
                }
            }
            shareRow.setOnClickListener {

            }

            bottomSheetDialog.setOnDismissListener {
                binding.blurredBitmap.setBackgroundColor(Color.TRANSPARENT)
                isVerticalMode = sharedPreferences?.getBoolean("isVerticalMode", true)!!
                isNightMode = sharedPreferences?.getBoolean("nightMode", false)!!
                isBookMark = sharedPreferences?.getBoolean("isBookMark", false)!!
            }

            bottomSheetDialog.show()

        }

        if(isNightMode)
            setNightBottom()
        else
            setLightBottom()

        lifecycleScope.launch(Dispatchers.IO) {


            bitmap = try {
                HomeFragment.generateThumbnail(File(filepath))!!
            } catch (e: Exception) {
                null
            }

            withContext(Dispatchers.Main){
                binding.pdfName.text = fileName
            }

            if(isNightMode){


                delay(500)
                val originalBitmap = binding.pdfView.drawToBitmap(Bitmap.Config.ARGB_8888)
                originalBitmap?.let {
                    withContext(Dispatchers.Main) {
                        val d: Drawable = BitmapDrawable(resources, it)
                        Glide.with(this@viewerPdf).load(d).into(binding.thumbNail)

//                    binding.date.text = fileDate
                    }
                }
            }else{
                bitmap?.let {
                    withContext(Dispatchers.Main) {
                        val d: Drawable = BitmapDrawable(resources, it)
                        Glide.with(this@viewerPdf).load(d).into(binding.thumbNail)
                        binding.pdfName.text = fileName
//                    binding.date.text = fileDate
                    }
                }
            }

        }


    }

    fun setNightBottom(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.container.outlineAmbientShadowColor = Color.WHITE
            binding.container.outlineSpotShadowColor = Color.WHITE
        }


        binding.container.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(p0: View?, p1: Outline?) {
                p1?.setRect(0, -50, p0!!.width, p0.height)
            }
        }

        binding.container.cardElevation = 20f
        binding.pdfName.setTextColor(Color.WHITE)
        binding.date.setTextColor(Color.WHITE)
        binding.bottomContainer.setBackgroundColor(Color.GRAY)
    }

    fun setLightBottom(){
        binding.container.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(p0: View?, p1: Outline?) {
                p1?.setRect(0, -10, p0!!.getWidth(), p0!!.getHeight())
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.container.outlineAmbientShadowColor = Color.BLACK
            binding.container.outlineSpotShadowColor = Color.BLACK
        }
        binding.container.cardElevation = 50f
        binding.pdfName.setTextColor(Color.BLACK)
        binding.date.setTextColor(Color.parseColor("#798699"))
        binding.bottomContainer.setBackgroundColor(Color.WHITE)


    }

    fun setBgBlur(){
        lifecycleScope.launch(Dispatchers.Main) {


            withContext(Dispatchers.IO){
                val originalBitmap = binding.pdfView.drawToBitmap(Bitmap.Config.ARGB_8888)
                blurredBitmap = BlurBuilder.blur(this@viewerPdf, originalBitmap!!)

                val d: Drawable = BitmapDrawable(resources, blurredBitmap)
                withContext(Dispatchers.Main){
                    Glide.with(this@viewerPdf).load(d).into(binding.thumbNail)
                }
            }
        }
    }

    private fun savefavourteFilesList(fvrtFilesList: List<FileItems>, b: Boolean) {

        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        val gson = Gson()
        val json = gson.toJson(fvrtFilesList)
        editor.putString("favourite_files_list", json)
        editor.apply()
        if (b) {
            Toast.makeText(this, "File added in favourite list.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this,
                "File removed from favourite list.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun reload(){
        val myfile = File(filepath)
        if (myfile.exists()) {
            configurator = pdfView.fromFile(myfile)

            configurator
                .enableAntialiasing(true)
                .swipeHorizontal(!isVerticalMode)

            if(isPageByPage) {
                configurator.pageFling(isPageByPage)
                    .pageSnap(true)
                    .autoSpacing(true)
            }

            configurator.nightMode(isNightMode)
                .load()

        }
        else {
            if (uriBybrowse != null) {

                configurator = pdfView.fromUri(uriBybrowse)
                configurator
                    .enableAntialiasing(true)
                    .swipeHorizontal(!isVerticalMode)

                if(isPageByPage) {
                    configurator.pageFling(isPageByPage)
                        .pageSnap(true)
                        .autoSpacing(true)
                }

                configurator.nightMode(isNightMode)
                    .load()

            } else if (intent.data != null) {
                val data = intent.data

                configurator = pdfView.fromUri(data)

                configurator
                    .enableAntialiasing(true)
                    .swipeHorizontal(!isVerticalMode)

                if(isPageByPage) {
                    configurator.pageFling(isPageByPage)
                        .pageSnap(true)
                        .autoSpacing(true)
                }

                configurator.nightMode(isNightMode)
                    .load()

                isopenfrom_explicitintent = true
                uriBybrowse = data
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()

//        binding.pdfView.drawingCache





    }

    private fun PageNumber() {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.go_to_page_dailog)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val pageNumberEditText =
                dialog.findViewById<AppCompatEditText>(R.id.textView6)
            val okButton = dialog.findViewById<LinearLayoutCompat>(R.id.ok)
            val cancelButton =
                dialog.findViewById<LinearLayoutCompat>(R.id.cancel)

            okButton.setOnClickListener {
                val pageNumber: Int
                val pageNumberString = pageNumberEditText.text.toString()

                if (pageNumberString.isNotEmpty()) {
                    pageNumber = try {
                        pageNumberString.toInt()
                    } catch (_: NumberFormatException) {
                        0
                    }
                    if (pageNumber in 0..pdfView.pageCount) {
                        pdfView.jumpTo(pageNumber - 1)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Please enter a valid page number", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a page number", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (_: Exception) {
        }
    }

    private fun ViewMood(file: File) {
        val myuri: Uri
        if (file.exists()) {
            myuri = Uri.fromFile(File(file.absolutePath))
        } else {
            myuri = uriBybrowse!!
        }

        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_view_mode, null)
        bottomSheetDialog.setContentView(view)
        val horizontalViewMode = view.findViewById<CardView>(R.id.horizantalviewmode)
        val horizontalViewModebg = view.findViewById<ConstraintLayout>(R.id.horizantalviewmodeclick)
        val verticalViewMode = view.findViewById<CardView>(R.id.verticalviewmode)
        val verticalViewModebg = view.findViewById<ConstraintLayout>(R.id.verticalviewmodeclick)
        val pageByPageSwitch = view.findViewById<SwitchCompat>(R.id.pagebypage)
        val nightModeSwitch = view.findViewById<SwitchCompat>(R.id.nightmode)
        val imagvertical = view.findViewById<ImageView>(R.id.imageView6)
        val imaghorizotal = view.findViewById<ImageView>(R.id.imageView5)
        val textView_ver = view.findViewById<TextView>(R.id.tv_vertical)
        val textView_hor = view.findViewById<TextView>(R.id.tv_horizental)
        if (isenableswipehorizontal) {
            horizontalViewModebg.setBackgroundResource(R.drawable.curve_sharp)
            verticalViewModebg.setBackgroundColor(getColor(R.color.white))
            imaghorizotal.setImageResource(R.drawable.horizantalimg2)
            textView_hor.setTextColor(getColor(R.color.white))
            imagvertical.setImageResource(R.drawable.verticalimg)
            textView_ver.setTextColor(getColor(R.color.black))
        } else {
            horizontalViewModebg.setBackgroundColor(getColor(R.color.white))
            verticalViewModebg.setBackgroundResource(R.drawable.curve_sharp)
            imaghorizotal.setImageResource(R.drawable.horizantalimg)
            textView_hor.setTextColor(getColor(R.color.black))
            imagvertical.setImageResource(R.drawable.verticalimg2)
            textView_ver.setTextColor(getColor(R.color.white))
        }
        horizontalViewMode.setOnClickListener {
            isenableswipehorizontal = true
            bottomSheetDialog.dismiss()
            pdfView.fromUri(myuri)
                .swipeHorizontal(true)
                .enableSwipe(true)
                .load()
        }

        verticalViewMode.setOnClickListener {
            isenableswipehorizontal = false
            bottomSheetDialog.dismiss()
            pdfView.fromUri(myuri)
                .swipeHorizontal(false)
                .load()
        }
        if (isenabledpagebypage) {
            pageByPageSwitch.isChecked = true
        }
        pageByPageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pageByPageSwitch.setBackgroundResource((R.drawable.bg_switch_on))
                pdfView.fromUri(myuri)
                    .pageFling(true)
                    .load()
                isenabledpagebypage = true
            } else {
                pageByPageSwitch.setBackgroundResource((R.drawable.bg_switch_off))
                pdfView.fromUri(myuri)
                    .pageFling(false)
                    .load()
                isenabledpagebypage = false
            }
        }
        if (isenablednight) {
            nightModeSwitch.isChecked = true
        }
        nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nightModeSwitch.setBackgroundResource((R.drawable.bg_switch_on))
                pdfView.fromUri(myuri)
                    .nightMode(true)
                    .load()
                isenablednight = true
            } else {
                nightModeSwitch.setBackgroundResource((R.drawable.bg_switch_off))
                pdfView.fromUri(myuri)
                    .nightMode(false)
                    .load()
                isenablednight = false
            }
        }

        bottomSheetDialog.show()
    }


    private fun sharePdf(file: File) {
        try {// save the changes made to the PDF file
            val uriii: Uri
            if (file.exists()) {
                uriii = FileProvider.getUriForFile(
                    this,
                    "$packageName.provider",
                    File(file.absolutePath)
                )
            } else {
                uriii = uriBybrowse!!
            }
            val intent = Intent()
            intent.action = "android.intent.action.SEND"
            intent.type = "application/pdf"
            intent.putExtra("android.intent.extra.STREAM", uriii)
            intent.putExtra("android.intent.extra.TEXT", "")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val createChooser = Intent.createChooser(intent, null as CharSequence?)
            startActivity(createChooser)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Something went wrong can't share app please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isopenfrom_explicitintent) {
            val intent = Intent(this@viewerPdf, MainActivity::class.java)
            startActivity(intent)
            isopenfrom_explicitintent = false
            finish()
        } else {
            super.onBackPressed()
        }
        uriBybrowse = null
    }

    object BlurBuilder {
        private const val BITMAP_SCALE = 2f
        private const val BLUR_RADIUS = 25F
        fun blur(context: Context?, image: Bitmap): Bitmap {
            val width = (image.width * BITMAP_SCALE).roundToInt()
            val height = (image.height * BITMAP_SCALE).roundToInt()
            val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
            val outputBitmap = Bitmap.createBitmap(inputBitmap)
            val rs: RenderScript = RenderScript.create(context)
            val theIntrinsic: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            val tmpIn: Allocation = Allocation.createFromBitmap(rs, inputBitmap)
            val tmpOut: Allocation = Allocation.createFromBitmap(rs, outputBitmap)
            theIntrinsic.setRadius(BLUR_RADIUS)
            theIntrinsic.setInput(tmpIn)
            theIntrinsic.forEach(tmpOut)
            tmpOut.copyTo(outputBitmap)
            return outputBitmap
        }
    }
}





