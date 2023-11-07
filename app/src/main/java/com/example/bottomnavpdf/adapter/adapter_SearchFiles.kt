package com.example.bottomnavpdf.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFilesBinding
import com.example.bottomnavpdf.ui.activities.SearchActivity
import com.example.bottomnavpdf.utils.PdfRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File

class adapter_SearchFiles(
    private var filelist: ArrayList<FileItems>,
   val context: SearchActivity
) :
    RecyclerView.Adapter<ViewHolder>() {
    private var mListener: OnItemClickListener? = null
    private val thumbnailCache = LruCache<String, Bitmap>(10 * 1024 * 1024)
    var mLongClickListener: OnItemLongClickListener? = null
    lateinit var binding: ItemFilesBinding

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun removeItem(position: Int) {
        filelist.removeAt(position)
        notifyItemRemoved(position)
    }

    fun filterList(filterlist: ArrayList<FileItems>) {
        filelist = filterlist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder: ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_files, parent, false
        )
        viewHolder = LoaderViewHolder(
            binding,
            thumbnailCache
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mCurrentItem = filelist[position]
        (holder as LoaderViewHolder).bind(mCurrentItem, mLongClickListener, mListener, filelist,context)
            Log.d("onBindViewHolder", "listview\t")
    }

    override fun getItemCount(): Int {
        return filelist.size
    }

    class LoaderViewHolder(
        binding: ItemFilesBinding,
        private val thumbnailCache: LruCache<String, Bitmap>
    ) : ViewHolder(binding.root) {
        val mBinding = binding
        private val picassoInstance = Picasso.Builder(binding.root.context.applicationContext)
            .addRequestHandler(PdfRequestHandler())
            .build()
        private var coroutineJob: Job? = null
        fun bind(
            mCurrentItem: FileItems,
            mLongClickListener: OnItemLongClickListener?,
            listener: OnItemClickListener?,
            filelist: ArrayList<FileItems>,
            context: SearchActivity,
        ) {
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
                    listener!!.onItemClick(mPosition)
                }
                mBinding.item.setOnLongClickListener {
                    true
                }
                mBinding.popupImgBtn.setOnClickListener {
                    try {
                        val mPosition = adapterPosition
                        val bm = (mBinding.fileImage.drawable as BitmapDrawable).bitmap
                        listener!!.onBottomSheetItemClick(mPosition, filelist[mPosition], bm)
                    } catch (_: Exception) {
                    }
                }
                val file = File(mCurrentItem.pdfRootPath)
                mBinding.pdffileRootPath.text = file.parentFile!!.name
                if (file.parentFile!!.name.equals("0")) {
                    mBinding.pdffileRootPath.text =context.getString(R.string.rootdir)
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