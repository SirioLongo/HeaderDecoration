package com.example.headerdecoration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameList?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = SimpleItemAdapter().apply {
                submitList(ContactsStore.getContacts())
            }
            addItemDecoration(
                HeaderItemDecoration(
                    object : HeaderItemDecoration.HeaderFactory {
                        override fun getHeaderView(itemViewType: Int, inflater: LayoutInflater, parent: ViewGroup): View {
                            val v = inflater.inflate(
                                R.layout.layout_section_header,
                                parent,
                                false
                            )
                            val textView = v.findViewById<TextView>(R.id.textView)
                            textView?.text = itemViewType.toChar().toString()
                            return v ?: View(this@MainActivity)
                        }
                    }
                )
            )
        }
    }
}
