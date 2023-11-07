package com.example.bottomnavpdf.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnClickListener
import com.example.bottomnavpdf.adapter.adapter_images_to_pdf
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivityImagesToPdfBinding
import com.example.bottomnavpdf.utils.Variables
import com.example.bottomnavpdf.viewmodel.LoaderViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors


class Images_to_pdf_Activity : AppCompatActivity() {
    var handlernew = Handler(Looper.getMainLooper())
    var executor = Executors.newSingleThreadExecutor()

    private lateinit var loaderViewModel: LoaderViewModel
    private lateinit var binding: ActivityImagesToPdfBinding
    private lateinit var mAdapter: adapter_images_to_pdf
    var originalFileList: ArrayList<FileItems> = ArrayList()
    var selectedFileList: ArrayList<FileItems> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagesToPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loaderViewModel = ViewModelProvider(this).get(LoaderViewModel::class.java)
        getFiles()
        val layoutManager = GridLayoutManager(this, 2)
        binding.recyclerviewSelectedactivity.layoutManager = layoutManager
        binding.appCompatImageView.setOnClickListener {
            onBackPressed()
        }
        binding.convertBtn.setOnClickListener {
            start_converting()
        }

    }

    fun start_converting() {
        if (selectedFileList.isNotEmpty()) {
            binding.lottieanimcard.visibility = View.VISIBLE
            convertToPdf {
            }
        } else {
            Toast.makeText(this, "No images selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFiles() {
        originalFileList.clear()
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val file =
                    (File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))))
                originalFileList.add(
                    FileItems(
                        id = 122,
                        pdfFilePath = file.absolutePath,
                        fileName = file.name,
                        pdfRootPath = "dummy_value",
                        dateCreatedName = "dummy_value",
                        dateModifiedName = "dummy_value",
                        sizeName = "dummy_value",
                        originalDateCreated = 1211,
                        originalDateModified = 1211,
                        originalSize = 1211
                    )
                )
            }
            if (!originalFileList.isEmpty()) {
                mAdapter = adapter_images_to_pdf(this@Images_to_pdf_Activity,
                    originalFileList, object : OnClickListener {
                        @SuppressLint("SetTextI18n")
                        override fun onItemClick(position: Int, b: Boolean) {
                            if (b) {
                                selectedFileList.add(originalFileList[position])
                                binding.convertBtntext.text = "${getString(R.string.convert)}\t(${selectedFileList.size})"
                            } else {
                                selectedFileList.remove(originalFileList[position])
                                if (selectedFileList.size == 0) {
                                    binding.convertBtntext.text = getString(R.string.convert).toString()
                                } else {
                                    binding.convertBtntext.text = "${getString(R.string.convert)}\t(${selectedFileList.size})"
                                }
                            }
                            Log.d("PPPPPath", originalFileList[position].pdfFilePath)
                        }
                    }
                )
                binding.recyclerviewSelectedactivity.adapter = mAdapter
                binding.loadingProgressBar.visibility = View.GONE
            } else {
                noList()
            }
        }
        cursor!!.close()
    }
    private fun convertToPdf(callback: () -> Unit) {
        try {
            //Run Background task
            executor.execute {
                val document = PdfDocument()
                for (i in selectedFileList.indices) {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        Uri.fromFile(File(selectedFileList.get(i).pdfFilePath))
                    )
                    val pageInfo =
                        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, i + 1).create()
                    val page = document.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    document.finishPage(page)
                }

                val folderName = "Images_to_PDF"
                val storageDir = Environment.getExternalStorageDirectory().absolutePath+"/PdfReader"
                val directory = File(storageDir, folderName)
                val timestamp = System.currentTimeMillis()
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, "images_$timestamp.pdf")
                val outputStream = FileOutputStream(file)
                document.writeTo(outputStream)
                callback.invoke()
                Log.i("PDF", "PDF created successfully!")
                document.close()
                val intent =
                    Intent(this@Images_to_pdf_Activity, viewerPdf::class.java).apply {
                        putExtra("key", file.path)
                    }
                finish()
                startActivity(intent)
                Variables.DATACHANGED=true
                //Update UI
                handlernew.post {
                    binding.lottieanimcard.visibility = View.GONE
                    Toast.makeText(this, "PDF created successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun noList() {
        binding.loadingProgressBar.visibility = View.GONE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}