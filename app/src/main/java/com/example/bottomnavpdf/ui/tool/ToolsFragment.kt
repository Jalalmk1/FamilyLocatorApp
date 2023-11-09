package com.example.bottomnavpdf.ui.tool

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.bottomnavpdf.R
import com.example.bottomnavpdf.databinding.FragmentToolsBinding
import com.example.bottomnavpdf.ui.activities.*
import com.usman.smartads.AdManager

class ToolsFragment(mainActivity: MainActivity) : Fragment() {
    private lateinit var binding: FragmentToolsBinding
    var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tools, container, false)
        val root: View = binding.root
        binding.cardViewTexttoPdf.setOnClickListener {
            AdManager.showInterstitialAd(requireContext() as Activity?,"TOOLS_TEXT_TO_PDF_PLACEMENT"){
                val textToPdfIntent = Intent(requireActivity(), TextToPdf::class.java)
                startActivity(textToPdfIntent)
            }
        }
        binding.cardViewImgtoPdf.setOnClickListener {
            AdManager.showInterstitialAd(requireContext() as Activity?,"TOOLS_IMAGE_TO_PDF_PLACEMENT"){
                val ImageToPdfIntent = Intent(requireActivity(), Images_to_pdf_Activity::class.java)
                startActivity(ImageToPdfIntent)
            }
        }
        binding.cardViewprint.setOnClickListener {
            AdManager.showInterstitialAd(requireContext() as Activity?,"TOOLS_PRINT_PDF_PLACEMENT"){
                startActivity(Intent(requireActivity(), print_doc_activity::class.java))
            }
        }
        binding.cardViewbrowse.setOnClickListener {
            selectPDF()
        }


        binding.cardViewMerge.setOnClickListener {
            AdManager.showInterstitialAd(requireContext() as Activity?,"TOOLS_MERGE_PDF_PLACEMENT"){
                val mergeIntent = Intent(requireActivity(), MergePdf::class.java).putExtra("FILESELECTED", "")
                startActivity(mergeIntent)
            }
        }
        binding.cardViewSplit.setOnClickListener {
            AdManager.showInterstitialAd(requireContext() as Activity?,"TOOLS_SPLIT_PDF_PLACEMENT"){
                SplitPDF.file_alreadyselected=null
                val splitIntent = Intent(requireActivity(), SplitPDF::class.java).putExtra("FILESELECTED", "")
                startActivity(splitIntent)
            }
        }
//         Initialize result launcher
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val sUri = result.data!!.data
                viewerPdf.uriBybrowse = sUri
                val intent = Intent(requireContext(), viewerPdf::class.java)


                startActivity(intent)
            } catch (_: Exception) {
            }
        }
        return root
    }

    private fun selectPDF() {
        // Initialize intent
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        resultLauncher!!.launch(intent)
    }

}