package com.example.bottomnavpdf.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavpdf.`interface`.OnClickListener
import com.example.bottomnavpdf.`interface`.myCallback
import com.example.bottomnavpdf.adapter.adapter_printFiles
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivityPrintDocBinding
import com.example.bottomnavpdf.viewmodel.LoaderViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class print_doc_activity : AppCompatActivity() {
    private lateinit var loaderViewModel: LoaderViewModel
    private lateinit var binding: ActivityPrintDocBinding
    private lateinit var mAdapter: adapter_printFiles
    var originalFileList: ArrayList<FileItems> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrintDocBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loaderViewModel = ViewModelProvider(this).get(LoaderViewModel::class.java)
        getFiles(0, "DESC")
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerviewSelectedactivity.layoutManager = layoutManager
        binding.appCompatImageView.setOnClickListener {
            onBackPressed()
        }
        binding.dashboardSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun printDocument(uri: Uri) {
        try {
            val adapter: PrintDocumentAdapter = object : PrintDocumentAdapter() {
                override fun onWrite(
                    pages: Array<PageRange?>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                   try {
                        contentResolver.openInputStream(uri).use { inputStream ->
                            FileOutputStream(destination.fileDescriptor).use { outputStream ->
                                val buffer = ByteArray(1024)
                                var bytesRead: Int
                                while (inputStream!!.read(buffer).also { bytesRead = it } > 0) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }
                                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        callback.onWriteFailed(null)
                    }
                }

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    // Inform the system about the print layout
                    if (cancellationSignal.isCanceled()) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("document_name")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback.onLayoutFinished(info, true)
                }
            }
            // Create the print job and start the printing activity
            val printManager = getSystemService(PRINT_SERVICE) as PrintManager
            val printJob = printManager.print("document_name", adapter, null)
            if (printJob.isFailed) {
                // Handle print job failure
            } else {
                // Printing started successfully
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Please select file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFiles(type: Int, order: String) {
        loaderViewModel.fetchPdfFiles(type, order,object : myCallback {
            override fun callbackInvoke() {
                submitList()
            }
        })
        loaderViewModel.fetchFolders {
        }
    }

    private fun submitList() {
        try {
            if (!loaderViewModel.isFilesListEmpty()) {
                binding.loadingProgressBar.visibility = View.GONE
                originalFileList = loaderViewModel.fileList
                mAdapter = adapter_printFiles(
                    originalFileList,
                    object : OnClickListener {
                        override fun onItemClick(position: Int, b: Boolean) {
                            Log.d("PPPPPath",originalFileList[position].pdfFilePath)
                            val uri = Uri.fromFile(File(originalFileList[position].pdfFilePath))
                            printDocument(uri)
                        }

                    },
                    this@print_doc_activity
                )
                binding.recyclerviewSelectedactivity.adapter = mAdapter

            } else {
                noList()
            }
        } catch (_: Exception) {
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
