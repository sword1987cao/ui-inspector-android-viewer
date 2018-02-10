package com.raybritton.uiinspectorserver.ui.main

import android.net.Uri
import android.os.Environment
import com.f2prateek.rx.preferences2.Preference
import com.raybritton.server_comm.Device
import com.raybritton.server_comm.Server
import com.raybritton.uiinspectorserver.data.IpProvider
import com.raybritton.uiinspectorserver.data.Parser
import com.raybritton.uiinspectorserver.data.model.AndroidActivity
import com.raybritton.uiinspectorserver.data.model.AndroidView
import com.raybritton.uiinspectorserver.data.model.TreeNode
import com.raybritton.uiinspectorserver.data.prefs.DimenUnit
import com.raybritton.uiinspectorserver.data.prefs.ShowEmptyAttrs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

class MainPresenter @javax.inject.Inject constructor(
        val view: MainMvp.View,
        val ctx: android.app.Application,
        val adapter: MainAdapter,
        @ShowEmptyAttrs val showEmptyAttrs: Preference<Boolean>,
        @DimenUnit val unit: Preference<MainMvp.Unit>) : MainMvp.Presenter {

    private val VERSION = 1

    private lateinit var server: Server
    private var statusSubscription: Disposable? = null
    private var deviceSubscription: Disposable? = null
    private var jsonSubscription: Disposable? = null
    private var selectedNode: TreeNode? = null
    private lateinit var activity: AndroidActivity
    private var json: String? = null
    private var list = listOf<TreeNode>()
    private var lastTreeNodeId = 0

    override fun onCreate() {
        val ipProvider = IpProvider(ctx)
        statusSubscription?.dispose()
        deviceSubscription?.dispose()
        jsonSubscription?.dispose()
        view.setStatus("Waiting for device")
        server = Server(ipProvider::getIpAddress, 13588)

        adapter.listener = this

        statusSubscription = server.status()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ msg ->
                    view.setStatus(msg)
                }, { t ->
                    view.setStatus(t.message!!)
                    Timber.d(t, "status")
                })

        jsonSubscription = server.json()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message ->
                    handleJson(message)
                }, { ex ->
                    Timber.e(ex, "unable to parse json")
                    view.setStatus("Bad data: ${ex.message}")
                })

        deviceSubscription = server.devices()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list -> view.showDeviceDialog(list) }

        Observable.combineLatest(showEmptyAttrs.asObservable(), unit.asObservable(), BiFunction<Boolean, MainMvp.Unit, Boolean> { _, _ -> true })
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (selectedNode != null) {
                        onNodeSelected(selectedNode!!)
                    }
                }
    }

    override fun onDestroy() {
        statusSubscription?.dispose()
        deviceSubscription?.dispose()
        jsonSubscription?.dispose()
    }

    override fun connect(device: Device) {
        when (device.status) {
            Device.Status.VALID -> server.connect(device)
            else -> view.showAlert(device.name, device.status.message)
        }
    }

    override fun reset() {
        onCreate()
    }

    override fun save(name: String) {
        if (json != null) {
            internalSave(name)
        }
    }

    private fun internalSave(name: String): File {
        val dir = File(Environment.getExternalStorageDirectory(), "Download")
        dir.mkdirs()
        val file = File(dir, "$name.uijson")
        file.writeText("${VERSION}_$json")
        view.showSaveComplete()
        return file
    }

    override fun share(name: String) {
        if (json != null) {
            view.startShareScreen(Uri.fromFile(internalSave(name)))
        }
    }

    override fun load(file: FileDescriptor) {
        val json = FileInputStream(file).bufferedReader().readLine()
        if (json.startsWith("${VERSION}_json")) {
            handleJson(json.substringAfter("_"))
            view.setStatus("Displaying data from file")
        } else {
            view.showLoadError()
        }
    }

    private fun makeTreeNode(view: AndroidView, list: MutableList<TreeNode>, indent: Int) {
        list.add(TreeNode(lastTreeNodeId++, view.humanOverride ?: view.simpleName, view, indent))
        view.children
                .forEach { makeTreeNode(it, list, indent + 1) }
    }

    private fun handleJson(message: String) {
        json = message
        activity = Parser().parse(message.substringAfter("|"))
        val tempList = mutableListOf<TreeNode>()
        makeTreeNode(activity.layout, tempList, 0)
        list = tempList.toList()

        adapter.setData(list)
        view.createLayerChecks(list.map { it.indentLevel }.max()!!)
        view.showAppUi("${activity.title} on ${activity.deviceName}", unit.get())
        adapter.selectFirst()
        view.showUi(list)
    }

    private fun convert(px: String): String {
        if (unit.get() == MainMvp.Unit.PX) {
            return px
        } else {
            var dp = px.toDouble()
            dp /= activity.density
            return dp.toInt().toString()
        }
    }

    override fun checksUpdated() {
        if (selectedNode != null) {
            onNodeSelected(selectedNode!!)
        }
    }

    override fun onImageTap(x: Int, y: Int) {
//        val node = activeRenderer.getNodeAt(x, y)
//        if (node != null) {
//            onNodeSelected(node)
//        }
    }

    override fun onRenderToggle(node: TreeNode) {
        view.toggleRendering(node)
    }

    override fun onNodeSelected(node: TreeNode) {
        this.selectedNode = node
        view.setupDetailPanel(node.cloneWithoutImage(showEmptyAttrs.get()), this::convert)
        adapter.select(node)
        view.setHighlightedNode(node)
    }

}
