package com.example.treasurehunter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: Adapter

    class Item(
        val res: Int,
        val title: String,
        val text: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAdapter()
        setIndicator()
        setListener()
    }

    private fun setAdapter() {
        val first = Item(R.raw.first,
            "Welcome to Treasure Hunter",
            "Play the game to win the award and enjoy it!")
        val second = Item(R.raw.second, "B", "B")
        val third = Item(R.raw.third, "B", "B")
        val fourth = Item(R.raw.fourth, "B", "B")
        val items = arrayListOf(first, second, third, fourth)

        adapter = Adapter(items)
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                btn_next.text = if (position == adapter.itemCount - 1) "Start" else "Next"
            }
        })
    }

    private fun setIndicator() {
        val indicators = arrayListOf<ImageView>()
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.marginStart = 8
        params.marginEnd = 8

        repeat(adapter.itemCount) {
            val image = ImageView(this)
            image.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_inactive))
            image.layoutParams = params
            indicators.add(image)
            linearLayout.addView(image)
        }

        setCurrentIndicator(0)
    }

    private fun setCurrentIndicator(position: Int) {
        (0 until linearLayout.childCount).forEach {
            val image = linearLayout.getChildAt(it) as ImageView
            val drawable = if (it == position) R.drawable.indicator else R.drawable.indicator_inactive
            image.setImageDrawable(ContextCompat.getDrawable(this, drawable))
        }
    }

    private fun setListener() {
        btn_next.setOnClickListener {
            val next = viewPager.currentItem + 1
            if (next < adapter.itemCount)
                viewPager.currentItem = next
            else {
                startActivity(Intent(this, MapActivity::class.java))
                finish()
            }
        }
    }
}