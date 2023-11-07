package com.example.bottomnavpdf.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.bottomnavpdf.dataModel.FileItems
import java.util.ArrayList

@SuppressLint("StaticFieldLeak")
object RecentFilesBook {
    var myContext: Context? = null
    var allrecentFileList = checkIsRecentListNull()

    private fun checkIsRecentListNull(): ArrayList<FileItems>? {
        if (allrecentFileList == null) {
            return ArrayList()
        }
        return allrecentFileList
    }
}