package com.raybritton.uiinspectorserver.ui.main

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.raybritton.uiinspectorserver.data.model.TreeNode
import java.util.*

interface TreeNodeRenderer {
    fun addView(view: View)
    fun setNodeViewMap(map: Map<String, TreeNode>)
    fun getContext(): Context
    fun removeAllViews()

    fun populate(views: List<TreeNode>) {
        removeAllViews()
        if (views.isEmpty()) return

        val nodeMap = mutableMapOf<String, TreeNode>()
        val layerMap = mutableMapOf<Int, FrameLayout>()
        views.forEach { node ->
            if (!layerMap.containsKey(node.indentLevel)) {
                layerMap[node.indentLevel] = FrameLayout(getContext())
            }
            val view = makeImageView(getContext(), node)
            layerMap[node.indentLevel]!!.addView(view)
            val id = UUID.randomUUID().toString()
            view.tag = id
            nodeMap[id] = node
        }

        layerMap.keys.iterator().forEachRemaining {
            if (layerMap[it + 1] != null) {
                layerMap[it]!!.addView(layerMap[it + 1])
            }
        }
        addView(layerMap[0]!!)
        setNodeViewMap(nodeMap)
    }

    private fun makeImageView(context: Context, node: TreeNode): ImageView {
        val view = ImageView(context)
        view.layoutParams = FrameLayout.LayoutParams(node.view.w, node.view.h).also {
            it.leftMargin = node.view.x
            it.topMargin = node.view.y
        }
        view.setImageBitmap(node.view.image)
        return view
    }

}

