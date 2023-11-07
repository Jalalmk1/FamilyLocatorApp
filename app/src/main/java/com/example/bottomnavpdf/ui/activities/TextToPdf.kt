package com.example.bottomnavpdf.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bottomnavpdf.databinding.ActivityTextToPdfBinding
import com.example.bottomnavpdf.utils.TextToPDFClass
import com.example.bottomnavpdf.utils.Variables
import java.io.File


class TextToPdf : AppCompatActivity() {

    private lateinit var binding: ActivityTextToPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextToPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.convertBtn.setOnClickListener {
            if (binding.name.text.isNullOrEmpty() || binding.text.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter name and text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val path = TextToPDFClass.convertTextToPdf(
                    this@TextToPdf,
                    binding.name.text.toString(),
                    binding.text.text.toString()
                )
                val pppp = File(path)
                val intent =
                    Intent(this@TextToPdf, viewerPdf::class.java).apply {
                        putExtra("key", pppp.path)
                    }
                finish()
                startActivity(intent)
                Variables.DATACHANGED=true
            }
        }
    }
}