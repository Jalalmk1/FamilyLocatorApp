package com.example.bottomnavpdf.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64.*
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavpdf.*
import com.example.bottomnavpdf.adapter.adapter_AllFiles
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.FragmentHomeBinding
import com.example.bottomnavpdf.`interface`.Main_selectedItems
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.`interface`.myCallback
import com.example.bottomnavpdf.ui.activities.MergePdf
import com.example.bottomnavpdf.ui.activities.SplitPDF
import com.example.bottomnavpdf.ui.activities.viewerPdf
import com.example.bottomnavpdf.utils.FvrtFilesBook
import com.example.bottomnavpdf.utils.GeneralUtils
import com.example.bottomnavpdf.utils.RecentFilesBook
import com.example.bottomnavpdf.utils.Variables
import com.example.bottomnavpdf.viewmodel.LoaderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment() : Fragment() {
    private lateinit var loaderViewModel: LoaderViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mAdapter: adapter_AllFiles
    private lateinit var listener: Main_selectedItems
    var permissionLayout: LinearLayoutCompat? = null

    constructor(listener: Main_selectedItems) : this() {
        this.listener = listener
    }

    companion object {
        var originalFileList: ArrayList<FileItems> = ArrayList()
        var bitmap: Bitmap? = null
        var type = 0
        var order = "DESC"

        fun generateThumbnail(pdfFile: File): Bitmap? {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            val page = renderer.openPage(0) // Page index (0-based)
            // Create a Bitmap for the thumbnail
            val thumbnailBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            // Render the page to the Bitmap
            page.render(thumbnailBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            // Close the renderer and page
            page.close()
            renderer.close()
            return thumbnailBitmap
        }
    }

    var isGrid = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        initViewModel()
        if (originalFileList.isNotEmpty()) {
            proceedFurther_recyclerview()
            binding.loadingProgressBar.visibility = View.GONE
        } else {
            getFiles(0, "DESC")
        }
        this.createLoaderRecyclerView()
        PreferenceManager.getDefaultSharedPreferences(context).let {
            val gson = Gson()
            val json = it.getString("favourite_files_list", null)
            if (!json.isNullOrEmpty()) {
                val fvrtFilesList = gson.fromJson(json, Array<FileItems>::class.java)
                val filesList = ArrayList<FileItems>()
                filesList.addAll(fvrtFilesList)
                FvrtFilesBook.allfvrtFileList!!.clear()
                FvrtFilesBook.allfvrtFileList = filesList
            }
        }

        return binding.root
    }

    fun onSelecteditemCall(isselected: Boolean) {
        try {
            mAdapter.change_menu_to_checkbox(isselected)
        } catch (_: Exception) {
        }
    }

   fun sortFiles(){

             val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
             val bottomSheetView = layoutInflater.inflate(R.layout.sortby_dailog, null)
             bottomSheetDialog.setContentView(bottomSheetView)
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_btn_ok)!!.setOnClickListener {
                 bottomSheetDialog.dismiss()
                 Collections.sort(originalFileList, FishNameComparator(type, order))
                 initrecyclerview()
             }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_btn_cancel)!!.setOnClickListener {
                 bottomSheetDialog.dismiss()
             }
             if (type == 0) {
                 setDefault_icons(bottomSheetDialog)
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_lastmodified)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.calendericon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )
                 if (order == "DESC") {

                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.newtoold_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.oldtonew_icon
                             ),
                             null,
                             null,
                             null
                         )
                 } else {
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.newtoold_icon
                             ),
                             null,
                             null,
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.oldtonew_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                 }
             }
             else if (type == 1) {
                 setDefault_icons(bottomSheetDialog)
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_names)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.sortname_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )
                 if (order == "DESC") {
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.a_to_z_ico
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.z_to_a_icon
                             ),
                             null,
                             null,
                             null
                         )
                 } else {
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.a_to_z_ico
                             ),
                             null,
                             null,
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.z_to_a_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                 }
             }
             else if (type == 2) {
                 setDefault_icons(bottomSheetDialog)
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_filesize)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.sortfilesize_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )

                 if (order == "DESC") {
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.smalltolarge_icon
                             ),
                             null,
                             null,
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.largetosmall_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                 } else {
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.smalltolarge_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.largetosmall_icon
                             ),
                             null,
                             null,
                             null
                         )
                 }
             }

             bottomSheetDialog.findViewById<View>(R.id.dialogsort_lastmodified)!!
                 .setOnClickListener {
                     setDefault_icons(bottomSheetDialog)
                     order = "DESC"
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!.visibility =
                         View.VISIBLE
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!.visibility =
                         View.VISIBLE
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!.visibility =
                         View.GONE
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!.visibility =
                         View.GONE
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!.visibility =
                         View.GONE
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!.visibility =
                         View.GONE
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_lastmodified)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.calendericon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                     type = 0
                 }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_names)!!.setOnClickListener {
                 setDefault_icons(bottomSheetDialog)
                 order = "DESC"
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_names)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.sortname_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )
                 type = 1
             }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_filesize)!!.setOnClickListener {
                 setDefault_icons(bottomSheetDialog)
                 order = "DESC"
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!.visibility =
                     View.GONE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!.visibility =
                     View.VISIBLE
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_filesize)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.sortfilesize_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )
                 type = 2
             }

             bottomSheetDialog.findViewById<View>(R.id.dialogsort_newtoold)!!.setOnClickListener {
                 order = "DESC"
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.newtoold_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.oldtonew_icon
                         ),
                         null,
                         null,
                         null
                     )

             }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_oldtonew)!!.setOnClickListener {
                 order = "ASC"
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_newtoold)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.newtoold_icon
                         ),
                         null,
                         null,
                         null
                     )
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_oldtonew)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.oldtonew_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )

             }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_a_to_z)!!.setOnClickListener {
                 order = "DESC"
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.a_to_z_ico
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.z_to_a_icon
                         ),
                         null,
                         null,
                         null
                     )

             }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_z_to_a)!!.setOnClickListener {
                 order = "ASC"
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_a_to_z)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.a_to_z_ico
                         ),
                         null,
                         null,
                         null
                     )
                 bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_z_to_a)!!
                     .setCompoundDrawablesWithIntrinsicBounds(
                         ContextCompat.getDrawable(
                             requireContext(),
                             R.drawable.z_to_a_icon
                         ),
                         null,
                         ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                         null
                     )

             }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_smalltolarge)!!
                 .setOnClickListener {
                     order = "ASC"
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.smalltolarge_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.largetosmall_icon
                             ),
                             null,
                             null,
                             null
                         )

                 }
             bottomSheetDialog.findViewById<View>(R.id.dialogsort_largetosmall)!!
                 .setOnClickListener {
                     order = "DESC"
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_smalltolarge)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.smalltolarge_icon
                             ),
                             null,
                             null,
                             null
                         )
                     bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_largetosmall)!!
                         .setCompoundDrawablesWithIntrinsicBounds(
                             ContextCompat.getDrawable(
                                 requireContext(),
                                 R.drawable.largetosmall_icon
                             ),
                             null,
                             ContextCompat.getDrawable(requireContext(), R.drawable.checkdone_icon),
                             null
                         )

                 }

             bottomSheetDialog.show()

    }

    var job: Job? = null
    override fun onResume() {
        super.onResume()




        permissionLayout = requireActivity().findViewById(R.id.framelayout_permission)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                updateitems()
            },
            500
        )

        lifecycleScope.launch(Dispatchers.IO) {
            while (!isReadPermission()) {
                withContext(Dispatchers.Main) {
                    Log.e("isReadPermission", "permission = ${isReadPermission()}")
                    if (isReadPermission()) {
                        permissionLayout?.visibility = View.GONE
                    }

                }
                delay(1000)
            }
            withContext(Dispatchers.Main) {
                Log.e("isReadPermission", "permission = ${isReadPermission()}")
                if (isReadPermission()) {
                    permissionLayout?.visibility = View.GONE
                }
            }
        }
    }

    fun itemremovedat(pos: Int) {
        mAdapter.removeItem(pos)
    }

    fun updateitems() {
        if (Variables.DATACHANGED) {
            val beforesize: Int
            Log.d("FRAGMENTSTUFF1", "updateitems()")
            Log.d("FRAGMENTSTUFF1", "list size before\t" + originalFileList.size)
            beforesize = originalFileList.size
            val uri: Uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.SIZE
            )


            val mimeTypePdf = "application/pdf"
            val whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeTypePdf + "')"
            val cursor: Cursor? =
                requireContext().contentResolver.query(uri, projection, whereClause, null, null)
            if (cursor != null) {
                originalFileList.clear()
                while (cursor.moveToNext()) {

                    var nextId = originalFileList.size + 1
                    val file =
                        (File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))))
                    originalFileList.add(
                        FileItems(
                            id = nextId++,
                            pdfFilePath = file.absolutePath,
                            fileName = file.name,
                            pdfRootPath = file.absolutePath,
                            dateCreatedName = getDate(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                        MediaStore.Files.FileColumns.DATE_ADDED
                                    )
                                ) * 1000
                            ),
                            dateModifiedName = getDate(
                                cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                        MediaStore.Files.FileColumns.DATE_MODIFIED
                                    )
                                ) * 1000
                            ),
                            sizeName = GeneralUtils.getFileSize(file.length()),
                            originalDateCreated = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    MediaStore.Files.FileColumns.DATE_ADDED
                                )
                            ),
                            originalDateModified = cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                    MediaStore.Files.FileColumns.DATE_MODIFIED
                                )
                            ),
                            originalSize = file.length()
                        )
                    )
                    Log.d("FRAGMENTSTUFF1", "list...\t" + file.absolutePath)
                }

                if (!originalFileList.isEmpty()) {
                    Collections.sort(originalFileList, FishNameComparator(type, order))
                    Log.d("FRAGMENTSTUFF1", "list was updated\t")
                    initrecyclerview()
                }
            }
            cursor!!.close()
            Log.d("FRAGMENTSTUFF1", "data was updated\t")
            Variables.DATACHANGED = false
        } else {
            Log.d("FRAGMENTSTUFF1", "data was same\t")
        }

