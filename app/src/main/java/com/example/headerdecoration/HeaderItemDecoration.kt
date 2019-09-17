package com.example.headerdecoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

/**
 * Adds a header on top of each section.
 * Sections are detected by itemViewTypes retrieved from the adapter
 * Any cluster of visible contiguous items having the same viewType is considered a section
 */
class HeaderItemDecoration(
    val headerFactory: HeaderFactory
) : RecyclerView.ItemDecoration() {

    // Fixed height for all headers
    // passed to the header view
    // ToDo: wrap content height support
    val HEADER_HEIGHT = 128

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
        val childCount = parent.childCount // Number of visible children
        // ToDo: avoid repopulating this intire list on every draw call and switch to add/remove population
        val sectionList = mutableListOf<Section>() // List of objects representing visible sections
        // Current section being considered during the loop
        var currentSection: Section? = null

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

        // The section list has been populated
        // for each one, draw a header
        for (section: Section in sectionList) {
            section.headerView = headerFactory.getHeaderView(section.itemViewType)
            section.startChild?.let { startChild ->
                section.endChild?.let { endChild ->
                    // If both ends of a section exist, compute the top and bottom of its header and draw it
                    // ToDo: Move layout() calls outside of every single draw call to lighten the computing load
                    val sectionStart = startChild.top - HEADER_HEIGHT
                    val sectionEnd = endChild.bottom
                    if(sectionStart <= 0 && sectionEnd >= HEADER_HEIGHT) {
                        // The section is scrolled beyond the top of the recycler view,
                        // The end of the section is beyond the height of the section,
                        // The section is anchored to the top of the recycler view
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
                            // The section is scrolled beyond the top of the recycler view almost entirely,
                            // there is not enough room for the header to be entirely seen
                            // The end of the section is closer to the top than the header's height
                            // The header is only partially visible and anchored to the end of the section
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
                            // This section is not the first one in the screen,
                            // The header is anchored to the top of the section
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