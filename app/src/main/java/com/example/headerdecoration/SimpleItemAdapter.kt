package com.example.headerdecoration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SimpleItemAdapter: ListAdapter<SimpleItem, SimpleViewHolder>(SimpleDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        return SimpleViewHolder(inflater.inflate(R.layout.item_simple, parent, false))
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.name.toCharArray()[0].toInt()
    }
}

class SimpleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val nameTextView = itemView.findViewById<TextView?>(R.id.nameTv)

    fun bind(data: SimpleItem) {
        nameTextView?.text = data.name
    }
}

class SimpleDiffCallback: DiffUtil.ItemCallback<SimpleItem>() {
    override fun areItemsTheSame(oldItem: SimpleItem, newItem: SimpleItem): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SimpleItem, newItem: SimpleItem): Boolean = oldItem.name == newItem.name

}