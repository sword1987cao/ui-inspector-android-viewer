package com.raybritton.uiinspectorserver.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.f2prateek.rx.preferences2.Preference
import com.raybritton.uiinspectorserver.R
import com.raybritton.uiinspectorserver.data.model.Device
import com.raybritton.uiinspectorserver.data.model.TreeNode
import com.raybritton.uiinspectorserver.data.prefs.*
import com.raybritton.uiinspectorserver.ui.main.MainMvp.Unit.DP
import com.raybritton.uiinspectorserver.ui.main.MainMvp.Unit.PX
import com.raybritton.uiinspectorserver.ui.views.ScalpelLayout
import com.raybritton.uiinspectorserver.ui.views.VerticalSeekBar
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.element_attrs_settings.*
import kotlinx.android.synthetic.main.element_ui_settings.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainMvp.View {
    private val REQUEST_FILE = 100

    val PADDING by lazy { TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt() }

    @Inject
    lateinit var presenter: MainMvp.Presenter

    @Inject
    lateinit var adapter: MainAdapter

    @field:[Inject DimenUnit]
    lateinit var unit: Preference<MainMvp.Unit>

    @field:[Inject ShowEmptyAttrs]
    lateinit var showEmptyAttrs: Preference<Boolean>

    @field:[Inject ShowPadding]
    lateinit var showPadding: Preference<Boolean>

    @field:[Inject ShowMargin]
    lateinit var showMargin: Preference<Boolean>

    @field:[Inject ShowInvisibleViews]
    lateinit var showInvisible: Preference<Boolean>

    @field:[Inject ShowBorders]
    lateinit var showBorders: Preference<Boolean>

    private var dialog: AlertDialog? = null
    private var title: String = ""
    private var lastFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tree_list.layoutManager = LinearLayoutManager(this)
        tree_list.itemAnimator = DefaultItemAnimator()
        tree_list.adapter = adapter

        title = getString(R.string.title)
        setTitle(title)

        show_padding.setPreference(showPadding)
        showPadding.asObservable()
                .subscribe { image.setShowPadding(it) }

        show_margin.setPreference(showMargin)
        showMargin.asObservable()
                .subscribe { image.setShowMargin(it) }

        show_borders.setPreference(showBorders)
        showBorders.asObservable()
                .subscribe { image.setShowBorders(it) }

        show_invisible.setPreference(showInvisible)
        showInvisible.asObservable()
                .subscribe { image.setShowInvisibleViews(it) }

        show_empty.setPreference(showEmptyAttrs)

        when(unit.get()) {
            DP -> {
                dp.isSelected = true
                px.isSelected = false
            }
            PX -> {
                px.isSelected = true
                dp.isSelected = false
            }
        }

        dp.setOnClickListener {
            unit.set(DP)
            setTitle("$title (DP)")
            dp.isSelected = true
            px.isSelected = false
        }

        px.setOnClickListener {
            unit.set(PX)
            setTitle("$title (PX)")
            px.isSelected = true
            dp.isSelected = false
        }

        reset.setOnClickListener { image.reset() }

        seekbar.onPositionChangeListener = object : VerticalSeekBar.OnPositionChangeListener {
            override fun onPositionChange(value: Float) {
                image.setFocusPoint(value)
            }
        }
        seekbar.setPosition(0.5f)

        presenter.onCreate()

        image.listener = object : ScalpelLayout.OnClickListener {
            override fun onClick(x: Int, y: Int) {
                presenter.onImageTap(x, y)
            }
        }
    }

    override fun onBackPressed() {
        if (ui_settings_panel.visibility == View.VISIBLE) {
            toggleSettingsVisibility()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_empty -> {
                toggleSettingsVisibility()
            }
            R.id.action_reset -> presenter.reset()
            R.id.action_save -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    val editText = EditText(this)
                    editText.setText(lastFileName ?: title)
                    AlertDialog.Builder(this, R.style.SaveDialog)
                            .setTitle(getString(R.string.dialog_title))
                            .setView(editText)
                            .setPositiveButton(getString(R.string.dialog_share)) { _, _ ->
                                lastFileName = editText.text.toString()
                                presenter.share(lastFileName!!)
                            }
                            .setNeutralButton(getString(R.string.dialog_save)) { _, _ ->
                                lastFileName = editText.text.toString()
                                presenter.save(lastFileName!!)
                            }
                            .setNegativeButton(getString(R.string.dialog_cancel), null)
                            .show()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
                }
            }
            R.id.action_load -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(Intent.createChooser(intent, "Pick a file chooser"), REQUEST_FILE)
            }
        }
        supportInvalidateOptionsMenu()
        return true
    }

    private fun toggleSettingsVisibility() {
        if (ui_settings_panel.visibility == View.VISIBLE) {
            ui_settings_panel.visibility = View.GONE
            attrs_settings_panel.visibility = View.GONE
        } else {
            ui_settings_panel.visibility = View.VISIBLE
            attrs_settings_panel.visibility = View.VISIBLE
        }
    }

    override fun setStatus(message: String) {
        status.text = message
    }

    override fun showDeviceDialog(list: List<Device>) {
        dialog?.dismiss()

        if (list.isEmpty()) {
            return
        }

        dialog = AlertDialog.Builder(this)
                .setItems(list.map { it.makeName() }.toTypedArray()) { _, which -> presenter.connect(list[which]) }
                .create()
        dialog!!.show()
    }

    private fun Device.makeName(): String {
        when (status) {
            Device.Status.VALID -> return name
            else -> return name + " ${status.short}"
        }
    }

    override fun showAppUi(title: String, unit: MainMvp.Unit) {
        this.title = title
        setTitle("$title ($unit)")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILE && resultCode == Activity.RESULT_OK && data != null) {
            val file = contentResolver.openFileDescriptor(data.data, "r")
            presenter.load(file.fileDescriptor)
        }
    }

    override fun setupDetailPanel(node: TreeNode, convert: (String) -> String) {
        val span = SpannableStringBuilder(node.view.fqcn)
        if (node.view.fqcn.contains('.')) {
            span.setSpan(StyleSpan(Typeface.BOLD), node.view.fqcn.lastIndexOf('.') + 1, node.view.fqcn.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        class_name.text = span
        size.text = "x: ${convert.invoke(node.view.x.toString())}  y: ${convert.invoke(node.view.y.toString())}  w: ${convert.invoke(node.view.w.toString())}  h: ${convert.invoke(node.view.h.toString())}"
        params_table.removeAllViews()
        node.view.params.forEach {
            params_table.addView(makeRow(it.key, it.value, convert))
        }
        attrs_table.removeAllViews()
        node.view.attr.forEach {
            attrs_table.addView(makeRow(it.key, it.value, convert))
        }
        tree_list.layoutManager.smoothScrollToPosition(tree_list, null, adapter.getIndex(node))
    }

    override fun createLayerChecks(size: Int) {
//        val listener = CompoundButton.OnCheckedChangeListener { view, checked -> presenter.onLayerToggled(view.tag as Int, checked) }
        layer_checks.removeAllViews()
        (0 until (size + 1)).forEach { idx ->
            val view = CheckBox(this).apply {
                this.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                this.isChecked = true
                this.tag = idx
//                this.setOnCheckedChangeListener(listener)
            }
            layer_checks.addView(view)
        }
    }

    override fun showUi(list: List<TreeNode>) {
        image.populate(list)
    }

    override fun showSaveComplete() {
        Toast.makeText(this, "Saved to downloads folder", Toast.LENGTH_SHORT).show()
    }

    override fun toggleRendering(node: TreeNode) {
        adapter.toggleRendering(node)
    }

    override fun setHighlightedNode(node: TreeNode) {
        image.setHighlight(node)
    }

    override fun startShareScreen(file: Uri) {
        var intent = Intent(Intent.ACTION_SEND)
        intent.setDataAndType(file, "application/*")
        intent = Intent.createChooser(intent, "Share")
        startActivity(intent)
    }

    override fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        presenter.reset()
    }

    override fun showLoadError() {
        Toast.makeText(this, "Invalid UI json", Toast.LENGTH_SHORT).show()
    }
}
