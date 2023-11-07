package com.example.bottomnavpdf.viewmodel

import android.app.Application
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bottomnavpdf.`interface`.myCallback
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.dataModel.FolderItems
import com.example.bottomnavpdf.utils.GeneralUtils.getDate
import com.example.bottomnavpdf.utils.GeneralUtils.getFileSize
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.absoluteValue


class LoaderViewModel(application: Application) : AndroidViewModel(application) {
    private val mTag = "loaderViewModel"
    var fileList: ArrayList<FileItems> = ArrayList()
    var fileListFolders: ArrayList<FolderItems> = ArrayList()

    private var isFileFetching = false

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e(mTag, "CoroutineExceptionHandler: $exception")
    }
    fun fetchPdfFiles(sortType: Int, order: String, callback: myCallback) {
        try {
            fileList.clear()
            if (fileList.isEmpty() && !isFileFetching) {
                viewModelScope.launch(Dispatchers.Main + handler) {
                    async(Dispatchers.IO + handler) {
                        Log.d(mTag, "fetchPdfFiles is called")
                        isFileFetching = true
                        val projection = arrayOf(
                            MediaStore.Files.FileColumns._ID,
                            MediaStore.Files.FileColumns.DATA,
                            MediaStore.Files.FileColumns.DISPLAY_NAME,
                            MediaStore.Files.FileColumns.DATE_ADDED,
                            MediaStore.Files.FileColumns.DATE_MODIFIED,
                            MediaStore.Files.FileColumns.SIZE
                        )
                        val mimeTypePdf = "application/pdf"
                        val whereClause =
                            MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeTypePdf + "')"
                        var orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
                        when (sortType) {
                            0 -> {
                                orderBy = MediaStore.Files.FileColumns.DATE_MODIFIED + " "+order
                            }
                            1 -> {
                                if (order == "DESC") {
                                    orderBy = MediaStore.Files.FileColumns.DISPLAY_NAME +" ASC"
                                }
                                else{
                                    orderBy = MediaStore.Files.FileColumns.DISPLAY_NAME +" DESC"
                                }
                            }
                            2 -> {
                                orderBy = MediaStore.Files.FileColumns.SIZE + " "+order
                            }
                        }

                        val cursor: Cursor? = getApplication<Application>().contentResolver.query(
                            MediaStore.Files.getContentUri("external"),
                            projection,
                            whereClause,
                            null,
                            orderBy
                        )

                        cursor?.let {
                            val dataCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                            val addedCol =
                                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                            val modifiedCol =
                                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                            if (it.moveToFirst()) {
                                do {
                                    var nextId = fileList.size + 1
                                    val data = it.getString(dataCol.absoluteValue)
                                    val dateAdded = it.getLong(addedCol)
                                    val dateModified = it.getLong(modifiedCol)
                                    it.getLong(sizeCol)
                                    val file = File(data).absoluteFile
                                    val root = file.absolutePath
                                    if (file.exists()) {
                                        // Set the bitmap to the fileimage of the RecyclerView item
                                        fileList.add(
                                            FileItems(
                                                id = nextId++,
                                                pdfFilePath = data,
                                                fileName = file.name,
                                                pdfRootPath = root,
                                                dateCreatedName = getDate(dateAdded * 1000),
                                                dateModifiedName = getDate(dateModified * 1000),
                                                sizeName = getFileSize(file.length()),
                                                originalDateCreated = dateAdded,
                                                originalDateModified = dateModified,
                                                originalSize = file.length()
                                            )
                                        )
                                        (file.length() / 1024).toInt()
                                        Log.d("DATEMODIFIED",file.absolutePath)
                                    }
                                } while (it.moveToNext())
                            }

                            it.close()
                        }
                    }.await()
                    Handler(Looper.getMainLooper()).postDelayed({
                        isFileFetching = false
                        Log.d(mTag, "invoke")
                        Log.d(mTag, "pdfFilesList size: ${fileList.size}")
                        callback.callbackInvoke()
                    },500)
                }
            } else {
                callback.callbackInvoke()
            }
        } catch (_: Exception) {
        }

    }


    fun fetchFolders(callback: () -> Unit) {
        if (fileListFolders.isNotEmpty()) {
            callback.invoke()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
            )

            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_NONE} AND ${MediaStore.Files.FileColumns.DATA} LIKE '%.pdf'"
            val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

            val cursor: Cursor? = getApplication<Application>().contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val folders = mutableMapOf<String, FolderItems>()

                while (cursor.moveToNext()) {
                    val data = cursor.getString(dataCol)
                    val dateModified = cursor.getLong(modifiedCol)
                    val dateCreated = cursor.getLong(addedCol)
                    val folderPath = data.substringBeforeLast(File.separator)
                    val folder = folders.getOrPut(folderPath) {
                        FolderItems(
                            folderPath = folderPath,
                            folderName = File(folderPath).name,
                            numOfFiles = 0,
                            folderSize = "",
                            originalDateModified = 0L,
                            dateCreatedName = getDate(dateCreated * 1000) // <-- set the value of the dateCreatedName field
                        )
                    }
                    folder.numOfFiles++
                    folder.originalDateModified = maxOf(folder.originalDateModified, dateModified)
                }

                fileListFolders.addAll(folders.values)
            }

            withContext(Dispatchers.Main) {
                callback.invoke()
            }
        }
    }

    fun isFilesListEmpty() = fileList.isEmpty()

}