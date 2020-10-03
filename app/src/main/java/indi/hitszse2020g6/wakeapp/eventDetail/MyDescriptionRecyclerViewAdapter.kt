package indi.hitszse2020g6.wakeapp.eventDetail

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import indi.hitszse2020g6.wakeapp.Detail
import indi.hitszse2020g6.wakeapp.R

import indi.hitszse2020g6.wakeapp.dummy.DummyContent.DummyItem

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
        holder.titleView.text = item.title
        holder.contentView.text = item.content

        holder.titleView.addTextChangedListener {
            item.title = holder.titleView.text.toString()
        }

        holder.contentView.addTextChangedListener {
            item.content = holder.contentView.text.toString()
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.eventDetail_detailListItem_title)
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