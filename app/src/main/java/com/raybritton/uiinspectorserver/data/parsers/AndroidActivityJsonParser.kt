package com.raybritton.uiinspectorserver.data.parsers

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.raybritton.uiinspectorserver.data.model.AndroidActivity
import com.raybritton.uiinspectorserver.data.model.AndroidView
import java.util.*

class AndroidActivityJsonParser(val viewParser: AndroidViewParser) : TypeAdapter<AndroidActivity>() {

    override fun write(writer: JsonWriter, value: AndroidActivity) {
        throw NotImplementedError("Not implemented")
    }

    override fun read(reader: JsonReader): AndroidActivity? {
        reader.beginObject()
        var activityName: String? = null
        var deviceName: String? = null
        var bitmapData: String? = null
        var root: AndroidView? = null
        var density: Double = 1.0

        while(reader.hasNext()) {
            val name = reader.nextName().toLowerCase(Locale.US)
            when(name) {
                "title" -> activityName = reader.nextString()
                "background" -> bitmapData = reader.nextString()
                "layout" -> root = viewParser.read(reader)
                "density" -> density = reader.nextDouble()
                "device" -> deviceName = reader.nextString()
            }
        }
        reader.endObject()

        bitmapData = bitmapData!!.replace("\\","")
        val bytes = Base64.decode(bitmapData, Base64.NO_WRAP)
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return AndroidActivity(activityName!!, deviceName!!, image, root!!, density)
    }

}