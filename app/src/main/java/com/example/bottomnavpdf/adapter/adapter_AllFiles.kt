package com.example.bottomnavpdf.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.Main_selectedItems
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFilesBinding
import com.example.bottomnavpdf.ui.activities.MainActivity
import com.example.bottomnavpdf.ui.home.HomeFragment
import com.example.bottomnavpdf.utils.PdfRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File

class adapter_AllFiles(
    private var filelist: ArrayList<FileItems>,
    val recyclerview: RecyclerView,
    val listenermainupdate: Main_selectedItems,
    val requireContext: Context, var type: Boolean = true
) :
    RecyclerView.Adapter<ViewHolder>() {
    private var mListener: OnItemClickListener? = null
    private var isGridLayout = false
    private val thumbnailCache = LruCache<String, Bitmap>(10 * 1024 * 1024)
    var mLongClickListener: OnItemLongClickListener? = null
    lateinit var binding: ItemFilesBinding


    companion object {
        var isCheckbox = false
        var isfromlongclick = false
        var isselectall: String? = null
        var selectedpos: Int = -1
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mLongClickListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun removeItem(position: Int) {
        if (position < filelist.size) {
            filelist.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val viewHolder: ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_files, parent, false
        )
        val viewHolder = LoaderViewHolder(
            binding,
            thumbnailCache
        )
        return viewHolder
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mCurrentItem = filelist[position]
        (holder as LoaderViewHolder).bind(
            mCurrentItem,
            mLongClickListener,
            mListener,
            filelist,
            recyclerview,
            listenermainupdate,
            requireContext, type
        )
        Log.d("onBindViewHolder", "listview\t")
    }


    fun setGridLayout(isGrid: Boolean) {
        try {
            isGridLayout = isGrid
            notifyDataSetChanged()
        } catch (_: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return filelist.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun change_menu_to_checkbox(isselected: Boolean) {
        isCheckbox = isselected
        notifyDataSetChanged()
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

        @SuppressLint("NotifyDataSetChanged")
        fun bind(
            mCurrentItem: FileItems,
            mLongClickListener: OnItemLongClickListener?,
            listener: OnItemClickListener?,
            filelist: ArrayList<FileItems>,
            recyclerview: RecyclerView,
            listenermainupdate: Main_selectedItems,
            requireContext: Context, type: Boolean
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

                Log.e("checkSelection", "------------------------------")
                Log.e("checkSelection", "isFromLong = ${isfromlongclick}")
                Log.e("checkSelection", "isAllCheck = ,${isCheckbox}")
                Log.e("checkSelection", "fileCheck = ,${mCurrentItem.isChecked}")

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
                    } else {
                        if (!type)
                            listenermainupdate.main_onItemselected(adapterPosition)
                    }

                    val mPosition = adapterPosition
                    listener?.onItemClick(mPosition)
                }
                mBinding.item.setOnLongClickListener {
                    val mPosition = adapterPosition
                    mLongClickListener!!.onItemLongClick(mPosition)

                    listenermainupdate.main_onItemselected(mPosition)

                    isfromlongclick = true
                    mBinding.checkBoxSelect.isChecked = true
                    recyclerview.adapter!!.notifyDataSetChanged()
                    selectedpos = mPosition
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
                    mBinding.pdffileRootPath.text = requireContext.getString(R.string.rootdir)
                }


                if (isfromlongclick) {

                    MainActivity.positionofadapter.add(adapterPosition)
                    mBinding.popupImgBtn.visibility = View.GONE
                    mBinding.checkBoxSelect.visibility = View.VISIBLE
                } else {
                    mBinding.checkBoxSelect.isChecked = false
                    MainActivity.selectedFileList.clear()
                    MainActivity.positionofadapter.clear()
                    mBinding.popupImgBtn.visibility = View.VISIBLE
                    mBinding.checkBoxSelect.visibility = View.GONE

                    mCurrentItem.isChecked = false
                }

                coroutineJob = CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        val bitmap = try {
                            HomeFragment.generateThumbnail(File(mCurrentItem.pdfFilePath))!!
                        } catch (e: Exception) {
                            null
                        }
                        bitmap
                    }?.let { bitmap ->
                        mBinding.fileImage.setImageBitmap(bitmap)
                    }
                }
            } catch (_: Exception) {
            }
            if (!type) {
                mBinding.popupImgBtn.visibility = View.GONE
            }
        }
    }
}