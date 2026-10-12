package io.github.khivus.torrserveflow.ui.fragments.play.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.text.LineBreaker
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.ColorUtils
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.server.api.Viewed
import io.github.khivus.torrserveflow.server.models.torrent.FileStat
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import io.github.khivus.torrserveflow.utils.Format
import io.github.khivus.torrserveflow.utils.ThemeUtil
import io.github.khivus.torrserveflow.utils.TorrentHelper
import io.github.khivus.torrserveflow.utils.TorrentFileLabel
import io.github.khivus.torrserveflow.utils.TorrentFileLabels
import java.io.File


class TorrentFilesAdapter : BaseAdapter() {
    private var files: List<FileStat> = listOf()
    private var labels: List<TorrentFileLabel> = emptyList()
    private val typeFile = 0
    private val typeButton = 1
    var viewed = listOf<Viewed>()

    fun update(torrent: Torrent, viewed: List<Viewed>?) {
        files = TorrentHelper.getPlayableFiles(torrent)
        labels = TorrentFileLabels.create(torrent.name.ifBlank { torrent.title }, files.map { it.path })
        if (viewed != null) this.viewed = viewed
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val type: Int = getItemViewType(position)
        val themedContext = ContextThemeWrapper(App.context, ThemeUtil.selectedTheme)
        val colorBright = ThemeUtil.getColorFromAttr(themedContext, R.attr.colorBright)
        val color1 = ColorUtils.setAlphaComponent(ThemeUtil.getColorFromAttr(themedContext, R.attr.colorBright), 140)
        // round labels model
        val radius = Format.dp2px(2.0f).toFloat()
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()

        val vi = view ?: if (type == typeFile) LayoutInflater.from(parent?.context).inflate(R.layout.torrent_files_item, parent, false)
        else LayoutInflater.from(parent?.context).inflate(R.layout.torrent_files_button, parent, false)
        if (files.size > 1 && position == count - 1) return vi

        val file = files[position]
        val tvFileName = vi.findViewById<TextView>(R.id.tvFileName)
        val label = labels[position]
        val ext = File(file.path).extension

        tvFileName.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
            text = label.title
            setTextColor(colorBright)
            contentDescription = file.path
        }
        vi.findViewById<TextView>(R.id.tvFileFolder).apply {
            text = label.folder
            visibility = if (label.folder.isBlank()) View.GONE else View.VISIBLE
            setTextColor(color1)
        }

        if (ext.isNotEmpty()) { // extension
            // append(" .$ext", color1)
            vi.findViewById<TextView>(R.id.tvExt)?.apply {
                visibility = View.VISIBLE
                text = ".$ext"
                val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
                shapeDrawable.fillColor = ColorStateList.valueOf(colorBright).withAlpha(40)
                shapeDrawable.setStroke(2.0f, ColorStateList.valueOf(colorBright).withAlpha(140))
                background = shapeDrawable
                setTextColor(color1)
            }
        } else {
            vi.findViewById<TextView>(R.id.tvExt)?.visibility = View.GONE
        }

        val size = Format.byteFmt(file.length)
        vi.findViewById<TextView>(R.id.tvFileSize)?.apply {
            text = size
            val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
            shapeDrawable.fillColor = ColorStateList.valueOf(colorBright).withAlpha(240)
            shapeDrawable.setStroke(2.0f, ColorStateList.valueOf(colorBright).withAlpha(255))
            background = shapeDrawable
            setTextColor(ThemeUtil.getColorFromAttr(themedContext, R.attr.colorSurface))
        }

        vi.findViewById<ImageView>(R.id.ivViewed)?.apply {
            visibility = View.GONE
            for (it in viewed) {
                if (it.file_index == file.id) {
                    visibility = View.VISIBLE
                    break
                }
            }
        }
        return vi
    }

    override fun getItem(p0: Int): Any? {
        if (p0 < 0 || p0 >= count) return null
        // play from beginning
        if (files.size > 1 && p0 == count - 1) return files[0]
        return files[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return if (files.size > 1) files.size + 1
        else files.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (files.size > 1 && position == count - 1) typeButton else typeFile
    }

    override fun getViewTypeCount(): Int {
        return 2
    }
}
