package com.example.bottomnavpdf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavpdf.R

class adapter_searchedhistory(val context: Context, val list: ArrayList<String>) :
    RecyclerView.Adapter<adapter_searchedhistory.myviewholder>() {
    private var mListener: ItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myviewholder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.single_searchedhistory, parent, false)
        return myviewholder(view)
    }

    fun ItemClickListener(listener: ItemClickListener) {
        mListener = listener
    }

    override fun onBindViewHolder(holder: myviewholder, position: Int) {
        holder.textView.text = list[position]
        holder.mainlayout.setOnClickListener {
            mListener!!.onItemClick(position)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class myviewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.searchedhistory_tv)
        val mainlayout: CardView = itemView.findViewById(R.id.searchcardview)
    }
}

interface ItemClickListener {
    fun onItemClick(position: Int)
}