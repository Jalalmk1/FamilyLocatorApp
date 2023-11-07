package com.example.bottomnavpdf.dataModel

data class FolderItems(
    var folderPath: String,
    var folderName: String,
    var numOfFiles: Int,
    var folderSize: String,
    var originalDateModified: Long,
    var dateCreatedName: String
)
