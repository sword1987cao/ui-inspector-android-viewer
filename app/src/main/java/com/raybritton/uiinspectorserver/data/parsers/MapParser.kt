package com.raybritton.uiinspectorserver.data.parsers

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

class MapParser: TypeAdapter<Map<String, String>>() {
    override fun write(writer: JsonWriter, value: Map<String, String>) {
        throw NotImplementedError("Not implemented")
    }

    override fun read(reader: JsonReader): Map<String, String> {
        reader.beginObject()
        val map = mutableMapOf<String, String>()
        while(reader.hasNext()) {
            map.put(reader.nextName(), reader.nextString())
        }
        reader.endObject()
        return HashMap(map)
    }

}