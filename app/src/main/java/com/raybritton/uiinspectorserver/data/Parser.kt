package com.raybritton.uiinspectorserver.data

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.raybritton.uiinspectorserver.data.model.AndroidActivity
import com.raybritton.uiinspectorserver.data.parsers.AndroidActivityJsonParser
import com.raybritton.uiinspectorserver.data.parsers.AndroidViewParser
import com.raybritton.uiinspectorserver.data.parsers.MapParser

class Parser {
    private val gson: Gson
    init {
        val mapParser = MapParser()
        val viewParser = AndroidViewParser(mapParser)
        val activityParser = AndroidActivityJsonParser(viewParser)
        gson = GsonBuilder()
                .registerTypeAdapter(AndroidActivity::class.java, activityParser)
                .create()
    }

    fun parse(json: String) : AndroidActivity {
        val data = gson.fromJson<AndroidActivity>(json)
        return data
    }
}