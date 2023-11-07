package com.example.bottomnavpdf.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.OnClickListener
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ItemFileImagesBinding
import kotlinx.coroutines.*
import java.io.File

class adapter_images_to_pdf(
    val context: Context,
    var filelist: ArrayList<FileItems>,
    val listener: OnClickListener
) :
    RecyclerView.Adapter<ViewHolder>() {
    lateinit var binding: ItemFileImagesBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder: ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_file_images, parent, false)
        viewHolder = LoaderViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filelist[position]
        (holder as LoaderViewHolder).bind(currentItem, listener, context)
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
        binding: ItemFileImagesBinding,
    ) : ViewHolder(binding.root) {
        private val mBinding = binding
        @SuppressLint("SuspiciousIndentation")
        fun bind(mCurrentItem: FileItems, listener: OnClickListener, context: Context) {
            try {
                Log.d("onBindViewHolder", "name\t${mCurrentItem.pdfFilePath}")
                mBinding.item.setOnClickListener {
                    val mPosition = adapterPosition
                    if (mBinding.checkBoxSelect.isChecked){
                        listener.onItemClick(mPosition, false)
                        mBinding.checkBoxSelect.isChecked=false
                    }
                    else{
                        listener.onItemClick(mPosition, true)
                        mBinding.checkBoxSelect.isChecked=true
                    }
                }
                val file = File(mCurrentItem.pdfFilePath)
                if (file.isFile) {
                    Glide.with(context).load(mCurrentItem.pdfFilePath).into(mBinding.fileImage)
                }
            } catch (_: Exception) {
            }
        }
    }
}