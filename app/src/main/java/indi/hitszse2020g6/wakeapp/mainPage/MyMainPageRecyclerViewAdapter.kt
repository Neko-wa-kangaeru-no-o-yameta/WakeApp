package indi.hitszse2020g6.wakeapp.mainPage

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import indi.hitszse2020g6.wakeapp.EventTableEntry
import indi.hitszse2020g6.wakeapp.R

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyMainPageRecyclerViewAdapter(
    private val values: List<EventTableEntry>
) : RecyclerView.Adapter<MyMainPageRecyclerViewAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_main_page_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.cardView.findViewById<TextView>(R.id.eventList_eventTitle).text = item.title
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.mainPageList_card)

        override fun toString(): String {
            return super.toString() + " '" + cardView.findViewById<TextView>(R.id.eventDetail_detailListItem_content).text
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        MainPageEventList.changePriority(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int): Boolean {
        MainPageEventList.removeEvent(position)
        notifyItemRemoved(position)
        return true
    }
}