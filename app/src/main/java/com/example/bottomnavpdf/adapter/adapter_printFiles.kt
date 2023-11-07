package com.example.bottomnavpdf.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFilesPrintBinding
import com.example.bottomnavpdf.ui.activities.print_doc_activity
import com.example.bottomnavpdf.utils.PdfRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File

class adapter_printFiles(
    var filelist: ArrayList<FileItems>,
    val listener: OnClickListener,
    val  context: print_doc_activity
) :
    RecyclerView.Adapter<ViewHolder>() {
    lateinit var binding: ItemFilesPrintBinding
    private val thumbnailCache = LruCache<String, Bitmap>(10 * 1024 * 1024)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder: ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_files_print, parent, false)
        viewHolder = LoaderViewHolder(binding, thumbnailCache)
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filelist[position]
        (holder as LoaderViewHolder).bind(currentItem, listener,context)
    }


    override fun getItemCount(): Int {
        return filelist.size
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class LoaderViewHolder(
        binding: ItemFilesPrintBinding,
        private val thumbnailCache: LruCache<String, Bitmap>
    ) : ViewHolder(binding.root) {
        private val mBinding = binding
        private val picassoInstance = Picasso.Builder(binding.root.context.applicationContext)
            .addRequestHandler(PdfRequestHandler())
            .build()
        private var coroutineJob: Job? = null

        @SuppressLint("SuspiciousIndentation")
        fun bind(mCurrentItem: FileItems, listener: OnClickListener, context: print_doc_activity) {
            try {
                Log.d("onBindViewHolder", "name\t${mCurrentItem.fileName}")
                val fileName = mCurrentItem.fileName
                val displayText = if (fileName.length > 30) {
                    "${fileName.substring(0, 15)}.....${fileName.substring(fileName.length - 15)}"
                } else {
                    fileName
                }
                mBinding.fileName.text = displayText
                mBinding.fileName.text = displayText
                mBinding.fileDate.text = mCurrentItem.dateCreatedName
                mBinding.fileSize.text = mCurrentItem.sizeName

                mBinding.item.setOnClickListener {
                    val mPosition = adapterPosition
                    listener.onItemClick(mPosition, false)
                }
                val file = File(mCurrentItem.pdfRootPath)
                mBinding.pdffileRootPath.text = file.parentFile!!.name
                if (file.parentFile!!.name.equals("0")) {
                    mBinding.pdffileRootPath.text = context.getString(R.string.rootdir)
                }

                coroutineJob?.cancel()


                val cacheKey = "${PdfRequestHandler.SCHEME_PDF}:${mCurrentItem.pdfFilePath}"
                val cachedThumbnail = thumbnailCache.get(cacheKey)

                if (cachedThumbnail != null) {
                    mBinding.fileImage.setImageBitmap(cachedThumbnail)
                } else {
                    coroutineJob = CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            val bitmap = try {
                                // Load the PDF thumbnail using PdfRequestHandler
                                picassoInstance.load(cacheKey)
                                    .resize(95, 95)
                                    .get()
                            } catch (e: Exception) {
                                null
                            }
                            bitmap
                        }?.let { bitmap ->
                            mBinding.fileImage.setImageBitmap(bitmap)
                            thumbnailCache.put(cacheKey, bitmap)
                        } ?: run {

                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}