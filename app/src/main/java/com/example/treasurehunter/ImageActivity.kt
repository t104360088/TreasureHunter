package com.example.treasurehunter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.drawToBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_image.*
import java.io.IOException

class ImageActivity : AppCompatActivity() {
    private var angle = 0f

    //取得返回的影像資料
    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //識別返回對象及執行結果
        if (requestCode == 0 && resultCode == RESULT_OK) {
            val image = data?.extras?.get("data") ?: return //取得資料
            val bitmap = image as Bitmap //將資料轉換成Bitmap
            imageView.setImageBitmap(bitmap) //使用Bitmap設定圖像
            recognizeImage(bitmap) //使用Bitmap進行辨識
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_photo.setOnClickListener {
            //建立一個要進行影像獲取的Intent物件
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //用try-catch避免例外產生，若產生則顯示Toast
            try {
                startActivityForResult(intent, 0) //發送Intent
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this,
                    "此裝置無相機應用程式", Toast.LENGTH_SHORT).show()
            }
        }

        btn_rotate.setOnClickListener {
            angle += 90f //原本角度再加上90度
            imageView.rotation = angle //使ImageView旋轉
            recognizeImage(imageView.drawToBitmap()) //取得Bitmap後進行辨識
        }
    }

    //辨識圖像
    private fun recognizeImage(bitmap: Bitmap) {
        try {
            //取得辨識標籤
            val labeler = ImageLabeling.getClient(
                ImageLabelerOptions.DEFAULT_OPTIONS
            )
            //建立InputImage物件
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            //匹配辨識標籤與圖像，並建立執行成功與失敗的監聽器
            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    //取得辨識結果與可信度
                    val result = arrayListOf<String>()
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        result.add("$text, 可信度：$confidence")
                    }
                    //將結果顯示於ListView
                    listView.adapter = ArrayAdapter(this,
                        android.R.layout.simple_list_item_1,
                        result
                    )
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,
                        "發生錯誤", Toast.LENGTH_SHORT).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}