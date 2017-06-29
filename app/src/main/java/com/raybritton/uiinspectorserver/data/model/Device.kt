package com.raybritton.uiinspectorserver.data.model

data class Device(val name: String, val code: Int, val status: Status) {
    override fun toString(): String {
        return name
    }

    enum class Status(val short: String = "", val message: String = "") {
        VALID, OUT_OF_DATE("Out of date", "App inspector library version is out of date, must be at least 1.3.0"), BAD_DATA("Bad data","Unable to parse data from app")
    }
}