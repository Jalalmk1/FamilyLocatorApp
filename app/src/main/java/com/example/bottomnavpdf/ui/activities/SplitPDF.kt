package com.example.bottomnavpdf.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.`interface`.myCallback
import com.example.bottomnavpdf.adapter.AdapterFilesLoader2
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivitySplitPdfBinding
import com.example.bottomnavpdf.viewmodel.LoaderViewModel
import java.io.File
import java.util.*


class SplitPDF : AppCompatActivity() {
    companion object {
        var file_alreadyselected: File? = null
        var filesListSplitview: List<FileItems> = ArrayList()
    }

    private lateinit var binding: ActivitySplitPdfBinding
    private lateinit var mAdapter: AdapterFilesLoader2
    private lateinit var loaderViewModel: LoaderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_split_pdf)
        initViewModel()
        createLoaderRecyclerView()
        binding.onBackPressed.setOnClickListener {
            onBackPressed()
        }
        binding.dashboardSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun initViewModel() {
        loaderViewModel = ViewModelProvider(this).get(LoaderViewModel::class.java)
    }

    private fun createLoaderRecyclerView() {
        val resut = intent.getStringExtra("FILESELECTED").toString()
        if (resut.isNotEmpty()&& resut != "") {
//            file_alreadyselected = File(intent.getStringExtra("FILESELECTED").toString())
            startActivity(Intent(this@SplitPDF,split2_activity::class.java)
                .putExtra("PATHHH", resut))
        }
        loaderViewModel.fetchPdfFiles(0, "DESC",object :myCallback{
            override fun callbackInvoke() {
                if (!loaderViewModel.isFilesListEmpty()) {
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.noFilesLayout.visibility = View.GONE
                    filesListSplitview = loaderViewModel.fileList
                    mAdapter = AdapterFilesLoader2(this@SplitPDF, filesListSplitview)
                    binding.recyclerview.adapter = mAdapter
                    binding.recyclerview.layoutManager = LinearLayoutManager(this@SplitPDF)
                    mAdapter.setOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            startActivity(Intent(this@SplitPDF,split2_activity::class.java)
                                .putExtra("PATHHH", filesListSplitview[position].pdfFilePath))
                            Log.d("SPLITAUTO","clicked\t"+filesListSplitview[position].pdfFilePath)
                        }

                        override fun onBottomSheetItemClick(
                            mPosition: Int,
                            position: FileItems,
                            bm: Bitmap
                        ) {
                        }
                    })
                    mAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
                        override fun onItemLongClick(position: Int) {
                        }
                    })
                } else {
                    noList()
                }
            }
        })
    }
    private fun noList() {
        binding.loadingProgressBar.visibility = View.GONE
        binding.noFilesLayout.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        filesListSplitview = emptyList()
        super.onBackPressed()
    }
}


