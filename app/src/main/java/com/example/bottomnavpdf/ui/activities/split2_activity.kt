package com.example.bottomnavpdf.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnClickListener
import com.example.bottomnavpdf.adapter.adapter_splitter
import com.example.bottomnavpdf.databinding.ActivitySplit2Binding
import com.example.bottomnavpdf.utils.Variables
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class split2_activity : AppCompatActivity() {
    lateinit var document: PDDocument
    lateinit var binding: ActivitySplit2Binding
    var handlernew = Handler(Looper.getMainLooper())
    var executor = Executors.newSingleThreadExecutor()
    var bitmaplist: ArrayList<Bitmap> = ArrayList()
    var selectedbitmapist: ArrayList<Bitmap> = ArrayList()
    var pageslist: ArrayList<Int> = ArrayList()
    private lateinit var mAdapter: adapter_splitter
    var isselectall: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplit2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager = GridLayoutManager(this, 2)
        binding.recyclerviewSplit2.layoutManager = layoutManager
        val path = intent.getStringExtra("PATHHH")
        val pdfFile = File(path!!)
        ///split pdf

        binding.splitbtn.setOnClickListener {
            start_Splitting(document)
        }
        binding.appCompatImageView.setOnClickListener {
            onBackPressed()
        }
        binding.selecall.setOnClickListener {
            try {
                if (isselectall) {
                    mAdapter.selectall_items(false)
                    isselectall = false
                    selectAll(false)
                } else {
                    selectAll(true)
                    isselectall = true
                    mAdapter.selectall_items(true)
                }
            } catch (_: Exception) {
            }
        }
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.splitbtn.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                document = PDDocument.load(pdfFile)
                val pageCount = document.numberOfPages
                Log.d("PATTTTT", "path\t" + path)
                Log.d("PATTTTT", "pages\t" + pageCount)
                val uri = Uri.fromFile(File(path))
                generateImageFromPdf(uri, pageCount)
            } catch (e: Exception) {
                Toast.makeText(this@split2_activity,"Invalid File!",Toast.LENGTH_SHORT).show()
                super.onBackPressed()
            }
        }, 1000)
    }

    fun selectAll(b: Boolean) {
        for (i in 0 until bitmaplist.size) {
            if (b) {
                pageslist.add(i)
                selectedbitmapist.add(bitmaplist[i])
            } else {
                pageslist.remove(i)
                selectedbitmapist.remove(bitmaplist[i])
            }
        }
        binding.PdfReaderTitle.text =
            selectedbitmapist.size.toString() + " ${getString(R.string.selected)}"

    }

    fun start_Splitting(document: PDDocument) {
        if (selectedbitmapist.isNotEmpty()) {
            try {
                Executors.newSingleThreadExecutor().execute {
                    Log.d("PATTTTT", "selected list size\t" + selectedbitmapist.size)
                    Log.d("PATTTTT", "pages list size\t" + pageslist.size)
                    val newDocument = PDDocument()
                    for (j in 0 until pageslist.size) {
                        newDocument.addPage(document.getPage(pageslist.get(j)))
                    }
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val currentDateAndTime: String = sdf.format(Date())
                    val path = Environment.getExternalStorageDirectory()
                        .toString() + "/PdfReader/" + "SplitPDF/"
                    val dir = File(path)
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val newpath = path + currentDateAndTime
                    newDocument.save("$newpath.pdf")
                    newDocument.close()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this@split2_activity,
                            "Splitting completed",
                            Toast.LENGTH_SHORT
                        ).show()
                        val pppp = File("$newpath.pdf")
                        val intent =
                            Intent(this@split2_activity, viewerPdf::class.java).apply {
                                putExtra("key", pppp.path)
                            }
                        finish()
                        startActivity(intent)
                        Variables.DATACHANGED=true
                    }
                }
            } catch (_: Exception) {
            }
        } else {
            Toast.makeText(this, "No Page selected!", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateImageFromPdf(pdfUri: Uri, pages: Int) {
        executor.execute {
            for (i in 0..pages) {
                Log.d("PATTTTT", "page count\t" + i)
                val pdfiumCore = PdfiumCore(this)
                try {
                    val fd = contentResolver.openFileDescriptor(pdfUri, "r")
                    val pdfDocument: PdfDocument = pdfiumCore.newDocument(fd)
                    pdfiumCore.openPage(pdfDocument, i)
                    val width = pdfiumCore.getPageWidthPoint(pdfDocument, i)
                    val height = pdfiumCore.getPageHeightPoint(pdfDocument, i)
                    val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    pdfiumCore.renderPageBitmap(pdfDocument, bmp, i, 0, 0, width, height)
                    pdfiumCore.closeDocument(pdfDocument) // important!
                    bitmaplist.add(bmp)
                    Log.d("PATTTTT", "bitmap \t" + bmp.toString())
                } catch (e: Exception) {
                    //todo with exception
                }
            }
            handlernew.post {
                if (!bitmaplist.isEmpty()) {
                    mAdapter = adapter_splitter(this@split2_activity,
                        bitmaplist, object : OnClickListener {
                            @SuppressLint("SetTextI18n")
                            override fun onItemClick(position: Int, b: Boolean) {
                                Log.d("PPPPPath", "onItemClick()")
                                if (b) {
                                    pageslist.add(position)
                                    selectedbitmapist.add(bitmaplist[position])
                                    binding.PdfReaderTitle.text =
                                        selectedbitmapist.size.toString() + " ${getString(R.string.selected)}"
                                } else {
                                    pageslist.remove(position)
                                    selectedbitmapist.remove(bitmaplist[position])
                                    binding.PdfReaderTitle.text =
                                        selectedbitmapist.size.toString() + " ${getString(R.string.selected)}"
                                }
                            }
                        }
                    )
                    binding.recyclerviewSplit2.adapter = mAdapter
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.splitbtn.isEnabled = true
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        pageslist.clear()
        selectedbitmapist.clear()
        super.onBackPressed()
    }
}