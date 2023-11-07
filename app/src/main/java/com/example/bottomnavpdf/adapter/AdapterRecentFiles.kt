package com.example.bottomnavpdf.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.Main_selectedItems
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFilesRecentBinding
import com.example.bottomnavpdf.ui.activities.MainActivity
import com.example.bottomnavpdf.ui.home.HomeFragment
import com.example.bottomnavpdf.utils.PdfRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File


class AdapterRecentFiles(
    val context: Context,
    private val fileList: ArrayList<FileItems>,
    val rvRecentAllfiles: RecyclerView,
    val listener: Main_selectedItems
) :
    RecyclerView.Adapter<ViewHolder>() {
    private val thumbnailCache = LruCache<String, Bitmap>(10 * 1024 * 1024)
    private var isGridLayout = false
    private var mListener: OnItemClickListener? = null
    var mLongClickListener: OnItemLongClickListener? = null

    companion object {
        var isCheckbox = false
        var isfromlongclick = false
        var isselectall: String? = null
        var selectedpos: Int = -1
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mLongClickListener = listener
    }

    fun removeItem(position: Int) {
        fileList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setGridLayout(isGrid: Boolean) {
        try {
            isGridLayout = isGrid
            notifyDataSetChanged()
        } catch (_: Exception) {
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ItemFilesRecentBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.item_files_recent, parent, false
        )
        val viewHolder = RecentFileViewHolder(binding)
        return viewHolder

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = fileList[position]
        val viewHolder = holder as RecentFileViewHolder
        viewHolder.bind(
            currentItem,
            thumbnailCache,
            mListener,
            mLongClickListener,
            fileList,
            context,
            rvRecentAllfiles,
            listener,
            context
        )
    }

    class RecentFileViewHolder(
        binding: ItemFilesRecentBinding
    ) : ViewHolder(binding.root) {
        private val mBinding = binding
        private var coroutineJob: Job? = null
        private val picassoInstance = Picasso.Builder(binding.root.context.applicationContext)
            .addRequestHandler(PdfRequestHandler())
            .build()

        @SuppressLint("NotifyDataSetChanged")
        fun bind(
            mCurrentItem: FileItems,
            thumbnailCache: LruCache<String, Bitmap>,
            mListener: OnItemClickListener?,
            mLongClickListener: OnItemLongClickListener?,
            fileList: ArrayList<FileItems>,
            context: Context,
            rvRecentAllfiles: RecyclerView,
            listenermainupdate: Main_selectedItems,
            context1: Context
        ) {
            mBinding.fileName.text = mCurrentItem.fileName
            mBinding.fileDate.text = mCurrentItem.dateCreatedName
            mBinding.fileSize.text = mCurrentItem.sizeName


            if (isfromlongclick) {

                if (isCheckbox) {
                    mBinding.checkBoxSelect.isChecked = true
                    MainActivity.selectedFileList.add(mCurrentItem)
                    MainActivity.positionofadapter.add(adapterPosition)
                    listenermainupdate.main_onItemselected(adapterPosition)
                } else {
                    mBinding.checkBoxSelect.isChecked = mCurrentItem.isChecked

                    if (mCurrentItem.isChecked) {
                        MainActivity.selectedFileList.add(mCurrentItem)
                        MainActivity.positionofadapter.add(adapterPosition)
                        listenermainupdate.main_onItemselected(adapterPosition)
                    }
                }
            }


            mBinding.item.setOnClickListener {
                if (isfromlongclick) {
                    if (isCheckbox) {
                        mBinding.checkBoxSelect.isChecked = true
                        MainActivity.selectedFileList.add(mCurrentItem)
                        MainActivity.positionofadapter.add(adapterPosition)
                        listenermainupdate.main_onItemselected(adapterPosition)

                        return@setOnClickListener
                    } else {
                        mCurrentItem.isChecked = !mCurrentItem.isChecked
                        mBinding.checkBoxSelect.isChecked = mCurrentItem.isChecked

                        if (mCurrentItem.isChecked) {
                            MainActivity.selectedFileList.add(mCurrentItem)
                            MainActivity.positionofadapter.add(adapterPosition)
                            listenermainupdate.main_onItemselected(adapterPosition)
                        } else {
                            MainActivity.selectedFileList.remove(mCurrentItem)
                            MainActivity.positionofadapter.remove(adapterPosition)
                            listenermainupdate.main_onItemselected(adapterPosition)
                        }

                        return@setOnClickListener
                    }
                }

                val mPosition = adapterPosition
                mListener?.onItemClick(mPosition)
            }
            mBinding.item.setOnLongClickListener {
                val mPosition = adapterPosition
                mLongClickListener!!.onItemLongClick(mPosition)

                listenermainupdate.main_onItemselected(mPosition)

                isfromlongclick = true
                mBinding.checkBoxSelect.isChecked = true
                rvRecentAllfiles.adapter?.notifyDataSetChanged()
                selectedpos = mPosition
                true
            }
            
            mBinding.popupImgBtn.setOnClickListener {
                val mPosition = adapterPosition
                try {
                    val bm = (mBinding.fileImage.drawable as BitmapDrawable).bitmap
                    mListener!!.onBottomSheetItemClick(mPosition, fileList[mPosition], bm)
                } catch (_: Exception) {
                    Toast.makeText(context, "Document is password protected", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            val file = File(mCurrentItem.pdfRootPath)
            mBinding.pdffileRootPath.text = file.parentFile!!.name
            if (file.parentFile!!.name.equals("0")) {
                mBinding.pdffileRootPath.text = context.getString(R.string.rootdir)
            }
            
            if (isfromlongclick) {

                MainActivity.positionofadapter.add(adapterPosition)
                mBinding.popupImgBtn.visibility = View.GONE
                mBinding.checkBoxSelect.visibility = View.VISIBLE
            }else{
                mBinding.checkBoxSelect.isChecked = false
                MainActivity.selectedFileList.clear()
                MainActivity.positionofadapter.clear()
                mBinding.popupImgBtn.visibility = View.VISIBLE
                mBinding.checkBoxSelect.visibility = View.GONE

                mCurrentItem.isChecked = false
            }
//            coroutineJob?.cancel()
//            val cacheKey = "${PdfRequestHandler.SCHEME_PDF}:${mCurrentItem.pdfFilePath}"
//            val cachedThumbnail = thumbnailCache.get(cacheKey)
//
//            if (cachedThumbnail != null) {
//                mBinding.fileImage.setImageBitmap(cachedThumbnail)
//            } else {
            coroutineJob = CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    val bitmap = try {
                        // Load the PDF thumbnail using PdfRequestHandler
//                            picassoInstance.load(cacheKey)
//                                .resize(95, 95)
//                                .get()
                        HomeFragment.generateThumbnail(File(mCurrentItem.pdfFilePath))!!
                    } catch (e: Exception) {
                        null
                    }
                    bitmap
                }?.let { bitmap ->
                    mBinding.fileImage.setImageBitmap(bitmap)
//                        thumbnailCache.put(cacheKey, bitmap)
                } ?: run {

                }
            }
//            }
        }

    }

    fun change_menu_to_checkbox(isselected: Boolean) {
        isCheckbox = isselected
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return fileList.size
    }
}
