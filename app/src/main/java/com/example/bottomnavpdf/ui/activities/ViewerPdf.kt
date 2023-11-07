package com.example.bottomnavpdf.ui.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import androidx.databinding.DataBindingUtil
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.databinding.ActivityViewerPdfBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File


class viewerPdf : AppCompatActivity() {
    var isopenfrom_explicitintent = false

    companion object {
        var uriBybrowse: Uri? = null
        var isenablednight = false
        var isenabledpagebypage = false
        var isenableswipehorizontal = false
    }

    private lateinit var binding: ActivityViewerPdfBinding
    private lateinit var pdfView: PDFView
    private lateinit var filepath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            com.example.bottomnavpdf.R.layout.activity_viewer_pdf
        )

        val intent = intent
        pdfView = binding.pdfView
        val extras = intent.extras
        filepath = extras?.getString("key").toString()
        val myfile = File(filepath)
        if (myfile.exists()) {
            pdfView.fromFile(myfile).enableAntialiasing(true).load()
        } else {
            if (uriBybrowse != null) {
                pdfView.fromUri(uriBybrowse).enableAntialiasing(true).load()
            } else if (intent.data != null) {
                val data = intent.data
                pdfView.fromUri(data).enableAntialiasing(true).load()
                isopenfrom_explicitintent = true
                uriBybrowse = data
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                com.example.bottomnavpdf.R.id.navigation_page_number -> {
                    PageNumber()
                    true
                }

                com.example.bottomnavpdf.R.id.navigation_viewmode -> {
                    ViewMood(myfile)
                    true
                }

                com.example.bottomnavpdf.R.id.navigation_share_preview -> {
                    sharePdf(myfile)
                    true
                }

                else -> false
            }
        }

        binding.editBottomNavViewer.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                com.example.bottomnavpdf.R.id.navigation_edit_copy -> {
                    // save the changes made to the PDF file

                    true
                }

                com.example.bottomnavpdf.R.id.navigation_edit_break -> {
                    // delete the PDF file

                    true
                }

                com.example.bottomnavpdf.R.id.navigation_edit_underlined -> {
                    // delete the PDF file

                    true
                }

                com.example.bottomnavpdf.R.id.navigation_edit_cut -> {
                    // delete the PDF file

                    true
                }

                com.example.bottomnavpdf.R.id.navigation_edit_write -> {
                    // edit the PDF file

                    true
                }

                else -> false
            }
        }
    }

    private fun PageNumber() {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(com.example.bottomnavpdf.R.layout.go_to_page_dailog)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(com.example.bottomnavpdf.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val pageNumberEditText =
                dialog.findViewById<AppCompatEditText>(com.example.bottomnavpdf.R.id.textView6)
            val okButton = dialog.findViewById<LinearLayoutCompat>(com.example.bottomnavpdf.R.id.ok)
            val cancelButton =
                dialog.findViewById<LinearLayoutCompat>(com.example.bottomnavpdf.R.id.cancel)

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
                        Toast.makeText(this, "Please enter a valid page number", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Something went wrong can't share app please try again.", Toast.LENGTH_SHORT).show()
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
}





