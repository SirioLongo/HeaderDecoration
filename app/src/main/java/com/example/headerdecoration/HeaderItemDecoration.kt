package com.example.headerdecoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class HeaderItemDecoration(
    val headerFactory: HeaderFactory
) : RecyclerView.ItemDecoration() {

    val HEADER_HEIGHT = 128

    val paint = Paint().apply {
        color = Color.DKGRAY
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.let { adapter ->
            val position = parent.getChildAdapterPosition(view)
            when {
                position == 0 -> outRect.top = HEADER_HEIGHT
                position > 0 -> {
                    val currentItemViewType = adapter.getItemViewType(position)
                    val previousItemViewType = adapter.getItemViewType(position - 1)
                    if (currentItemViewType != previousItemViewType) {
                        outRect.top = HEADER_HEIGHT
                    }
                }
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val childCount = parent.childCount
        val sectionList = mutableListOf<Section>()
        var currentSection: Section? = null

        // Create section list

        for (i in 0 until childCount) {
            parent.adapter?.let { adapter ->
                val child = parent.getChildAt(i)
                val positionInList = parent.getChildAdapterPosition(child)
                val itemViewType = adapter.getItemViewType(positionInList)

                when {
                    i == 0 -> {
                        // create first section and add this child as starting child
                        val section = Section(itemViewType).apply {
                            startChild = child
                        }
                        currentSection = section
                        // add section to list
                        sectionList.add(section)
                    }
                    i > 0 -> {
                        if (currentSection != null && currentSection?.itemViewType != itemViewType) {
                            // if this is the start of a new section, set the previous item as end child
                            // of the previous section and create a new one as done above
                            currentSection?.endChild = parent.getChildAt(i - 1)
                            val section = Section(itemViewType).apply {
                                startChild = child
                                endChild = child
                            }
                            currentSection = section
                            sectionList.add(section)
                        } else {
                            // this item belongs to the current section
                            // do nothing
                        }
                    }
                    else -> {

                    }
                }
            }

        }

        for (section: Section in sectionList) {
            section.headerView = headerFactory.getHeaderView(section.itemViewType)
            section.startChild?.let { startChild ->
                section.endChild?.let { endChild ->
                    val sectionStart = startChild.top - HEADER_HEIGHT
                    val sectionEnd = endChild.bottom
                    paint.color = section.sectionColor?.toArgb()?: Color.DKGRAY
                    if(sectionStart <= 0 && sectionEnd >= HEADER_HEIGHT) {
                        section.headerView?.apply{
                            layout(
                                0,
                                0,
                                parent.width,
                                HEADER_HEIGHT
                            )
                            draw(c)
                        }
                    } else if (sectionStart <= 0 && sectionEnd < HEADER_HEIGHT) {
                        section.headerView?.apply {
                            layout(
                                0,
                                0,
                                parent.width,
                                HEADER_HEIGHT
                            )
                            c.save()
                            c.translate(0f, sectionEnd.toFloat() - HEADER_HEIGHT)
                            draw(c)
                            c.restore()
                        }
                    } else if (sectionStart > 0) {
                        section.headerView?.apply {
                            layout(
                                0,
                                0,
                                parent.width,
                                HEADER_HEIGHT
                            )
                            c.save()
                            c.translate(0f, sectionStart.toFloat())
                            draw(c)
                            c.restore()
                        }
                    } else {

                    }
                }
            }
        }


    }

    private class Section(
        val itemViewType: Int
    ) {
        var sectionColor: Color? = null
        var startChild: View? = null
        var endChild: View? = null
        var headerView: View? = null
    }

    interface HeaderFactory {
        fun getHeaderView(itemViewType: Int): View
    }
}