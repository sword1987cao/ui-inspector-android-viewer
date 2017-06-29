package com.raybritton.uiinspectorserver.data.parsers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.raybritton.uiinspectorserver.data.model.AndroidView
import java.util.*

class AndroidViewParser(val mapParser: MapParser) : TypeAdapter<AndroidView>() {
    override fun write(writer: JsonWriter, value: AndroidView) {
        throw UnsupportedOperationException("Not supported")
    }

    override fun read(reader: JsonReader): AndroidView? {
        reader.beginObject()
        var fqcn: String? = null
        var bitmapData: String? = null
        var x: Int? = 0
        var y: Int? = 0
        var w: Int? = 0
        var h: Int? = 0
        var attr: Map<String, String> = HashMap()
        var params: Map<String, String> = HashMap()
        var humanName: String? = null
        val children: MutableList<AndroidView> = mutableListOf()

        while (reader.hasNext()) {
            val name = reader.nextName().toLowerCase(Locale.US)
            when (name) {
                "fqcn" -> fqcn = reader.nextString()
                "bmp" -> bitmapData = reader.nextString()
                "x" -> x = reader.nextInt()
                "y" -> y = reader.nextInt()
                "w" -> w = reader.nextInt()
                "h" -> h = reader.nextInt()
                "attrs" -> attr = mapParser.read(reader)
                "params" -> params = mapParser.read(reader)
                "humanoverride" -> humanName = reader.nextString()
                "children" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val view = read(reader)
                        if (view != null) {
                            children.add(view)
                        }
                    }
                    reader.endArray()
                }
            }
        }
        reader.endObject()

        var image: Bitmap? = null
        if (bitmapData != null && bitmapData.isNotEmpty()) {
            bitmapData = bitmapData.replace("\\", "")
            val bytes = Base64.decode(bitmapData, Base64.NO_WRAP)
            image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        return AndroidView(fqcn!!, image, x!!, y!!, w!!, h!!, attr, params, humanName, children.toList())
    }

}