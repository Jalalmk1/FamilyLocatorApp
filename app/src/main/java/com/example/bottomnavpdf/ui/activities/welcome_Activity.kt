//package com.example.bottomnavpdf.ui.activities
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.viewpager.widget.ViewPager.OnPageChangeListener
//import com.example.bottomnavpdf.adapter.adapter_intro
//import com.example.bottomnavpdf.databinding.ActivityWelcomeBinding
//
//class welcome_Activity : AppCompatActivity() {
//    lateinit var binding: ActivityWelcomeBinding
//    var fileList: ArrayList<String> = ArrayList()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityWelcomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        binding.tabdots.setupWithViewPager(binding.viewPagerIntro)
//        fileList.add("intro1")
//        fileList.add("intro2")
//        fileList.add("intro3")
//        val adapter = adapter_intro(this, fileList)
//        binding.viewPagerIntro.adapter=adapter
//        binding.viewPagerIntro.addOnPageChangeListener(object :OnPageChangeListener{
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//
//            }
//
//            override fun onPageSelected(position: Int) {
//
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//
//            }
//
//        })
//    }
//}