package com.raybritton.uiinspectorserver.ui.main

import android.app.Application
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.raybritton.uiinspectorserver.R
import com.raybritton.uiinspectorserver.data.model.TreeNode
import kotlinx.android.synthetic.main.element_node.view.*

class MainAdapter @javax.inject.Inject constructor(application: Application): RecyclerView.Adapter<MainAdapter.TreeNodeViewHolder>() {
    interface NodeCallback {
        fun onRenderToggle(node: TreeNode)
        fun onNodeSelected(node: TreeNode)
    }

    private var list = listOf<TreeNode>()
    lateinit var listener: MainAdapter.NodeCallback

    private var selected = -1
    private val unselectedColour: Int = Color.parseColor("#eeeeee")
    private val selectedColour: Int = Color.parseColor("#cccccc")
    private var render = mutableListOf<Boolean>()
    private val indentSize = application.resources.getDimensionPixelSize(R.dimen.indent)

    fun setData(nodeList: List<TreeNode>) {
        list = nodeList
        render = ArrayList<Boolean>(list.size)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.TreeNodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_node, parent, false)
        return TreeNodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainAdapter.TreeNodeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class TreeNodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(node: TreeNode) {
//            itemView.render.setImageResource(if (render[adapterPosition]) R.drawable.ic_check_filled else R.drawable.ic_check_empty)
            itemView.indent.layoutParams = LinearLayout.LayoutParams(indentSize * node.indentLevel, LinearLayout.LayoutParams.WRAP_CONTENT)
            itemView.text.text = node.name
            if (selected == adapterPosition) {
                itemView.setBackgroundColor(selectedColour)
            } else {
                itemView.setBackgroundColor(unselectedColour)
            }
            itemView.setOnClickListener {
                listener.onNodeSelected(node)
            }
            itemView.render_toggle.setOnClickListener {
                listener.onRenderToggle(node)
            }
        }
    }

    fun select(node: TreeNode) {
        val old = selected
        selected = list.indexOf(node)
        notifyItemChanged(old)
        notifyItemChanged(selected)
    }

    fun selectFirst() {
        val old = selected
        selected = 0
        notifyItemChanged(old)
        notifyItemChanged(selected)
    }

    fun toggleRendering(node: TreeNode) {
        val idx = list.indexOf(node)
        render[idx] = !render[idx]
        notifyItemChanged(idx)
    }

    fun getIndex(node: TreeNode): Int {
        return list.indexOf(node)
    }
}
