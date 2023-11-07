package com.example.bottomnavpdf.ui.activities


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.myCallback
import com.example.bottomnavpdf.adapter.CheckableFilesAdapter2
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivityMergePdfBinding
import com.example.bottomnavpdf.utils.Variables
import com.example.bottomnavpdf.viewmodel.LoaderViewModel
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executors


class MergePdf : AppCompatActivity() {
    private lateinit var binding: ActivityMergePdfBinding
    private val loaderViewModel: LoaderViewModel by viewModels()
    private lateinit var adapter: CheckableFilesAdapter2
    var mergedPdfFiles: ArrayList<FileItems> = ArrayList()
    var selectedPdfFilePaths = ArrayList<String>()
    private lateinit var mergeBtn: CardView

    companion object {
        var file_alreadyselected: File? = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_merge_pdf)
        mergeBtn = binding.mergeBtn

        createRecyclerview()

        mergeBtn.setOnClickListener {
            for (fileItem in mergedPdfFiles) {
                if (fileItem.isChecked) {
                    selectedPdfFilePaths.add(fileItem.pdfFilePath)
                    adapter.notifyDataSetChanged()
                }
            }

            if (selectedPdfFilePaths.size > 1) {
                val dialog = Dialog(this)
                dialog.setContentView(com.example.bottomnavpdf.R.layout.rename_dailog)
                dialog.setCancelable(true)
                dialog.window!!.setBackgroundDrawableResource(com.example.bottomnavpdf.R.color.transparent)
                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val okButton =
                    dialog.findViewById<AppCompatButton>(com.example.bottomnavpdf.R.id.okRename)
                val cancelButton =
                    dialog.findViewById<AppCompatButton>(com.example.bottomnavpdf.R.id.cancelRename)

                okButton.setOnClickListener {
                    val fileName =
                        dialog.findViewById<AppCompatEditText>(com.example.bottomnavpdf.R.id.textView6).text.toString()
                            .trim()
                    try {
                        if (fileName.isNotEmpty()) {
                            binding.lottieanim.visibility = View.VISIBLE
                            Executors.newSingleThreadExecutor().execute {
                                try {
                                    val mergerUtility = PDFMergerUtility()
                                    for (filePath in selectedPdfFilePaths) {
                                        mergerUtility.addSource(FileInputStream(File(filePath)))
                                    }
                                    val path =
                                        "${Environment.getExternalStorageDirectory()}/PdfReader/MergePDF/"
                                    val dir = File(path).apply { mkdirs() }
                                    val newname = "$fileName.pdf"
                                    val outFile = File(dir, newname)
                                    val fileOutputStream = FileOutputStream(outFile)
                                    try {
                                        mergerUtility.destinationStream = fileOutputStream
                                        mergerUtility.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
                                        // Scan the merged PDF file with the media scanner
                                        MediaScannerConnection.scanFile(
                                            this@MergePdf,
                                            arrayOf(outFile.path),
                                            null,
                                            null
                                        )
                                        fileOutputStream.close()
                                        val intent =
                                            Intent(this@MergePdf, viewerPdf::class.java).apply {
                                                putExtra("key", outFile.path)
                                            }
                                        finish()
                                        startActivity(intent)

                                    } catch (_: Exception) {
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(
                                                this,
                                                "Invalid File!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        super.onBackPressed()
                                    } finally {
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("PdfUtils", "Error merging PDF files: ${e.message}")
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(this, "Invalid File!", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    super.onBackPressed()
                                }
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(this, "Merge Completed!", Toast.LENGTH_SHORT)
                                        .show()
                                    binding.lottieanim.visibility = View.GONE
                                    Variables.DATACHANGED = true
                                }
                            }
                        } else {
                            Toast.makeText(this, "Please enter a file name.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Failed to merge PDF Files: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                }
                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()

            } else {
                Toast.makeText(
                    this,
                    "Please select at least two file to merge.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        binding.onBackPressed.setOnClickListener {
            onBackPressed()
        }
        binding.dashboardSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun createRecyclerview() {
        val resut = intent.getStringExtra("FILESELECTED").toString()
        if (resut.isNotEmpty()) {
            file_alreadyselected = File(intent.getStringExtra("FILESELECTED").toString())
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        loaderViewModel.fetchPdfFiles(0, "DESC", object : myCallback {
            override fun callbackInvoke() {
                mergedPdfFiles = loaderViewModel.fileList
                adapter =
                    CheckableFilesAdapter2(
                        mergedPdfFiles,
                        selectedPdfFilePaths,
                        binding.recyclerView
                    )
                binding.recyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun onItemClick(size: Int) {
                        if (size != 0) {
                            binding.convertBtntext.text =
                                "${getString(R.string.merge_pdf)}\t($size)"
                        } else {
                            binding.convertBtntext.text =
                                "${getString(R.string.merge_pdf)}"
                        }

                    }

                    override fun onBottomSheetItemClick(
                        mPosition: Int,
                        position: FileItems,
                        bm: Bitmap
                    ) {
                    }
                })
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
    }

}