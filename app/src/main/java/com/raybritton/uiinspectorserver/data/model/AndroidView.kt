package com.raybritton.uiinspectorserver.data.model

import android.graphics.Bitmap
import java.util.*

/**
 * fqcn = Fully Qualified Class Name i.e. com.android.widget.LinearLayout
 */
data class AndroidView(
        val fqcn: String,
        val image: Bitmap?,
        val x: Int,
        val y: Int,
        val w: Int, //width
        val h: Int, //height
        val attr: Map<String, String> = HashMap(),
        val params: Map<String, String> = HashMap(),
        val humanOverride: String? = null,
        val children: List<AndroidView> = ArrayList()) {

    val simpleName: String
    init {
        if(fqcn.contains('.')) {
            simpleName = fqcn.substring(fqcn.lastIndexOf('.') + 1)
        } else {
            simpleName = fqcn
        }
    }

    override fun toString(): String {
        return humanOverride ?: simpleName
    }

    private fun convertPx(dimen: String?): Int {
        if (dimen == null) {
            return 0
        }
        return dimen.split("|")[1].toInt()
    }

    val leftPadding by lazy { convertPx(attr["padding_left"]).toFloat() }
    val rightPadding by lazy { convertPx(attr["padding_right"]).toFloat() }
    val topPadding by lazy { convertPx(attr["padding_top"]).toFloat() }
    val bottomPadding by lazy { convertPx(attr["padding_bottom"]).toFloat() }

    val leftMargin by lazy { convertPx(params["margin_left"]).toFloat() }
    val rightMargin by lazy { convertPx(params["margin_right"]).toFloat() }
    val topMargin by lazy { convertPx(params["margin_top"]).toFloat() }
    val bottomMargin by lazy { convertPx(params["margin_bottom"]).toFloat() }

    val alpha by lazy {
        if (attr.containsKey("alpha")) {
            attr["alpha"]!!.split("|")[1].toFloat()
        } else {
            1.0f
        }
    }

    val isVisible by lazy {
        attr["visibility"] == "str|visible"
    }
}
