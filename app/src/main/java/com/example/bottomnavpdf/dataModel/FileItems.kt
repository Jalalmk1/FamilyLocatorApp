package com.example.bottomnavpdf.dataModel

import android.graphics.Bitmap


data class FileItems(
    val id: Int = 0,
    var pdfFilePath: String,
    var pdfRootPath: String,
    var fileName: String,
    var dateCreatedName: String,
    var dateModifiedName: String,
    var sizeName: String,
    var originalDateCreated: Long,
    var originalDateModified: Long,
    var originalSize: Long,
    var isChecked: Boolean = false,
    var fileImagebitmap: Bitmap? = null
)