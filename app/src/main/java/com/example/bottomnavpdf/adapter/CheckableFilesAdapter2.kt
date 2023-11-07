package com.example.bottomnavpdf.adapter

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFileCheckableBinding
import com.example.bottomnavpdf.ui.activities.MergePdf
import com.example.bottomnavpdf.utils.PdfRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*

class CheckableFilesAdapter2(
    val fileListMerge: ArrayList<FileItems>,
    val selectedPdfFilePaths: ArrayList<String>,
    val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mListener: OnItemClickListener? = null
    private val thumbnailCache = LruCache<String, Bitmap>(10 * 1024 * 1024)
    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ItemFileCheckableBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_file_checkable, parent, false
        )
        viewHolder = MergeFileViewHolder(binding, mListener!!, thumbnailCache)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = fileListMerge.get(position)
        val viewHolder = holder as MergeFileViewHolder
        viewHolder.bind(currentItem)
    }

    inner class MergeFileViewHolder(
        binding: ItemFileCheckableBinding,
        listener: OnItemClickListener,
        private val thumbnailCache: LruCache<String, Bitmap>
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding
        private val picassoInstance = Picasso.Builder(binding.root.context.applicationContext)
            .addRequestHandler(PdfRequestHandler())
            .build()
        private var coroutineJob: Job? = null

        init {
            binding.item.setOnClickListener {
//                val mPosition = adapterPosition
                if (!binding.checkbox.isChecked) {
                    selectedPdfFilePaths.add(fileListMerge.get(position).pdfFilePath)
                    binding.checkbox.isChecked=true
                    listener.onItemClick(selectedPdfFilePaths.size)
                } else {
                    selectedPdfFilePaths.remove(fileListMerge.get(adapterPosition).pdfFilePath)
                    binding.checkbox.isChecked=false
                    listener.onItemClick(selectedPdfFilePaths.size)
                }
            }
//            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    selectedPdfFilePaths.add(fileListMerge.get(position).pdfFilePath)
//                } else {
//                    selectedPdfFilePaths.remove(fileListMerge.get(adapterPosition).pdfFilePath)
//
//                }
//            }
        }

        fun bind(mCurrentItem: FileItems) {
            mBinding.fileName.text = mCurrentItem.fileName
            mBinding.fileDate.text = mCurrentItem.dateCreatedName
            mBinding.fileSize.text = mCurrentItem.sizeName
            mBinding.checkbox.isChecked = mCurrentItem.isChecked
            mBinding.pdffileRootPath.text = mCurrentItem.pdfRootPath
            coroutineJob?.cancel()

            // Load the bitmap asynchronously using a coroutine
            // Check if the thumbnail is present in the cache
            val cacheKey = "${PdfRequestHandler.SCHEME_PDF}:${mCurrentItem.pdfFilePath}"
            val cachedThumbnail = thumbnailCache.get(cacheKey)

            if (cachedThumbnail != null) {
                // If the thumbnail is present in the cache, set it to the ImageView
                mBinding.fileImage.setImageBitmap(cachedThumbnail)
            } else {
                // If the thumbnail is not present in the cache, load it asynchronously using a coroutine
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
                        // Set the bitmap to the ImageView
                        mBinding.fileImage.setImageBitmap(bitmap)
                        // Add the thumbnail to the cache
                        thumbnailCache.put(cacheKey, bitmap)
                    } ?: run {
                        // If bitmap is null, set a placeholder image
                        //  mBinding.fileImage.setImageResource(R.drawable.pdf_icon)
                    }
                }
            }

            if (MergePdf.file_alreadyselected != null && mCurrentItem.pdfFilePath == MergePdf.file_alreadyselected!!.absolutePath
            ) {
                Log.d("SPLITAUTO", "position\t" +adapterPosition)
                selectedPdfFilePaths.add(fileListMerge[adapterPosition].pdfFilePath)
                mBinding.checkbox.isChecked = true
                MergePdf.file_alreadyselected=null
            } else {
                Log.d("SPLITAUTO", "file was null")
                mBinding.checkbox.isChecked = false
            }
        }
    }

    override fun getItemCount(): Int {
        return fileListMerge.size
    }
}