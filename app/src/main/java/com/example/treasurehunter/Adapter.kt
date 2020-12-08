package com.example.treasurehunter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

class Adapter(private val items: List<MainActivity.Item>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv_title = v.findViewById<TextView>(R.id.tv_title)
        val tv_text = v.findViewById<TextView>(R.id.tv_text)
        val view_anim = v.findViewById<LottieAnimationView>(R.id.view_anim)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.item_container, parent, false
        )
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items.getOrNull(position)?.let {
            holder.tv_title.text = it.title
            holder.tv_text.text = it.text
            holder.view_anim.setAnimation(it.res)
        }
    }
}