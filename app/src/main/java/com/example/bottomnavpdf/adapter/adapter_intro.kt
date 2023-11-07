//package com.example.bottomnavpdf.adapter
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.appcompat.widget.AppCompatImageView
//import androidx.viewpager.widget.PagerAdapter
//import com.example.bottomnavpdf.R
//
//class adapter_intro(val context: Context,var filelist: ArrayList<String>) : PagerAdapter() {
//
//    override fun getCount(): Int {
//      return  filelist.size
//    }
//
//    override fun isViewFromObject(view: View, `object`: Any): Boolean {
//        return view === `object`
//    }
//
//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val view=LayoutInflater.from(context).inflate(R.layout.intropageritems,container,false)
//        val  imageView=view.findViewById<AppCompatImageView>(R.id.imageview_viewpager_intro)
//        val  tv1=view.findViewById<TextView>(R.id.textView_viewpager_intro_counter)
//        val tv2=view.findViewById<TextView>(R.id.textView_viewpager_intro_title)
//        imageView.setImageResource(R.drawable.fav_img)
//        tv1.text="This is title"
//        tv2.text="This is Body"
//        container.addView(view)
//        return  view
//    }
//    override fun getItemPosition(`object`: Any): Int {
//        return super.getItemPosition(`object`)
//    }
//
//    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        container.removeView(`object` as View)
//    }
//}