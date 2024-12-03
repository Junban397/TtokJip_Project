package com.example.ttokjip.ui

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import com.example.ttokjip.R

class LoadingDialog(context: Context) : Dialog(context, R.style.TransparentDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loding)

        val loadingImage = findViewById<ImageView>(R.id.loadingImage)
        val animation = loadingImage.drawable as AnimationDrawable
        animation.start() // 애니메이션 시작
    }
}