//        val beforesize: Int
//        val dummylist: ArrayList<FileItems> = ArrayList()
//        Log.d("FRAGMENTSTUFF1", "updateitems()")
//        Log.d("FRAGMENTSTUFF1", "list size before\t" + originalFileList.size)
//        beforesize = originalFileList.size
//        val uri: Uri = MediaStore.Files.getContentUri("external")
//        val projection = arrayOf(
//            MediaStore.Files.FileColumns.DATA,
//            MediaStore.Files.FileColumns.DISPLAY_NAME,
//            MediaStore.Files.FileColumns.DATE_ADDED,
//            MediaStore.Files.FileColumns.DATE_MODIFIED,
//            MediaStore.Files.FileColumns.SIZE
//        )
//        val mimeTypePdf = "application/pdf"
//        val whereClause = MediaStore.Files.FileColumns.MIME_TYPE + " IN ('" + mimeTypePdf + "')"
//        val cursor: Cursor? =
//            requireContext().contentResolver.query(uri, projection, whereClause, null, null)
//        if (cursor != null) {
//            dummylist.clear()
//            while (cursor.moveToNext()) {
//                var nextId = dummylist.size + 1
//                val file =
//                    (File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))))
//                dummylist.add(
//                    FileItems(
//                        id = nextId++,
//                        pdfFilePath = file.absolutePath,
//                        fileName = file.name,
//                        pdfRootPath = file.absolutePath,
//                        dateCreatedName = getDate(
//                            cursor.getLong(
//                                cursor.getColumnIndexOrThrow(
//                                    MediaStore.Files.FileColumns.DATE_ADDED
//                                )
//                            ) * 1000
//                        ),
//                        dateModifiedName = getDate(
//                            cursor.getLong(
//                                cursor.getColumnIndexOrThrow(
//                                    MediaStore.Files.FileColumns.DATE_MODIFIED
//                                )
//                            ) * 1000
//                        ),
//                        sizeName = GeneralUtils.getFileSize(file.length()),
//                        originalDateCreated = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)),
//                        originalDateModified = cursor.getLong(
//                            cursor.getColumnIndexOrThrow(
//                                MediaStore.Files.FileColumns.DATE_MODIFIED
//                            )
//                        ),
//                        originalSize = file.length()
//                    )
//                )
//                Log.d("FRAGMENTSTUFF1", "list...\t" + file.absolutePath)
//            }
//            if (!dummylist.isEmpty()) {
//                if (beforesize != dummylist.size) {
//                    originalFileList.clear()
//                    originalFileList.addAll(dummylist)
//                    Collections.sort(originalFileList, FishNameComparator(0, "DESC"))
//                    Log.d("FRAGMENTSTUFF1", "list was updated\t")
//                    initrecyclerview()
//                    dummylist.clear()
//                } else {
//                    Log.d("FRAGMENTSTUFF1", "list was same\t")
//                }
//            }
//        }
//        cursor!!.close()
    }

    private fun initViewModel() {
        loaderViewModel = ViewModelProvider(this).get(LoaderViewModel::class.java)
    }

    private fun createLoaderRecyclerView() {
//        mAdapter = AdapterFilesLoader()
        // Set the initial layout manager to a linear layout manager
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.layoutManager = layoutManager
        binding.dashboardGridLinear.setOnClickListener {
            if (isGrid) {
                val anim = ScaleAnimation(
                    1f, 0.9f, 1f, 0.9f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                )
                anim.duration = 200
                anim.repeatCount = 1
                anim.repeatMode = Animation.REVERSE
                binding.dashboardGridLinear.startAnimation(anim)
                val gridLayoutManager = GridLayoutManager(requireContext(), 3)
                binding.recyclerview.layoutManager = gridLayoutManager
                mAdapter.setGridLayout(true)
                isGrid = false
                binding.dashboardGridLinear.setImageResource(R.drawable.gridimage)
            } else {
                val anim = ScaleAnimation(
                    1f, 0.9f, 1f, 0.9f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                )
                anim.duration = 100
                anim.repeatCount = 1
                anim.repeatMode = Animation.REVERSE
                binding.dashboardGridLinear.startAnimation(anim)
                binding.recyclerview.layoutManager = layoutManager
                mAdapter.setGridLayout(false)
                isGrid = true
                binding.dashboardGridLinear.setImageResource(R.drawable.listimage)
            }
        }


    }

    private fun getFiles(type: Int, order: String) {


        Log.e("isReadPermission", "permission = ${isReadPermission()}")
        if (isReadPermission()) {
            if (permissionLayout?.visibility != View.GONE)
                permissionLayout?.visibility = View.GONE
        }

        loaderViewModel.fetchPdfFiles(type, order, object : myCallback {
            override fun callbackInvoke() {
                submitList()
                Log.e("isReadPermission", "permission = ${isReadPermission()}")
                if (requireActivity().checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (permissionLayout?.visibility != View.GONE)
                        permissionLayout?.visibility = View.GONE
                }
            }
        })
        loaderViewModel.fetchFolders {
            Log.e("isReadPermission", "permission = ${isReadPermission()}")
            if (requireActivity().checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (permissionLayout?.visibility != View.GONE)
                    permissionLayout?.visibility = View.GONE
            }

        }
    }


    fun isReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            requireActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun submitList() {
        try {
            if (!loaderViewModel.isFilesListEmpty()) {
                binding.loadingProgressBar.visibility = View.GONE
                binding.noFilesLayout.visibility = View.GONE
                originalFileList.clear()
                originalFileList = loaderViewModel.fileList
                Collections.sort(originalFileList, FishNameComparator(type, order))
                proceedFurther_recyclerview()
            } else {
                noList()
            }
        } catch (e: Exception) {
            Log.d("HOMEEXCEP", e.message.toString())
        }
    }

    fun proceedFurther_recyclerview() {
        mAdapter = adapter_AllFiles(
            originalFileList,
            binding.recyclerview,
            listener,
            requireContext()
        )
        mAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                loadPreviousRecentFiles()
                val openlist = originalFileList[position]
                val file = File(openlist.pdfFilePath)
                if (file.exists() && file.isFile) {
                    val intent = Intent(context, viewerPdf::class.java)
                        .putExtra("key", file.absolutePath)
                    startActivity(intent)
                } else {
                    originalFileList.removeAt(position)
                    mAdapter.notifyItemRemoved(position)
                    Toast.makeText(
                        requireContext(),
                        "Sorry item not found!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (!RecentFilesBook.allrecentFileList?.contains(openlist)!!) {
                    RecentFilesBook.allrecentFileList?.add(openlist)
                    saveRecentFilesList(RecentFilesBook.allrecentFileList!!)
                    Log.i("RECENTCHECK", "file added in recent")
                } else {
                    Log.i("RECENTCHECK", "file already in recent")
                }
                val current = Date()
                val format1 = SimpleDateFormat("hh:mm, MMM dd, yyyy")
                val currentdate = format1.format(current)
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                val ppp = file.parentFile?.absolutePath
                editor.putString(ppp, currentdate)
                editor.apply()
                Log.i("SELECTEDITEM", "root path set\t" + ppp)
            }

            @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
            override fun onBottomSheetItemClick(
                position: Int,
                titleBottom: FileItems,
                bm: Bitmap
            ) {
                loadPreviousFavFiles()
                val bottomSheetDialog =
                    BottomSheetDialog(requireContext(), R.style.SheetDialog)
                val bottomSheetView =
                    layoutInflater.inflate(R.layout.bottom_sheet_dailog, null)
                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetView.findViewById<AppCompatImageView>(R.id.imageView3)
                    .setImageBitmap(bm)
                bottomSheetView.findViewById<TextView>(R.id.textView3).text =
                    titleBottom.fileName
                bottomSheetView.findViewById<TextView>(R.id.textView4).text =
                    titleBottom.dateCreatedName
                bottomSheetView.findViewById<TextView>(R.id.text_size_bottom).text =
                    titleBottom.sizeName
                bottomSheetView.findViewById<TextView>(R.id.text_path_bottom).text =
                    titleBottom.pdfFilePath
                bottomSheetView.findViewById<View>(R.id.bottomsheet_topdetail)
                    .setOnClickListener {
                        val file = File(titleBottom.pdfFilePath)
                        val lastModDate = Date(file.lastModified())
                        val format1 = SimpleDateFormat("hh:mm, MMM dd, yyyy")
                        val moddate = format1.format(lastModDate)
                        val sharedPref =
                            requireActivity().getPreferences(Context.MODE_PRIVATE)
                        val ppp = file.parentFile?.absolutePath
                        Log.i("SELECTEDITEM", "root path get\t$ppp")
                        val lastviewdate = sharedPref.getString(ppp, null)
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.filedetail_dailog)
                        dialog.getWindow()!!
                            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window!!.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        dialog.setCancelable(true)
                        dialog.findViewById<TextView>(R.id.detail_dialog_name).text =
                            titleBottom.fileName
                        dialog.findViewById<TextView>(R.id.detail_dialog_path).text =
                            titleBottom.pdfFilePath

                        dialog.findViewById<TextView>(R.id.detail_dialog_lastmodified).text =
                            moddate
                        if (lastviewdate != null) {
                            dialog.findViewById<TextView>(R.id.detail_dialog_lastviewed).text =
                                lastviewdate
                        } else {
                            dialog.findViewById<TextView>(R.id.detail_dialog_lastviewed).visibility =
                                View.GONE
                            dialog.findViewById<TextView>(R.id.detail_dialog_lastviewed_title).visibility =
                                View.GONE
                        }
                        dialog.findViewById<TextView>(R.id.detail_dialog_size).text =
                            titleBottom.sizeName
                        dialog.findViewById<View>(R.id.ok_detail_dialog)
                            .setOnClickListener {
                                dialog.dismiss()
                            }
                        dialog.show()
                    }
                bottomSheetView.findViewById<View>(R.id.del_tool_container)
                    .setOnClickListener {
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.deleteitem_dailog)
                        dialog.setCancelable(true)
                        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
                        dialog.window!!.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val btdelete = dialog.findViewById<AppCompatButton>(R.id.deletebtn)
                        val btncancel =
                            dialog.findViewById<AppCompatButton>(R.id.cancel_button)
                        btncancel.setOnClickListener {
                            dialog.dismiss()
                            bottomSheetDialog.dismiss()

                        }
                        btdelete.setOnClickListener {
                            val fileName = titleBottom.fileName
                            val directoryPath = titleBottom.pdfFilePath
                            val parentDirPath = File(directoryPath).parent
                            val file = File(parentDirPath, fileName)
                            Log.v("File", "File name: $fileName")
                            Log.v("File", "Directory path: $directoryPath")
                            Log.v("File", "File exists: ${file.exists()}")
                            Log.v("File", "Is a file: ${file.isFile}")
                            if (file.exists()) {
                                if (file.delete()) {
                                    mAdapter.removeItem(position)
                                    Toast.makeText(
                                        requireContext(),
                                        "File deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    try {
                                        if (FvrtFilesBook.allfvrtFileList?.contains(
                                                titleBottom
                                            )!!
                                        ) {
                                            FvrtFilesBook.allfvrtFileList?.remove(
                                                titleBottom
                                            )
                                            savefavourteFilesList(
                                                FvrtFilesBook.allfvrtFileList!!,
                                                false
                                            )
                                        }
                                        if (RecentFilesBook.allrecentFileList?.contains(
                                                titleBottom
                                            )!!
                                        ) {
                                            RecentFilesBook.allrecentFileList?.remove(
                                                titleBottom
                                            )
                                            saveRecentFilesList(RecentFilesBook.allrecentFileList!!)
                                        }
                                    } catch (_: Exception) {
                                    }
                                    bottomSheetDialog.dismiss()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to delete file: ${file.path}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "File not found or is not a file: ${file.path}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            dialog.dismiss()
                            bottomSheetDialog.dismiss()
                        }
                        dialog.show()
                    }
                bottomSheetView.findViewById<View>(R.id.rename_tool_container)
                    .setOnClickListener {
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.rename_dailog)
                        dialog.setCancelable(true)
                        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
                        dialog.window!!.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        val okButton = dialog.findViewById<AppCompatButton>(R.id.okRename)
                        val cancelButton =
                            dialog.findViewById<AppCompatButton>(R.id.cancelRename)
                        okButton.setOnClickListener {
                            val existingFile = File(titleBottom.pdfFilePath)
                            val arrValues: Array<String> =
                                titleBottom.pdfFilePath.split(".").toTypedArray()
                            val newName =
                                dialog.findViewById<AppCompatEditText>(R.id.textView6).text.toString()
                            var newFilePath =
                                existingFile.parentFile?.absolutePath + "/$newName.${arrValues[arrValues.size - 1]}"
                            newFilePath = newFilePath.replace("//", "/")
                            val newFile = File(newFilePath + "")
                            if (newFile.exists()) {
                                Toast.makeText(
                                    requireContext(),
                                    "File with this name already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (existingFile.renameTo(newFile)) {
                                    // Update file path after renaming
                                    titleBottom.pdfFilePath = newFilePath
                                    val list = originalFileList[position]
                                    list.fileName = newName
                                    mAdapter.notifyItemChanged(position)
                                    Toast.makeText(
                                        requireContext(),
                                        "File renamed successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Log.d("RenameFile", "Failed to rename file")
                                    Log.e("RenameFile", "Error: ${existingFile}")
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to rename file",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            dialog.dismiss()
                        }
                        cancelButton.setOnClickListener {
                            dialog.dismiss()
                        }
                        val editText =
                            dialog.findViewById<AppCompatEditText>(R.id.textView6)
                        editText.setText(titleBottom.fileName)
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        editText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                // Show the drawableEnd if the text is not empty
                                if (!s.isNullOrEmpty()) {
                                    editText.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.edit_text_cut,
                                        0
                                    )
                                } else {
                                    editText.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        0,
                                        0
                                    )
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }
                        })

                        editText.setOnTouchListener { _, event ->
                            // Clear the text when the drawableEnd is clicked
                            if (event.action == MotionEvent.ACTION_UP) {
                                val drawableEnd = editText.compoundDrawables[2]
                                if (drawableEnd != null && event.rawX >= (editText.right - drawableEnd.bounds.width())) {
                                    editText.text = null
                                    return@setOnTouchListener true
                                }
                            }
                            return@setOnTouchListener false
                        }
                        dialog.show()
                        bottomSheetDialog.dismiss()
                    }

                bottomSheetView.findViewById<View>(R.id.fav_tool_container)
                    .setOnClickListener {
                        if (!FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                            FvrtFilesBook.allfvrtFileList?.add(titleBottom)
                            savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, true)
                        } else {
                            FvrtFilesBook.allfvrtFileList?.remove(titleBottom)
                            savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, false)
                            Log.i("menu", "onMenuItemClick: file already in recent")
                        }
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.share_tool_container)
                    .setOnClickListener {
                        val uriii = FileProvider.getUriForFile(
                            activity!!,
                            activity!!.packageName + ".provider",
                            File(titleBottom.pdfFilePath)
                        )
                        val intent = Intent()
                        intent.action = "android.intent.action.SEND"
                        intent.type = "application/pdf"
                        intent.putExtra("android.intent.extra.STREAM", uriii)
                        intent.putExtra("android.intent.extra.TEXT", "")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        val createChooser =
                            Intent.createChooser(intent, null as CharSequence?)
                        startActivity(createChooser)
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.passwrd_tool_container)
                    .setOnClickListener {
                    }
                bottomSheetView.findViewById<View>(R.id.split_tool_container)
                    .setOnClickListener {
                        val splitIntent = Intent(
                            requireActivity(),
                            SplitPDF::class.java
                        ).putExtra("FILESELECTED", titleBottom.pdfFilePath)
                        startActivity(splitIntent)
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.merge_tool_container)
                    .setOnClickListener {
                        val splitIntent = Intent(
                            requireActivity(),
                            MergePdf::class.java
                        ).putExtra("FILESELECTED", titleBottom.pdfFilePath)
                        startActivity(splitIntent)
                        bottomSheetDialog.dismiss()
                    }

                if (FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                    val imageview =
                        bottomSheetView.findViewById<AppCompatImageView>(R.id.fav_tool_image)
                    val textView = bottomSheetView.findViewById<TextView>(R.id.fav_tool_tv)
                    imageview.setImageResource(R.drawable.favoritesfilled)
                    textView.text = getString(R.string.removefav)
                }
                bottomSheetDialog.show()
            }

        })
        mAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {

                Log.e("isLONG","CALLE")

                val dashboard_checkbox = requireActivity().findViewById<AppCompatImageView>(R.id.userSelected)
                val all = requireActivity().findViewById<AppCompatImageView>(R.id.selectallfiles_btn)

                all.setImageResource(R.drawable.selectall_icon)
                dashboard_checkbox.visibility = View.VISIBLE

                dashboard_checkbox.performClick()
                adapter_AllFiles.isCheckbox = false
            }
        })
        if (!isGrid) {
            val gridLayoutManager = GridLayoutManager(requireContext(), 3)
            binding.recyclerview.layoutManager = gridLayoutManager
            mAdapter.setGridLayout(true)
            binding.recyclerview.adapter = mAdapter
        } else {
            val layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerview.layoutManager = layoutManager
            mAdapter.setGridLayout(false)
            binding.recyclerview.adapter = mAdapter
        }
    }

    private fun noList() {
        binding.loadingProgressBar.visibility = View.GONE
        binding.noFilesLayout.visibility = View.VISIBLE
    }

    fun initrecyclerview() {
        mAdapter = adapter_AllFiles(
            originalFileList,
            binding.recyclerview,
            listener,
            requireContext()
        )
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        mAdapter.setGridLayout(false)
        binding.recyclerview.adapter = mAdapter
        mAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                loadPreviousRecentFiles()
                val openlist = originalFileList[position]
                val file = File(openlist.pdfFilePath)
                if (file.exists() && file.isFile) {
                    val intent = Intent(context, viewerPdf::class.java)
                        .putExtra("key", file.absolutePath)
                    startActivity(intent)
                } else {
                    originalFileList.removeAt(position)
                    mAdapter.notifyItemRemoved(position)
                    Toast.makeText(requireContext(), "Sorry item not found!", Toast.LENGTH_SHORT)
                        .show()
                }
                if (!RecentFilesBook.allrecentFileList?.contains(openlist)!!) {
                    RecentFilesBook.allrecentFileList?.add(openlist)
                    saveRecentFilesList(RecentFilesBook.allrecentFileList!!)
                    Log.i("RECENTCHECK", "file added in recent")
                } else {
                    Log.i("RECENTCHECK", "file already in recent")
                }
                val current = Date()
                val format1 = SimpleDateFormat("hh:mm, MMM dd, yyyy")
                val currentdate = format1.format(current)
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                val ppp = file.parentFile?.absolutePath
                editor.putString(ppp, currentdate)
                editor.apply()
                Log.i("SELECTEDITEM", "root path set\t" + ppp)
            }

            @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
            override fun onBottomSheetItemClick(
                position: Int,
                titleBottom: FileItems,
                bm: Bitmap
            ) {
                loadPreviousFavFiles()
                val bottomSheetDialog =
                    BottomSheetDialog(requireContext(), R.style.SheetDialog)
                val bottomSheetView =
                    layoutInflater.inflate(R.layout.bottom_sheet_dailog, null)
                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetView.findViewById<AppCompatImageView>(R.id.imageView3)
                    .setImageBitmap(bm)
                bottomSheetView.findViewById<TextView>(R.id.textView3).text =
                    titleBottom.fileName
                bottomSheetView.findViewById<TextView>(R.id.textView4).text =
                    titleBottom.dateCreatedName
                bottomSheetView.findViewById<TextView>(R.id.text_size_bottom).text =
                    titleBottom.sizeName
                bottomSheetView.findViewById<TextView>(R.id.text_path_bottom).text =
                    titleBottom.pdfFilePath
                bottomSheetView.findViewById<View>(R.id.bottomsheet_topdetail)
                    .setOnClickListener {
                        val file = File(titleBottom.pdfFilePath)
                        val lastModDate = Date(file.lastModified())
                        val format1 = SimpleDateFormat("hh:mm, MMM dd, yyyy")
                        val moddate = format1.format(lastModDate)
                        val sharedPref =
                            requireActivity().getPreferences(Context.MODE_PRIVATE)
                        val ppp = file.parentFile?.absolutePath
                        Log.i("SELECTEDITEM", "root path get\t$ppp")
                        val lastviewdate = sharedPref.getString(ppp, null)
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.filedetail_dailog)
                        dialog.getWindow()!!
                            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window!!.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        dialog.setCancelable(true)
                        dialog.findViewById<TextView>(R.id.detail_dialog_name).text =
                            titleBottom.fileName
                        dialog.findViewById<TextView>(R.id.detail_dialog_path).text =
                            titleBottom.pdfFilePath

                        dialog.findViewById<TextView>(R.id.detail_dialog_lastmodified).text =
                            moddate
                        if (lastviewdate != null) {
                            dialog.findViewById<TextView>(R.id.detail_dialog_lastviewed).text =
                                lastviewdate
                        } else {
                            dialog.findViewById<TextView>(R.id.detail_dialog_lastviewed).visibility =
                                View.GONE
                            dialog.findViewById<TextView>(R.id.detail_dialog_lastviewed_title).visibility =
                                View.GONE
                        }
                        dialog.findViewById<TextView>(R.id.detail_dialog_size).text =
                            titleBottom.sizeName
                        dialog.findViewById<View>(R.id.ok_detail_dialog)
                            .setOnClickListener {
                                dialog.dismiss()
                            }
                        dialog.show()
                    }
                bottomSheetView.findViewById<View>(R.id.del_tool_container)
                    .setOnClickListener {
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.deleteitem_dailog)
                        dialog.setCancelable(true)
                        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
                        dialog.window!!.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val btdelete = dialog.findViewById<AppCompatButton>(R.id.deletebtn)
                        val btncancel =
                            dialog.findViewById<AppCompatButton>(R.id.cancel_button)
                        btncancel.setOnClickListener {
                            dialog.dismiss()
                            bottomSheetDialog.dismiss()
                        }
                        btdelete.setOnClickListener {
                            val fileName = titleBottom.fileName
                            val directoryPath = titleBottom.pdfFilePath
                            val parentDirPath = File(directoryPath).parent
                            val file = File(parentDirPath, fileName)
                            Log.v("File", "File name: $fileName")
                            Log.v("File", "Directory path: $directoryPath")
                            Log.v("File", "File exists: ${file.exists()}")
                            Log.v("File", "Is a file: ${file.isFile}")
                            if (file.exists()) {
                                if (file.delete()) {
                                    mAdapter.removeItem(position)
//                                    bitmaplist.removeAt(position)
                                    Toast.makeText(
                                        requireContext(),
                                        "File deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    try {
                                        if (FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                                            FvrtFilesBook.allfvrtFileList?.remove(titleBottom)
                                            savefavourteFilesList(
                                                FvrtFilesBook.allfvrtFileList!!,
                                                false
                                            )
                                        }
                                        if (RecentFilesBook.allrecentFileList?.contains(
                                                titleBottom
                                            )!!
                                        ) {
                                            RecentFilesBook.allrecentFileList?.remove(
                                                titleBottom
                                            )
                                            saveRecentFilesList(RecentFilesBook.allrecentFileList!!)
                                        }
                                    } catch (_: Exception) {
                                    }
                                    bottomSheetDialog.dismiss()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to delete file: ${file.path}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "File not found or is not a file: ${file.path}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            bottomSheetDialog.dismiss()
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                bottomSheetView.findViewById<View>(R.id.rename_tool_container)
                    .setOnClickListener {
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.rename_dailog)
                        dialog.setCancelable(true)
                        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
                        dialog.window!!.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        val okButton = dialog.findViewById<AppCompatButton>(R.id.okRename)
                        val cancelButton =
                            dialog.findViewById<AppCompatButton>(R.id.cancelRename)
                        okButton.setOnClickListener {
                            val existingFile = File(titleBottom.pdfFilePath)
                            val arrValues: Array<String> =
                                titleBottom.pdfFilePath.split(".").toTypedArray()
                            val newName =
                                dialog.findViewById<AppCompatEditText>(R.id.textView6).text.toString()
                            var newFilePath =
                                existingFile.parentFile?.absolutePath + "/$newName.${arrValues[arrValues.size - 1]}"
                            newFilePath = newFilePath.replace("//", "/")
                            val newFile = File(newFilePath + "")
                            if (newFile.exists()) {
                                Toast.makeText(
                                    requireContext(),
                                    "File with this name already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (existingFile.renameTo(newFile)) {
                                    // Update file path after renaming
                                    titleBottom.pdfFilePath = newFilePath
                                    val list = originalFileList[position]
                                    list.fileName = newName
                                    mAdapter.notifyItemChanged(position)
                                    Toast.makeText(
                                        requireContext(),
                                        "File renamed successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Log.d("RenameFile", "Failed to rename file")
                                    Log.e("RenameFile", "Error: ${existingFile}")
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to rename file",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            dialog.dismiss()
                        }
                        cancelButton.setOnClickListener {
                            dialog.dismiss()
                        }
                        val editText =
                            dialog.findViewById<AppCompatEditText>(R.id.textView6)
                        editText.setText(titleBottom.fileName)
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        editText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                // Show the drawableEnd if the text is not empty
                                if (!s.isNullOrEmpty()) {
                                    editText.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.edit_text_cut,
                                        0
                                    )
                                } else {
                                    editText.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        0,
                                        0
                                    )
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }
                        })

                        editText.setOnTouchListener { _, event ->
                            // Clear the text when the drawableEnd is clicked
                            if (event.action == MotionEvent.ACTION_UP) {
                                val drawableEnd = editText.compoundDrawables[2]
                                if (drawableEnd != null && event.rawX >= (editText.right - drawableEnd.bounds.width())) {
                                    editText.text = null
                                    return@setOnTouchListener true
                                }
                            }
                            return@setOnTouchListener false
                        }
                        dialog.show()
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.fav_tool_container)
                    .setOnClickListener {
                        if (!FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                            FvrtFilesBook.allfvrtFileList?.add(titleBottom)
                            savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, true)
                        } else {
                            FvrtFilesBook.allfvrtFileList?.remove(titleBottom)
                            savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, false)
                            Log.i("menu", "onMenuItemClick: file already in recent")
                        }
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.share_tool_container)
                    .setOnClickListener {
                        val uriii = FileProvider.getUriForFile(
                            activity!!,
                            activity!!.packageName + ".provider",
                            File(titleBottom.pdfFilePath)
                        )
                        val intent = Intent()
                        intent.action = "android.intent.action.SEND"
                        intent.type = "application/pdf"
                        intent.putExtra("android.intent.extra.STREAM", uriii)
                        intent.putExtra("android.intent.extra.TEXT", "")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        val createChooser =
                            Intent.createChooser(intent, null as CharSequence?)
                        startActivity(createChooser)
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.passwrd_tool_container)
                    .setOnClickListener {
                    }
                bottomSheetView.findViewById<View>(R.id.split_tool_container)
                    .setOnClickListener {
                        val splitIntent = Intent(
                            requireActivity(),
                            SplitPDF::class.java
                        ).putExtra("FILESELECTED", titleBottom.pdfFilePath)
                        startActivity(splitIntent)
                        bottomSheetDialog.dismiss()
                    }
                bottomSheetView.findViewById<View>(R.id.merge_tool_container)
                    .setOnClickListener {
                        val splitIntent = Intent(
                            requireActivity(),
                            MergePdf::class.java
                        ).putExtra("FILESELECTED", titleBottom.pdfFilePath)
                        startActivity(splitIntent)
                        bottomSheetDialog.dismiss()
                    }

                if (FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                    val imageview =
                        bottomSheetView.findViewById<AppCompatImageView>(R.id.fav_tool_image)
                    val textView = bottomSheetView.findViewById<TextView>(R.id.fav_tool_tv)
                    imageview.setImageResource(R.drawable.favoritesfilled)
                    textView.text = getString(R.string.removefav)
                }
                bottomSheetDialog.show()
            }

        })
        mAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {

                Log.e("isLONG","CALLE")

                val dashboard_checkbox = requireActivity().findViewById<AppCompatImageView>(R.id.userSelected)
                val all = requireActivity().findViewById<AppCompatImageView>(R.id.selectallfiles_btn)

                all.setImageResource(R.drawable.selectall_icon)
                dashboard_checkbox.visibility = View.VISIBLE

                dashboard_checkbox.performClick()
                adapter_AllFiles.isCheckbox = false
            }
        })
        binding.noFilesLayout.visibility = View.GONE

    }

    private fun saveRecentFilesList(recentFilesList: List<FileItems>) {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(recentFilesList)
        editor.putString("recent_files_list", json)
        editor.apply()
    }


    private fun savefavourteFilesList(fvrtFilesList: List<FileItems>, b: Boolean) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(fvrtFilesList)
        editor.putString("favourite_files_list", json)
        editor.apply()
        if (b) {
            Toast.makeText(requireContext(), "File added in favourite list.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                requireContext(),
                "File removed from favourite list.",
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.i(
            "RecentFragment",
            "saveRecentFilesList: recentFilesList saved to shared preferences, size = ${fvrtFilesList.size}"
        )
        Log.i("RECENTCHECK", "savefavourteFilesList size\t" + fvrtFilesList.size)
    }


    fun setDefault_icons(bottomSheetDialog: BottomSheetDialog) {
        bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_lastmodified)!!
            .setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.calendericon
                ),
                null,
                null,
                null
            )
        bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_names)!!
            .setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.sortname_icon
                ),
                null,
                null,
                null
            )
        bottomSheetDialog.findViewById<TextView>(R.id.dialogsort_filesize)!!
            .setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.sortfilesize_icon
                ),
                null,
                null,
                null
            )
    }

    class FishNameComparator(val type: Int, val order: String) : Comparator<FileItems?> {
        override fun compare(p0: FileItems?, p1: FileItems?): Int {
            if (type == 0 && order == "ASC") {
                return p0!!.originalDateCreated.compareTo(p1!!.originalDateCreated)
            } else if (type == 0 && order == "DESC") {
                return p1!!.originalDateCreated.compareTo(p0!!.originalDateCreated)
            }
            if (type == 1 && order == "DESC") {
                return p0!!.fileName.compareTo(p1!!.fileName)
            } else if (type == 1 && order == "ASC") {
                return p1!!.fileName.compareTo(p0!!.fileName)
            }
            if (type == 2 && order == "ASC") {
                return p0!!.originalSize.compareTo(p1!!.originalSize)
            } else if (type == 2 && order == "DESC") {
                return p1!!.originalSize.compareTo(p0!!.originalSize)
            }

            return 0
        }
    }

    fun getDate(millis: Long): String {
        val lastModDate = Date(millis)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return dateFormat.format(lastModDate)
    }

    fun loadPreviousRecentFiles() {
        val gson = Gson()
        val json = requireActivity().getPreferences(Context.MODE_PRIVATE)
            .getString("recent_files_list", null)
        if (!json.isNullOrEmpty()) {
            val recentFilesList = loadRecentFilesList(json, gson)
            RecentFilesBook.allrecentFileList!!.clear()
            if (recentFilesList.isNotEmpty()) {
                recentFilesList.forEach {
                    RecentFilesBook.allrecentFileList!!.add(it)
                    Log.i("RECENTCHECK", "recent size\t" + RecentFilesBook.allrecentFileList!!.size)
                }
            }
        }
    }

    fun loadPreviousFavFiles() {
        val gson = Gson()
        val json = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("favourite_files_list", null)
        if (!json.isNullOrEmpty()) {
            val favrtfilelist = loadfavrtFilesList(json, gson)
            FvrtFilesBook.allfvrtFileList!!.clear()
            if (favrtfilelist.isNotEmpty()) {
                favrtfilelist.forEach {
                    FvrtFilesBook.allfvrtFileList!!.add(it)
                    Log.i("RECENTCHECK", "recent size\t" + FvrtFilesBook.allfvrtFileList!!.size)
                }
            }
        }
    }


    private fun loadRecentFilesList(json: String, gson: Gson): List<FileItems> {
        Log.i("RecentFragment", "loadRecentFilesList: json = $json")
        val recentFilesList = gson.fromJson(json, Array<FileItems>::class.java)
        Log.i(
            "RecentFragment",
            "loadRecentFilesList: recentFilesList loaded from shared preferences, size = ${recentFilesList?.size}"
        )
        return recentFilesList.toList()
    }

    private fun loadfavrtFilesList(json: String, gson: Gson): List<FileItems> {
        val fvrtFilesList = gson.fromJson(json, Array<FileItems>::class.java)
        return fvrtFilesList.toList()
    }

}

