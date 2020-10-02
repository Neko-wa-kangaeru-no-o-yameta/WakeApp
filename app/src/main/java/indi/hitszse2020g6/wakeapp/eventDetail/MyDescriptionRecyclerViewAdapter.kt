package indi.hitszse2020g6.wakeapp.eventDetail

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import indi.hitszse2020g6.wakeapp.Detail
import indi.hitszse2020g6.wakeapp.R

import indi.hitszse2020g6.wakeapp.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyDescriptionRecyclerViewAdapter(
    private val values: List<Detail>
) : RecyclerView.Adapter<MyDescriptionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_event_detail_description, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.title
        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.eventDetail_detailListItem_title)
        val contentView: TextView = view.findViewById(R.id.eventDetail_detailListItem_content)
        val delBtnView: ImageButton = view.findViewById(R.id.eventDetail_detailListItem_delButton)

        init {
            delBtnView.setOnClickListener {
                EventDetailList.ITEMS.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}