package com.example.bottomnavpdf.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFilesBinding
import com.example.bottomnavpdf.ui.activities.SplitPDF
import com.example.bottomnavpdf.utils.PdfRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File

class AdapterFilesLoader2(val context: Context, val fileList: List<FileItems> = ArrayList()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mListener: OnItemClickListener? = null
    private val thumbnailCache = LruCache<String, Bitmap>(10 * 1024 * 1024)
    private var mLongClickListener: OnItemLongClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ItemFilesBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_files, parent, false
        )
        viewHolder = LoaderViewHolder(
            binding,
            mListener!!,
            mLongClickListener!!,
            thumbnailCache
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = fileList[position]
        (holder as LoaderViewHolder).bind(currentItem, mListener, position,context)
    }

    class LoaderViewHolder(
        binding: ItemFilesBinding,
        listener: OnItemClickListener,
        longClickListener: OnItemLongClickListener,
        private val thumbnailCache: LruCache<String, Bitmap>,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding

        private val picassoInstance = Picasso.Builder(binding.root.context.applicationContext)
            .addRequestHandler(PdfRequestHandler())
            .build()
        private var coroutineJob: Job? = null

        init {
            binding.popupImgBtn.visibility = View.GONE

            binding.item.setOnClickListener {
                val mPosition = adapterPosition
                listener.onItemClick(mPosition)
                Log.d("SPLITAUTO", "position\t" + adapterPosition)
            }
            binding.item.setOnLongClickListener {
                val mPosition = adapterPosition
                longClickListener.onItemLongClick(mPosition)
                true
            }

        }

        fun bind(
            mCurrentItem: FileItems,
            mListener: OnItemClickListener?,
            position: Int,
            context: Context
        ) {
            try {
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
                if (SplitPDF.file_alreadyselected != null && mCurrentItem.pdfFilePath == SplitPDF.file_alreadyselected!!.absolutePath) {
                    Log.d(
                        "SPLITAUTO",
                        "perform click\t" + SplitPDF.file_alreadyselected!!.absolutePath
                    )
                    Log.d("SPLITAUTO", "perform click\t" + mCurrentItem.pdfFilePath)
                    mListener!!.onItemClick(position)
                } else {
                    Log.d("SPLITAUTO", "file was null")
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mLongClickListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    } }