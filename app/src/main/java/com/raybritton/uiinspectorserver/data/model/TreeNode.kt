package com.raybritton.uiinspectorserver.data.model

data class TreeNode(val id: Int, val name: String, val view: AndroidView, val indentLevel: Int) {

    fun cloneWithoutImage(includeEmpty: Boolean): TreeNode {
        var params: Map<String, String> = view.params.toMutableMap()
        var attrs: Map<String, String> = view.attr.toMutableMap()
        if (!includeEmpty) {
            params = params.filterNot { isZero(it.value) }
            attrs = attrs.filterNot { isZero(it.value) }
        }
        return copy(view = view.copy(image = null, params = params, attr = attrs))
    }

    private fun isZero(value: String): Boolean {
        if (value.isNullOrBlank()) {
            return true
        }
        when (value.substringAfter('|')) {
            "0.0", "0", "-" -> return true
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        return if (other is TreeNode) other.id == id else false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
