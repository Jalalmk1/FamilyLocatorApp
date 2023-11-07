package com.example.bottomnavpdf.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.bottomnavpdf.dataModel.FileItems
import java.util.ArrayList

@SuppressLint("StaticFieldLeak")
object FvrtFilesBook {
    var myContext: Context? = null
    var allfvrtFileList = checkIsfvrtListNull()

    fun init(context: Context?, fvrtFilesList: List<FileItems>) {
        myContext = context
        allfvrtFileList = fvrtFilesList as ArrayList<FileItems>
    }

    private fun checkIsfvrtListNull(): ArrayList<FileItems>? {
        if (allfvrtFileList == null) {
            return ArrayList()
        }
        return allfvrtFileList
    }

//    fun addfvrtFile(sp: FileItems) {
//        if (allfvrtFileList!!.size < 10) {
//            allfvrtFileList!!.add(sp)
//        } else {
//            allfvrtFileList!!.removeAt(0)
//            allfvrtFileList!!.add(sp)
//        }
//    }
}