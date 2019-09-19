package com.example.headerdecoration

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class SectionDecoration(
    private val drawable: Drawable,
    private val viewTypeOfGroupItem: Int,
    private val marginLeft: Int,
    val marginRight: Int,
    val marginTop: Int,
    val marginBottom: Int
) : RecyclerView.ItemDecoration() {

    private var marginRightPx = (marginRight * Resources.getSystem().displayMetrics.density).toInt()
    private var marginLeftPx = (marginLeft * Resources.getSystem().displayMetrics.density).toInt()
    private var marginTopPx = (marginTop * Resources.getSystem().displayMetrics.density).toInt()
    private var marginBottomPx = (marginBottom * Resources.getSystem().displayMetrics.density).toInt()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        var sectionFirstVisiblePosition: Int? = null
        var sectionLastVisiblePosition: Int? = null

        // top will be the top margin above the first element of type X
        // the bottom margin is under the last element of type X
        // for loop per individuare quali sono le view correntemente visibili di tipo X
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val positionInAdapter = parent.getChildAdapterPosition(child)
            parent.adapter?.let { adapter ->
                val itemViewType = adapter.getItemViewType(positionInAdapter)
                if (itemViewType == viewTypeOfGroupItem) {
                    //devo disegnare
                    if (i == 0) {
                        sectionFirstVisiblePosition = i
                        if (i != parent.childCount - 1 && adapter.getItemViewType(positionInAdapter + 1) != viewTypeOfGroupItem) {
                            sectionLastVisiblePosition = i
                        }
                        if (i == parent.childCount - 1) {
                            sectionLastVisiblePosition = i
                        }
                    } else if (i > 0) {
                        if (adapter.getItemViewType(positionInAdapter - 1) != viewTypeOfGroupItem) {
                            sectionFirstVisiblePosition = i
                        }
                        if (i != parent.childCount - 1 && adapter.getItemViewType(positionInAdapter + 1) != viewTypeOfGroupItem) {
                            sectionLastVisiblePosition = i
                        }
                        if (i == parent.childCount - 1) {
                            sectionLastVisiblePosition = i
                        }
                    }
                }
            }
        }

        // Decide wether the top and bottom of the section are visible
        parent.adapter?.let { adapter ->
            sectionFirstVisiblePosition?.let { first ->
                sectionLastVisiblePosition?.let { last ->
                    val firstAdapterPosition =
                        parent.getChildAdapterPosition(parent.getChildAt(first))
                    val lastAdapterPosition =
                        parent.getChildAdapterPosition(parent.getChildAt(last))

                    val top =
                        if (firstAdapterPosition == 0 || (firstAdapterPosition > 0 && adapter.getItemViewType(
                                firstAdapterPosition - 1
                            ) != viewTypeOfGroupItem)
                        ) {
                            // The top of the section isn't visible
                            parent.getChildAt(sectionFirstVisiblePosition?:0).top
                        } else {
                            // An arbitraty starting point beyond the top of the recyclerview
                            -parent.width
                        }
                    val bottom =
                        if (lastAdapterPosition == adapter.itemCount - 1 || (lastAdapterPosition < adapter.itemCount - 1 && adapter.getItemViewType(
                                lastAdapterPosition + 1
                            ) != viewTypeOfGroupItem)
                        ) {
                            // The bottom of the section isn't visible
                            parent.getChildAt(sectionLastVisiblePosition?:0).bottom
                        } else {
                            // An arbitraty starting point beyond the bottom of the recyclerview
                            parent.height + parent.width
                        }

                    // Disegno
                    drawable.setBounds(
                        marginLeftPx,
                        top + marginTopPx,
                        parent.width - marginRightPx,
                        bottom - marginBottomPx
                    )
                    drawable.draw(c)
                }
            }
        }
    }
}
