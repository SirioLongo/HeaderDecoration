package com.example.headerdecoration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
                SectionDecoration(
                    getDrawable(R.drawable.section_background),
                    'G'.toInt(),
                    16,
                    16,
                    8,
                    8
                )
//                HeaderItemDecoration(
//                    object: HeaderItemDecoration.HeaderFactory {
//                        override fun getHeaderView(itemViewType: Int): View {
//                            val v = layoutInflater.inflate(R.layout.layout_section_header, null) as? TextView
//                            v?.text = itemViewType.toChar().toString()
//                            return v?: View(this@MainActivity)
//                        }
//
//                    }
//                )
            )
            addItemDecoration(
                SectionDecoration(
                    getDrawable(R.drawable.section_background),
                    'A'.toInt(),
                    16,
                    16,
                    8,
                    8
                )
            )
        }
    }
}
