package com.raybritton.uiinspectorserver.data.model

import android.graphics.Bitmap

data class AndroidActivity(
        val title: String,
        val deviceName: String,
        val image: Bitmap,
        val layout: AndroidView,
        val density: Double)