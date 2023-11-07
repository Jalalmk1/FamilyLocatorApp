package com.example.bottomnavpdf.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.adapter.ItemClickListener
import com.example.bottomnavpdf.adapter.adapter_AllFiles
import com.example.bottomnavpdf.adapter.adapter_SearchFiles
import com.example.bottomnavpdf.adapter.adapter_searchedhistory
import com.example.bottomnavpdf.dataModel.FileItems
import com.example.bottomnavpdf.databinding.ActivitySearchBinding
import com.example.bottomnavpdf.`interface`.Main_selectedItems
import com.example.bottomnavpdf.`interface`.OnItemClickListener
import com.example.bottomnavpdf.`interface`.myCallback
import com.example.bottomnavpdf.utils.FvrtFilesBook
import com.example.bottomnavpdf.utils.RecentFilesBook
import com.example.bottomnavpdf.utils.RecentFilesBook.allrecentFileList
import com.example.bottomnavpdf.utils.Variables
import com.example.bottomnavpdf.viewmodel.LoaderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SearchActivity : AppCompatActivity() {
    var set: Set<String> = HashSet()
    var historyfilessearched: ArrayList<String> = ArrayList()
    var itemsearched: String? = null
    private lateinit var loaderViewModel: LoaderViewModel
    private lateinit var mAdapter: adapter_SearchFiles
    lateinit var sharedPref: SharedPreferences
    private var originalFileList: List<FileItems> = ArrayList()
    val filteredlist: ArrayList<FileItems> = ArrayList()



    private lateinit var binding: ActivitySearchBinding

    companion object{
        var saveSearch:ArrayList<FileItems>? = ArrayList<FileItems>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        initViewModel()
        getFiles()
        initSearchView()
        binding.appCompatImageView.setOnClickListener {
            onBackPressed()
        }
        val gson = Gson()
        val json = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("favourite_files_list", null)
        if (!json.isNullOrEmpty()) {
            val fvrtFilesList = gson.fromJson(json, Array<FileItems>::class.java)
            val filesList = ArrayList<FileItems>()
            filesList.addAll(fvrtFilesList)
            FvrtFilesBook.allfvrtFileList!!.clear()
            FvrtFilesBook.allfvrtFileList = filesList
        }
    }

    fun showSearchHistory(){
        historyfilessearched.clear()
        val set: Set<String?>
        if (sharedPref.getStringSet("itemsall", null) != null) {
            set = sharedPref.getStringSet("itemsall", null)!!
            for (item in set) {
                if (!item.isNullOrEmpty()) {
                    Log.d("ITEMSTORED", "$item fetched\t")
                    historyfilessearched.add(item)

                    saveSearch = loadSearch()

                    if (saveSearch?.isNotEmpty() == true) {

                        saveSearch?.let {
                            val adapter = adapter_AllFiles(
                                saveSearch!!,
                                binding.recyclerview,
                                object : Main_selectedItems {
                                    override fun main_onItemselected(position: Int) {

                                    }
                                },
                                this , false
                            )
                            binding.recyclerhstorysearched.adapter = adapter
//                        binding.recyclerhstorysearched.layoutManager = GridLayoutManager(this, 4, RecyclerView.VERTICAL, true)
                            binding.recyclerhstorysearched.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

                            binding.recyclerview.visibility = View.GONE
                            binding.recyclerhstorysearched.visibility = View.VISIBLE
                            binding.linearLayoutCompatHistorytitle.visibility = View.VISIBLE
                        }

                        /* adapter.ItemClickListener(object : ItemClickListener {
                             override fun onItemClick(position: Int) {
                                 binding.searchView.setText(historyfilessearched.get(position))
                                 binding.searchView.setSelection(binding.searchView.length())//placing cursor at the end of the text
                             }
                         })*/
                    } else {
                        binding.recyclerhstorysearched.visibility = View.GONE
                        binding.linearLayoutCompatHistorytitle.visibility = View.GONE
                    }
                }
            }
        } else {
            binding.recyclerhstorysearched.visibility = View.GONE
            binding.linearLayoutCompatHistorytitle.visibility = View.GONE
        }

    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchView() {

        showSearchHistory()

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val text = p0.toString()
                if (text.isEmpty()) {
                    if (historyfilessearched.isNotEmpty()) {
                      /*  binding.recyclerhstorysearched.visibility = View.VISIBLE
                        binding.linearLayoutCompatHistorytitle.visibility = View.VISIBLE
                        //new fankari after 14 sep       ///////////start
                        val adapter =
                            adapter_searchedhistory(this@SearchActivity, historyfilessearched)
                        binding.recyclerhstorysearched.adapter = adapter
                        binding.recyclerhstorysearched.layoutManager =
                            GridLayoutManager(this@SearchActivity, 4, RecyclerView.VERTICAL, true)
                        binding.recyclerview.visibility = View.GONE
                        binding.recyclerhstorysearched.visibility = View.VISIBLE
                        binding.linearLayoutCompatHistorytitle.visibility = View.VISIBLE
                        adapter.ItemClickListener(object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                binding.searchView.setText(historyfilessearched.get(position))
                                binding.searchView.setSelection(binding.searchView.length())//placing cursor at the end of the text
                            }
                        })*/

                        showSearchHistory()
                    }
                    binding.searchView.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(this@SearchActivity, R.drawable.search_icon),
                        null
                    )
                    binding.recyclerview.visibility = View.GONE
                    noList()
                } else {
                    binding.searchView.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(this@SearchActivity, R.drawable.cross_btn2),
                        null
                    )
                    itemsearched = text
                    binding.recyclerhstorysearched.visibility = View.GONE
                    binding.recyclerview.visibility = View.VISIBLE
                    filteredlist.clear()
                    filter(text)
                }
            }
        })
        binding.searchView.setOnTouchListener { _, event ->
            // Clear the text when the drawableEnd is clicked
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.searchView.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (binding.searchView.right - drawableEnd.bounds.width())) {
                    binding.searchView.text = null
                    Log.d("SEARCHHISTORY", "clear text")
                    noList()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
        binding.searchedhistorydelete.setOnClickListener {
            val dialog = Dialog(this@SearchActivity)
            dialog.setContentView(R.layout.clearhistory_dailog)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val btdelete = dialog.findViewById<AppCompatButton>(R.id.deletebtn)
            val btncancel = dialog.findViewById<AppCompatButton>(R.id.cancel_button)

            btncancel.setOnClickListener {
                dialog.dismiss()
            }
            btdelete.setOnClickListener {
                val editor = sharedPref.edit()
                editor.remove("itemsall")
                editor.remove("search").apply()
                saveSearch?.clear()
                binding.recyclerhstorysearched.visibility = View.GONE
                historyfilessearched.clear()
                binding.linearLayoutCompatHistorytitle.visibility = View.GONE
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun initViewModel() {
        loaderViewModel = ViewModelProvider(this).get(LoaderViewModel::class.java)
    }

    private fun getFiles() {
        loaderViewModel.fetchPdfFiles(0, "DESC", object : myCallback {
            override fun callbackInvoke() {
                submitList()
            }
        })
    }

    private fun submitList() {
        if (!loaderViewModel.isFilesListEmpty()) {
            filteredlist.clear()
            binding.nodatafound.visibility = View.GONE
            originalFileList = loaderViewModel.fileList
            filteredlist.addAll(originalFileList)
            mAdapter = adapter_SearchFiles(filteredlist, this@SearchActivity)
            binding.recyclerview.adapter = mAdapter
            val layoutManager = LinearLayoutManager(this)
            binding.recyclerview.layoutManager = layoutManager
            mAdapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                    loadPreviousRecentFiles()

                    if (!itemsearched.isNullOrEmpty()) {
                        val editor11 = sharedPref.edit()
                        historyfilessearched.add(itemsearched!!)
                        val set: MutableSet<String> = HashSet()
                        set.addAll(historyfilessearched)
                        editor11.putStringSet("itemsall", set)
                        editor11.apply()
                    }
                    val openlist = filteredlist[position]
                    val file = File(openlist.pdfFilePath)
                    if (file.exists() && file.isFile) {
                        val intent = Intent(this@SearchActivity, viewerPdf::class.java)
                            .putExtra("key", file.absolutePath)
                        startActivity(intent)
                    }

                    val file2:FileItems? = saveSearch?.find { openlist.fileName == it.fileName }


                        saveSearch?.add(openlist)
                        saveSearch(saveSearch!!)


                    if (!RecentFilesBook.allrecentFileList?.contains(openlist)!!) {
                        RecentFilesBook.allrecentFileList?.add(openlist)
                        saveRecentFilesList(allrecentFileList!!)
                    } else {
                        Log.i("menu", "onMenuItemClick: file already in recent")

                    }
                    val current = Date()
                    val format1 = SimpleDateFormat("hh:mm, MMM dd, yyyy")
                    val currentdate = format1.format(current)
                    val editor = sharedPref.edit()
                    val ppp = file.parentFile?.absolutePath
                    editor.putString(ppp, currentdate)
                    editor.apply()
                    Log.i("SELECTEDITEM", "root path set\t" + ppp)
                }

                @SuppressLint("ClickableViewAccessibility")
                override fun onBottomSheetItemClick(
                    position: Int,
                    titleBottom: FileItems,
                    bm: Bitmap
                ) {

                    loadPreviousFavFiles()

                    val bottomSheetDialog =
                        BottomSheetDialog(this@SearchActivity, R.style.SheetDialog)
                    val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dailog, null)

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
                            val ppp = file.parentFile?.absolutePath
                            Log.i("SELECTEDITEM", "root path get\t" + ppp)
                            val lastviewdate = sharedPref.getString(ppp, null)
                            val dialog = Dialog(this@SearchActivity)
                            dialog.setContentView(R.layout.filedetail_dailog)
                            dialog.window!!
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
                            dialog.findViewById<View>(R.id.ok_detail_dialog).setOnClickListener {
                                dialog.dismiss()
                            }
                            dialog.show()
                        }
                    bottomSheetView.findViewById<View>(R.id.del_tool_container).setOnClickListener {
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
                                    this@SearchActivity,
                                    "File deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                bottomSheetDialog.dismiss()
                                Variables.DATACHANGED = true
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
                            } else {
                                Toast.makeText(
                                    this@SearchActivity,
                                    "Failed to delete file: ${file.path}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@SearchActivity,
                                "File not found or is not a file: ${file.path}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        bottomSheetDialog.dismiss()
                    }
                    bottomSheetView.findViewById<View>(R.id.rename_tool_container)
                        .setOnClickListener {
                            val dialog = Dialog(this@SearchActivity)
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
                                        this@SearchActivity,
                                        "File with this name already exists",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (existingFile.renameTo(newFile)) {
                                        // Update file path after renaming
                                        binding.searchView.text = null
                                        titleBottom.pdfFilePath = newFilePath
                                        originalFileList[position].fileName = newName
                                        mAdapter.notifyItemChanged(position)
                                        Toast.makeText(
                                            this@SearchActivity,
                                            "File renamed successfully" + originalFileList[position].fileName,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Variables.DATACHANGED = true

                                    } else {
                                        Log.d("RenameFile", "Failed to rename file")
                                        Log.e("RenameFile", "Error: $existingFile")
                                        Toast.makeText(
                                            this@SearchActivity,
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
                    bottomSheetView.findViewById<View>(R.id.fav_tool_container)
                        .setOnClickListener {
                            if (!FvrtFilesBook.allfvrtFileList?.contains(titleBottom)!!) {
                                FvrtFilesBook.allfvrtFileList?.add(titleBottom)
                                savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, true)
                            } else {
                                FvrtFilesBook.allfvrtFileList?.remove(titleBottom)
                                savefavourteFilesList(FvrtFilesBook.allfvrtFileList!!, true)
                                Log.i("menu", "onMenuItemClick: file already in recent")
                                Toast.makeText(
                                    this@SearchActivity,
                                    "File removed from favourite list.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            bottomSheetDialog.dismiss()
                        }
                    bottomSheetView.findViewById<View>(R.id.share_tool_container)
                        .setOnClickListener {
                            val uriii = FileProvider.getUriForFile(
                                this@SearchActivity,
                                packageName + ".provider",
                                File(titleBottom.pdfFilePath)
                            )
                            val intent = Intent()
                            intent.action = "android.intent.action.SEND"
                            intent.type = "application/pdf"
                            intent.putExtra("android.intent.extra.STREAM", uriii)
                            intent.putExtra("android.intent.extra.TEXT", "")
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            val createChooser = Intent.createChooser(intent, null as CharSequence?)
                            startActivity(createChooser)
                            bottomSheetDialog.dismiss()
                        }
                    bottomSheetView.findViewById<View>(R.id.split_tool_container)
                        .setOnClickListener {
                            val splitIntent = Intent(
                                this@SearchActivity,
                                SplitPDF::class.java
                            ).putExtra("FILESELECTED", titleBottom.pdfFilePath)
                            startActivity(splitIntent)
                            bottomSheetDialog.dismiss()
                        }
                    bottomSheetView.findViewById<View>(R.id.merge_tool_container)
                        .setOnClickListener {
                            val splitIntent = Intent(
                                this@SearchActivity,
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
        } else {
            noList()
        }
    }


    private fun saveSearch(recentFilesList: List<FileItems>) {

        Log.e("saveSearch","name = ${recentFilesList.get(0)}")
        val sharedPref = sharedPref
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(recentFilesList)
        editor.putString("searchFiles", json)
        editor.apply()
    }

    fun loadSearch(): ArrayList<FileItems>? {
        val gson = Gson()
        val json = sharedPref
            .getString("searchFiles", null)

        val list: ArrayList<FileItems>? = ArrayList()
        if (!json.isNullOrEmpty()) {
            val recentFilesList = loadRecentFilesList(json, gson)

            if (recentFilesList.isNotEmpty()) {
                recentFilesList.forEach {
                    list?.add(it)
                }
            }
        }

        return list
    }

    fun loadPreviousRecentFiles() {
        val gson = Gson()
        val json = getPreferences(Context.MODE_PRIVATE).getString("recent_files_list", null)
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

    private fun loadRecentFilesList(json: String, gson: Gson): List<FileItems> {
        Log.i("RecentFragment", "loadRecentFilesList: json = $json")
        val recentFilesList = gson.fromJson(json, Array<FileItems>::class.java)
        Log.i(
            "RecentFragment",
            "loadRecentFilesList: recentFilesList loaded from shared preferences, size = ${recentFilesList?.size}"
        )
        return recentFilesList.toList()
    }

    private fun saveRecentFilesList(recentFilesList: List<FileItems>) {
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

    private fun noList() {
        binding.nodatafound.visibility = View.GONE
    }

    private fun filter(text: String) {
        for (item in originalFileList) {
            if (item.fileName.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                Log.d("filterresult", "Filterd: ${item.fileName}")
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            Log.d("filterresult", "list is empty:")
            binding.recyclerview.visibility = View.GONE
            binding.nodatafound.visibility = View.VISIBLE
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            Log.d("filterresult", "list is not empty:\tsize" + filteredlist.size)
            binding.recyclerview.visibility = View.VISIBLE
            binding.nodatafound.visibility = View.GONE
            binding.recyclerhstorysearched.visibility = View.GONE
            binding.linearLayoutCompatHistorytitle.visibility = View.GONE
            mAdapter.filterList(filteredlist)
        }
    }

    private fun savefavourteFilesList(fvrtFilesList: List<FileItems>, b: Boolean) {
        Log.d("FVRTCHECK", "savefavourteFilesList size\t" + fvrtFilesList.size)
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        val gson = Gson()
        val json = gson.toJson(fvrtFilesList)
        editor.putString("favourite_files_list", json)
        editor.apply()
        if (b) {
            Toast.makeText(this, "File added in favourite list.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this,
                "File removed from favourite list.",
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.i(
            "RecentFragment",
            "saveRecentFilesList: recentFilesList saved to shared preferences, size = ${fvrtFilesList.size}"
        )
    }

    fun loadPreviousFavFiles() {
        val gson = Gson()
        val json = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("favourite_files_list", null)
        if (!json.isNullOrEmpty()) {
            val favrtfilelist = loadfavrtFilesList(json, gson)
            FvrtFilesBook.allfvrtFileList!!.clear()
            if (favrtfilelist.isNotEmpty()) {
                favrtfilelist.forEach {
                    FvrtFilesBook.allfvrtFileList!!.add(it)
                    Log.i("FVRTCHECK", "fvrt size\t" + FvrtFilesBook.allfvrtFileList!!.size)
                }
            }
        }
    }

    private fun loadfavrtFilesList(json: String, gson: Gson): List<FileItems> {
        val fvrtFilesList = gson.fromJson(json, Array<FileItems>::class.java)
        return fvrtFilesList.toList()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        historyfilessearched.clear()
        filteredlist.clear()
    }
}