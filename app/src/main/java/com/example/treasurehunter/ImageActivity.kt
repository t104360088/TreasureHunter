package com.example.treasurehunter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.drawToBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_image.*
import java.io.IOException

class ImageActivity : AppCompatActivity() {
    private var answer = ""
    private var stageIndex = 0

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK) {
            val image = data?.extras?.get("data") ?: return
            val bitmap = image as Bitmap
            imageView.setImageBitmap(bitmap)
            recognizeImage(bitmap)
        } else if (requestCode == 1) {
            val imageUri = data?.data ?: return

            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            }

            imageView.setImageBitmap(bitmap)
            recognizeImage(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        intent.extras?.let {
            answer = it.getString("Answer") ?: return
            stageIndex = it.getInt("StageIndex")
            tv_hint.text = it.getString("Hint")
        }

        btn_photo.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(intent, 0)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this,
                    "No camera application", Toast.LENGTH_SHORT).show()
            }
        }

        btn_choose.setOnClickListener {
            val intent = Intent(ACTION_GET_CONTENT)
            intent.type = "image/*"
            try {
                startActivityForResult(intent, 1)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this,
                    "No album application", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recognizeImage(bitmap: Bitmap) {
        try {
            val labeler = ImageLabeling.getClient(
                ImageLabelerOptions.DEFAULT_OPTIONS
            )
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val result = arrayListOf<String>()
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        result.add("$text, 可信度：$confidence")

                    }
//                    listView.adapter = ArrayAdapter(this,
//                        android.R.layout.simple_list_item_1,
//                        result
//                    )

                    val isCorrect = labels.any { it.text.toLowerCase().contains(answer.toLowerCase()) }

                    if (isCorrect) {
                        val msg = "The answer is $answer"
                        DialogManager.instance.showCorrectMsg(this, msg)?.setOnClickListener {
                            DialogManager.instance.cancelDialog()

                            val intent = Intent()
                            intent.putExtra("NextStageIndex", stageIndex + 1)
                            setResult(0, intent)
                            finish()
                        }
                    } else {
                        DialogManager.instance.showWrongMsg(this)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}