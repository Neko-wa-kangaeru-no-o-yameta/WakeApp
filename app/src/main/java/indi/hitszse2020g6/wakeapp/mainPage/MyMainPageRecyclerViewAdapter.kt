package indi.hitszse2020g6.wakeapp.mainPage

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import indi.hitszse2020g6.wakeapp.EventTableEntry
import indi.hitszse2020g6.wakeapp.R

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyMainPageRecyclerViewAdapter(
    private val values: LiveData<List<EventTableEntry>>
) : RecyclerView.Adapter<MyMainPageRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_main_page_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values.value!![position]
        holder.idView.text = item.title
        holder.contentView.text = item.uid.toString()
    }

    override fun getItemCount(): Int {
        return if(values.value == null) 0
        else values.value!!.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.item_number)
        val contentView: TextView = view.findViewById(R.id.content)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}