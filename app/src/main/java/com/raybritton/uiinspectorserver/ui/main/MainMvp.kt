package com.raybritton.uiinspectorserver.ui.main

import android.net.Uri
import com.raybritton.server_comm.Device
import com.raybritton.uiinspectorserver.data.model.TreeNode
import java.io.FileDescriptor

interface MainMvp {
    enum class Unit {
        DP, PX
    }

    interface View {
        fun setStatus(message: String)
        fun showDeviceDialog(list: List<Device>)
        fun showAppUi(title: String, unit: Unit)
        fun setupDetailPanel(node: TreeNode, convert: (String) -> String)
        fun startShareScreen(file: Uri)
        fun showLoadError()
        fun showSaveComplete()
        fun createLayerChecks(size: Int)
        fun toggleRendering(node: TreeNode)
        fun showUi(list: List<TreeNode>)
        fun setHighlightedNode(node: TreeNode)
        fun showAlert(title: String, message: String)
    }

    interface Presenter : MainAdapter.NodeCallback {
        override fun onRenderToggle(node: TreeNode);
        override fun onNodeSelected(node: TreeNode);
        fun onCreate()
        fun connect(device: Device)
        fun reset()
        fun save(name: String)
        fun load(file: FileDescriptor)
        fun share(name: String)
        fun checksUpdated()
        fun onImageTap(x: Int, y: Int)
    }
}
