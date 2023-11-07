package com.example.bottomnavpdf.ui.favourite

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.`interface`.Main_selectedItems
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.OnItemLongClickListener
import com.example.bottomnavpdf.adapter.AdapterFavouriteFiles
import com.example.bottomnavpdf.adapter.adapter_AllFiles
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.FragmentFavouriteBinding
import com.example.bottomnavpdf.ui.activities.MergePdf
import com.example.bottomnavpdf.ui.activities.SplitPDF
import com.example.bottomnavpdf.ui.activities.viewerPdf
import com.example.bottomnavpdf.utils.FvrtFilesBook
import com.example.bottomnavpdf.utils.RecentFilesBook
import com.example.bottomnavpdf.utils.Variables
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FavouriteFragment() : Fragment() {
        private lateinit var binding: FragmentFavouriteBinding
    private lateinit var adapter: AdapterFavouriteFiles
    private lateinit var listener:Main_selectedItems

    constructor(listener: Main_selectedItems):this(){
        this.listener = listener
    }
    companion object {
         var filesList = ArrayList<FileItems>()
        var type = 0
        var order = "DESC"
    }
    var isGrid = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite ,container, false)
        val root: View = binding.root
        Log.i("FVRTCHECK", "fvrt oncreate()\t")

        PreferenceManager.getDefaultSharedPreferences(context).let {
            val gson = Gson()
            val json = it.getString("favourite_files_list", null)
            if (!json.isNullOrEmpty()){
                val fvrtFilesList = loadfavrtFilesList(json,gson)
                Log.i("FVRTCHECK", "fvrt list size\t"+fvrtFilesList.size)
                if (fvrtFilesList.isNotEmpty()) {
                    setVisibility(View.GONE)
                    filesList.clear()
                    for (item in fvrtFilesList){
                        val file=File(item.pdfFilePath)
                        if (file.exists()){
                            filesList.add(item)
                        }
                    }
                    Collections.sort(
                        filesList,
                        FishNameComparator(type, order)
                    )
                    adapter = AdapterFavouriteFiles(requireContext(),filesList,binding.recyclerviewFavourite,listener)
                    initRecyclerView()
                } else {
                    setVisibility(View.VISIBLE)
                }
            }
            else {
                Log.i("FVRTCHECK", "json null in fvrts\t")
                setVisibility(View.VISIBLE)
            }
        }


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
                binding.recyclerviewFavourite.layoutManager = gridLayoutManager
                adapter.setGridLayout(true)
                isGrid = false
                binding.dashboardGridLinear.setImageResource(R.drawable.gridimage)
            }
            else{
                val layoutManager = LinearLayoutManager(requireContext())
                val anim = ScaleAnimation(
                    1f, 0.9f, 1f, 0.9f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                )
                anim.duration = 100
                anim.repeatCount = 1
                anim.repeatMode = Animation.REVERSE
                binding.dashboardGridLinear.startAnimation(anim)
                binding.recyclerviewFavourite.layoutManager = layoutManager
                adapter.setGridLayout(false)
                isGrid = true
                binding.dashboardGridLinear.setImageResource(R.drawable.listimage)
            }
        }
        return root
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
        fun onSelecteditemCall(isselected: Boolean) {
            try {
                adapter.change_menu_to_checkbox(isselected)
            } catch (_: Exception) {
            }
        }

    fun setVisibility(visibility: Int) {
        binding.nodatafound.visibility = visibility
    }

    fun sort(){
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val bottomSheetView = layoutInflater.inflate(R.layout.sortby_dailog, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        for (item in filesList) {
            Log.d("SORTEDITEM",item.originalSize.toString())
        }
        bottomSheetDialog.findViewById<View>(R.id.dialogsort_btn_ok)!!.setOnClickListener {
            Collections.sort(
                filesList,
                FishNameComparator(type, order)
            )
            adapter = AdapterFavouriteFiles(requireContext(),filesList,binding.recyclerviewFavourite,listener)
            initRecyclerView()
            for (item in filesList) {
                Log.d("SORTEDITEM",item.originalSize.toString())
            }
            bottomSheetDialog.dismiss()
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

            if (order == "DESC"){
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
            else{
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
    private fun initRecyclerView() {
      binding.recyclerviewFavourite.adapter = adapter
        binding.recyclerviewFavourite.layoutManager = LinearLayoutManager(requireContext())
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val openlistfav = filesList[position]
                val intent = Intent(requireContext(), viewerPdf::class.java)
                    .putExtra("key", openlistfav.pdfFilePath)
                startActivity(intent)
            }
            override fun onBottomSheetItemClick(mPosition: Int, titleBottom: FileItems, bm: Bitmap) {
                val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
                val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dailog, null)
                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetView.findViewById<AppCompatImageView>(R.id.imageView3).setImageBitmap(bm)
                bottomSheetView.findViewById<TextView>(R.id.textView3).text = titleBottom.fileName
                bottomSheetView.findViewById<TextView>(R.id.textView4).text =
                    titleBottom.dateCreatedName
                bottomSheetView.findViewById<TextView>(R.id.text_size_bottom).text =
                    titleBottom.sizeName
                bottomSheetView.findViewById<TextView>(R.id.text_path_bottom).text =
                    titleBottom.pdfFilePath
                bottomSheetView.findViewById<View>(R.id.bottomsheet_topdetail).setOnClickListener {
                    val file = File(titleBottom.pdfFilePath)
                    val lastModDate = Date(file.lastModified())
                    val format1 = SimpleDateFormat("hh:mm, MMM dd, yyyy")
                    val moddate = format1.format(lastModDate)
                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val ppp = file.parentFile?.absolutePath
                    Log.i("SELECTEDITEM", "root path get\t" + ppp)
                    val lastviewdate = sharedPref.getString(ppp, null)
                    val dialog = Dialog(requireContext())
                    dialog.setContentView(R.layout.filedetail_dailog)
                    dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.window!!.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    dialog.setCancelable(true)
                    dialog.findViewById<TextView>(R.id.detail_dialog_name).text =
                        titleBottom.fileName
                    dialog.findViewById<TextView>(R.id.detail_dialog_path).text =
                        titleBottom.pdfFilePath

                    dialog.findViewById<TextView>(R.id.detail_dialog_lastmodified).text = moddate
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
                    dialog.findViewById<View>(R.id.ok_detail_dialog).setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.show()
                }
                bottomSheetView.findViewById<View>(R.id.del_tool_container).setOnClickListener {
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
                    btdelete.setOnClickListener{
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
                                adapter.removeItem(mPosition)
                                Toast.makeText(
                                    requireContext(),
                                    "File deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Variables.DATACHANGED = true
                                try {
                                    if (FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                                        FvrtFilesBook.allfvrtFileList?.remove(titleBottom)
                                        savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!)
                                    }
                                    if (RecentFilesBook.allrecentFileList?.contains(titleBottom)!!) {
                                        RecentFilesBook.allrecentFileList?.remove(titleBottom)
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
                        }
                        else {
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
                bottomSheetView.findViewById<View>(R.id.rename_tool_container).setOnClickListener {
                    val dialog = Dialog(requireContext())
                    dialog.setContentView(R.layout.rename_dailog)
                    dialog.setCancelable(true)
                    dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
                    dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)

                    val okButton = dialog.findViewById<AppCompatButton>(R.id.okRename)
                    val cancelButton = dialog.findViewById<AppCompatButton>(R.id.cancelRename)
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
                                titleBottom.pdfFilePath = newFilePath
                                val list = filesList[mPosition]
                                list.fileName = newName
                                adapter.notifyItemChanged(mPosition)
                                Toast.makeText(
                                    requireContext(),
                                    "File renamed successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Variables.DATACHANGED = true
                            } else {
                                Log.d("RenameFile", "Failed to rename file")
                                Log.e("RenameFile", "Error: $existingFile")
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
                    val editText = dialog.findViewById<AppCompatEditText>(R.id.textView6)
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
                                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
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
                bottomSheetView.findViewById<View>(R.id.fav_tool_container).setOnClickListener {
                    if (!FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                        FvrtFilesBook.allfvrtFileList?.add(titleBottom)
                        savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!)
                        Toast.makeText(requireContext(),"File added in favourite list.",Toast.LENGTH_SHORT).show()
                    } else {
                        FvrtFilesBook.allfvrtFileList?.remove(titleBottom)
                        savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!)
                        adapter.removeItem(mPosition)
                        Log.i("menu", "onMenuItemClick: file already in recent")
                        Toast.makeText(requireContext(),"File removed from favourite list.",Toast.LENGTH_SHORT).show()
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
                if (FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!){
                    val imageview=  bottomSheetView.findViewById<AppCompatImageView>(R.id.fav_tool_image)
                    val textView=  bottomSheetView.findViewById<TextView>(R.id.fav_tool_tv)
                    imageview.setImageResource(R.drawable.favoritesfilled)
                    textView.text = getString(R.string.removefav)
                }
                bottomSheetDialog.show()
            }
        })
        adapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {

                val dashboard_checkbox = requireActivity().findViewById<AppCompatImageView>(R.id.userSelected)
                val all = requireActivity().findViewById<AppCompatImageView>(R.id.selectallfiles_btn)

                all.setImageResource(R.drawable.selectall_icon)
                dashboard_checkbox.visibility = View.VISIBLE

                dashboard_checkbox.performClick()
                adapter_AllFiles.isCheckbox = false
            }
        })
        if (filesList.isEmpty()) {
            setVisibility(View.GONE)
        }
    }
    private fun savefavourteFilesList(fvrtFilesList: List<FileItems>) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(fvrtFilesList)
        editor.putString("favourite_files_list", json)
        editor.apply()

    }
    private fun saveRecentFilesList(recentFilesList: List<FileItems>) {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(recentFilesList)
        editor.putString("recent_files_list", json)
        editor.apply()
        Log.i(
            "RecentFragment",
            "saveRecentFilesList: recentFilesList saved to shared preferences, size = ${recentFilesList.size}"
        )
    }
    private fun loadfavrtFilesList(json: String, gson: Gson): List<FileItems> {
        val fvrtFilesList = gson.fromJson(json, Array<FileItems>::class.java)
        return fvrtFilesList.toList()
    }
        class FishNameComparator(val type: Int,val order: String) : Comparator<FileItems?> {
            override fun compare(p0: FileItems?, p1: FileItems?): Int {
                if (type==0&& order == "ASC") {
                    return p0!!.originalDateCreated.compareTo(p1!!.originalDateCreated)
                }
                else if (type==0&& order == "DESC") {
                    return p1!!.originalDateCreated.compareTo(p0!!.originalDateCreated)
                }
                if (type==1&& order == "DESC") {
                    return p0!!.fileName.compareTo(p1!!.fileName)
                }
                else if (type==1&& order == "ASC") {
                    return p1!!.fileName.compareTo(p0!!.fileName)
                }
                if (type==2&& order == "ASC") {
                    return p0!!.originalSize.compareTo(p1!!.originalSize)
                }
                else if (type==2&& order == "DESC") {
                    return p1!!.originalSize.compareTo(p0!!.originalSize)
                }

                return 0
            }
        }
}