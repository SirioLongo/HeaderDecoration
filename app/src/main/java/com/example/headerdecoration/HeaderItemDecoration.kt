package com.example.headerdecoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
/**
 * Adds a header on top of each section.
 * Sections are detected by itemViewTypes retrieved from the adapter
 * Any cluster of visible contiguous items having the same viewType is considered a section
 */
class HeaderItemDecoration(
    private val headerFactory: HeaderFactory
) : RecyclerView.ItemDecoration() {

    // Fixed height for all headers
    // passed to the header view
    // ToDo: wrap content height support
    val HEADER_HEIGHT = 100

    /**
     * Here we find out which of the visible children are first items in their own sections and add
     * spacing at the top equal to the height of a header (so that the header can sit here before
     * sliding)
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.let { adapter ->
            val position = parent.getChildAdapterPosition(view) // The absolute position of the item in the adapter
            when {
                position == 0 -> {
                    // The first visible item can be considered the first in its own section
                    // Set the spacing at the top of the item
                    outRect.top = HEADER_HEIGHT
                }
                position > 0 -> {
                    // Any other item is considered the first in its own section if the previous has
                    // a different viewType
                    val currentItemViewType = adapter.getItemViewType(position)
                    val previousItemViewType = adapter.getItemViewType(position - 1)
                    if (currentItemViewType != previousItemViewType) {
                        // Set the spacing at the top of the item
                        outRect.top = HEADER_HEIGHT
                    }
                }
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val sectionList = createSectionList(parent)
        drawHeaders(sectionList, parent, c)
    }

    private fun createSectionList(parent: RecyclerView): MutableList<Section> {
        val sectionList = mutableListOf<Section>() // List of objects representing visible sections
        // Current section being considered during the loop
        var currentSection: Section? = null
        val childCount = parent.childCount // Number of visible children

        // Loop to create the section list
        for (i in 0 until childCount) {
            // i represents the item's position relative to the recycler view and not the adapter
            parent.adapter?.let { adapter ->
                // Child view at the desired relative position
                val child = parent.getChildAt(i)
                // The child's absolute position within the adapter
                val positionInList = parent.getChildAdapterPosition(child)
                // The child's viewType
                val itemViewType = adapter.getItemViewType(positionInList)

                when {
                    i == 0 -> {
                        // create first section and add this child as starting child
                        val section = Section(itemViewType).apply {
                            startChild = child
                        }
                        // Set this as the current section
                        currentSection = section
                        // add section to list
                        sectionList.add(section)
                    }
                    i > 0 -> {
                        if (currentSection != null && currentSection?.itemViewType != itemViewType) {
                            // if this is the start of a new section, set the previous item as end child
                            // of the previous section and create a new one as done above
                            // ToDo: consider moving this assignment to next branch of when() statement
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
        return sectionList
    }

    private fun drawHeaders(
        sectionList: MutableList<Section>,
        parent: RecyclerView,
        c: Canvas
    ) {
        for (section: Section in sectionList) {
            section.headerView = headerFactory.getHeaderView(section.itemViewType)
            section.startChild?.let { startChild ->
                section.endChild?.let { endChild ->
                    // If both ends of a section exist, compute the top and bottom of its header and draw it
                    // ToDo: Move layout() calls outside of every single draw call to lighten the computing load
                    val sectionStart = startChild.top - HEADER_HEIGHT
                    val sectionEnd = endChild.bottom
                    when {
                        sectionStartsBeyondTheTopWithRoomForHeader(sectionStart, sectionEnd) -> {
                            // The section header is anchored to the top of the recycler view
                            section.headerView?.apply {
                                drawHeaderAtHeight(
                                    section = section,
                                    parent = parent,
                                    canvas = c,
                                    height = 0f
                                )
                            }
                        }
                        sectionStartsBeyondTheTopWithNoRoomForHeader(sectionStart, sectionEnd) -> {
                            section.headerView?.apply {
                                // The header is only partially visible and anchored to the end of the section
                                drawHeaderAtHeight(
                                    section = section,
                                    parent = parent,
                                    canvas = c,
                                    height = sectionEnd.toFloat() - HEADER_HEIGHT
                                )
                            }
                        }
                        sectionIsNotTheFirstVisible(sectionStart) -> {
                            // The header is anchored to the top of the section
                            drawHeaderAtHeight(
                                section = section,
                                parent = parent,
                                canvas = c,
                                height = sectionStart.toFloat()
                            )
                        }
                        else -> {

                        }
                    }
                }
            }
        }
    }

    private fun sectionStartsBeyondTheTopWithNoRoomForHeader(
        sectionStart: Int,
        sectionEnd: Int
    ): Boolean {
        return sectionStart <= 0 && sectionEnd < HEADER_HEIGHT
    }

    private fun sectionIsNotTheFirstVisible(sectionStart: Int): Boolean {
        return sectionStart > 0
    }

    private fun sectionStartsBeyondTheTopWithRoomForHeader(
        sectionStart: Int,
        sectionEnd: Int
    ): Boolean {
        return sectionStart <= 0 && sectionEnd >= HEADER_HEIGHT
    }

    private fun drawHeaderAtHeight(
        section: Section,
        parent: RecyclerView,
        canvas: Canvas,
        height: Float
    ) {
        section.headerView?.apply {
            layout(
                0,
                0,
                parent.width,
                HEADER_HEIGHT
            )
            canvas.save()
            canvas.translate(0f, height)
            draw(canvas)
            canvas.restore()
        }
    }

    /**
     * This class represents a section, it has its viewType, the start and end child and a reference to
     * the header view
     */
    private class Section(
        val itemViewType: Int
    ) {
        var startChild: View? = null
        var endChild: View? = null
        var headerView: View? = null
    }

    // This interface is used to provide the view to be drawn as a header for heach item viewType
    interface HeaderFactory {
        fun getHeaderView(itemViewType: Int): View
    }
}