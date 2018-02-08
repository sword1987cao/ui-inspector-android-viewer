package com.raybritton.uiinspectorserver.ui.main

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Base64
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import com.raybritton.uiinspectorserver.R

fun MainActivity.makeRow(key: String, value: String, convert: (String) -> String): android.widget.TableRow {
    if (value.startsWith("px")) {
        return makeRow(key, convert.invoke(value.substringAfter('|')))
    } else if (value.startsWith("str")) {
        return makeRow(key, value.substringAfter('|'))
    } else if (value.startsWith("bmp")) {
        val bitmapData = value.substringAfter('|').replace("\\", "")
        val bytes = Base64.decode(bitmapData, Base64.NO_WRAP)
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return makeRow(key, image)
    } else if (value.startsWith("clr")) {
        var colour = value.substringAfter('|')
        if (colour[0] != '#') {
            colour = "#" + colour
        }
        return makeRow(key, Color.parseColor(colour), colour)
    } else {
        return makeRow("Unknown: $key", value)
    }
}

private fun MainActivity.makeRow(leftText: String, rightText: String): android.widget.TableRow {
    val row = TableRow(this)
    val right = TextView(this)
    right.text = rightText
    row.addView(makeKeyView(leftText))
    row.addView(right)
    return row
}

private fun MainActivity.makeRow(leftText: String, rightImage: android.graphics.Bitmap): android.widget.TableRow {
    val row = TableRow(this)
    val right = ImageView(this)
    right.setImageBitmap(rightImage)
    row.addView(makeKeyView(leftText))
    row.addView(right)
    return row
}

private fun MainActivity.makeRow(leftText: String, colour: Int, hex: String): android.widget.TableRow {
    val row = TableRow(this)
    val right = TextView(this)
    val drawable = resources.getDrawable(R.drawable.square, theme).mutate()
    drawable.setColorFilter(colour, PorterDuff.Mode.SRC_ATOP)
    right.compoundDrawablePadding = PADDING
    right.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    right.text = hex
    row.addView(makeKeyView(leftText))
    row.addView(right)
    return row
}

private fun MainActivity.makeKeyView(leftText: String): android.widget.TextView {
    val left = TextView(this)
    left.text = leftText
    left.setPadding(PADDING, 0, PADDING, 0)
    return left
}