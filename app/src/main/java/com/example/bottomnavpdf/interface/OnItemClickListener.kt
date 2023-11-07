package com.example.bottomnavpdf.`interface`

import android.graphics.Bitmap
import com.example.bottomnavpdf.dataModel.FileItems

interface OnItemClickListener {
    fun onItemClick(position: Int)
    fun onBottomSheetItemClick(mPosition: Int, position: FileItems, bm: Bitmap)



}
