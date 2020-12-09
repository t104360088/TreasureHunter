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
        val bgColor: Int,
        val anim: Int,
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
        val first = Item(R.color.yellow, R.raw.first,
            "Welcome to Treasure Hunter",
            "Hi, you are a hunter looking for treasures,\nthis is a game tailored for you, enjoy it！")
        val second = Item(R.color.green, R.raw.second,
            "Go to destination",
            "Reach the designated location based on the landmark")
        val third = Item(R.color.orange, R.raw.third,
            "Take pictures of the target",
            "Follow the prompts to find the answer and take a photo, and enter the next stage after the correct answer")
        val fourth = Item(R.color.purple, R.raw.fourth,
            "Win the treasure",
            "After completing the three stages, the map will show the location of the treasure")
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