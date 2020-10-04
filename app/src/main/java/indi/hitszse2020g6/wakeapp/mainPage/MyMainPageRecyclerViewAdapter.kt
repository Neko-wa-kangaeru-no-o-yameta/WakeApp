package indi.hitszse2020g6.wakeapp.mainPage

import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import indi.hitszse2020g6.wakeapp.EventTableEntry
import indi.hitszse2020g6.wakeapp.INTENT_AFFAIR_DETAIL
import indi.hitszse2020g6.wakeapp.INTENT_SCHEDULE_DETAIL
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.eventDetail.AffairDetailActivity
import indi.hitszse2020g6.wakeapp.eventDetail.ScheduleDetailActivity
import indi.hitszse2020g6.wakeapp.eventDetail.UNIQUE_ID_TO_AFFAIR_DETAIL
import indi.hitszse2020g6.wakeapp.eventDetail.UNIQUE_ID_TO_SCHEDULE_DETAIL
import java.util.*

class MyMainPageRecyclerViewAdapter(
    private val values: List<EventTableEntry>
) : RecyclerView.Adapter<MyMainPageRecyclerViewAdapter.ViewHolder>(), MainPageItemTouchHelperAdapter {

    private fun toggleImageDrawable(btn: ImageButton, on: Boolean, onID: Int, offID: Int) {
        with(btn) {
            setImageDrawable(
                if(on) {
                    ContextCompat.getDrawable(context, onID)
                } else {
                    ContextCompat.getDrawable(context, offID)
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_main_page_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cardView.setOnClickListener {
            if(position < values.size && values[position].isAffair) {
                val intent = Intent(it.context, AffairDetailActivity::class.java)
                intent.putExtra(UNIQUE_ID_TO_AFFAIR_DETAIL, values[position].uid)
                (it.context as AppCompatActivity).startActivityForResult(intent, INTENT_AFFAIR_DETAIL)
            } else {
                val intent = Intent(it.context, ScheduleDetailActivity::class.java)
                intent.putExtra(UNIQUE_ID_TO_SCHEDULE_DETAIL, values[position].uid)
                (it.context as AppCompatActivity).startActivityForResult(intent, INTENT_SCHEDULE_DETAIL)
            }
        }

        holder.cardView.findViewById<TextView>(R.id.eventList_eventTitle).text = values[position].title

        holder.cardView.findViewById<TextView>(R.id.eventDetail_startTimeTV).apply {
            if(values[position].isAffair) {
                visibility = GONE
            } else {
                val c = Calendar.getInstance().apply { timeInMillis = values[position].startTime*1000 }
                text = context.getString(R.string.eventList_startTimeTVContent).format(c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
            }
        }

        val c = Calendar.getInstance().apply { timeInMillis = values[position].stopTime*1000 }
        holder.cardView.findViewById<TextView>(R.id.eventDetail_stopTimeTV).apply {
            text = context.getString(
                R.string.eventList_stopTimeTVContent
            ).format(
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE)
            )
        }

        holder.cardView.findViewById<ImageButton>(R.id.eventList_noticeBtn).apply {
            toggleImageDrawable(this, values[position].notice, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            setOnClickListener {
                values[position].notice = !values[position].notice
                Log.d("APAdapter", "reverting notice state:${values[position].notice}")
                toggleImageDrawable(it as ImageButton, values[position].notice, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            }
        }

        with(holder.cardView.findViewById<ImageButton>(R.id.eventList_focusBtn)) {
            toggleImageDrawable(this, values[position].focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            if(values[position].isAffair) {
                this.visibility = INVISIBLE
                this.isEnabled = false
            } else {
                this.visibility = VISIBLE
                this.isEnabled = true
                setOnClickListener {
                    values[position].focus = !values[position].focus
                    toggleImageDrawable(this, values[position].focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
                }
            }
        }

        with(holder.cardView.findViewById<ImageButton>(R.id.eventList_muteBtn)) {
            toggleImageDrawable(this, values[position].mute, R.drawable.mute_on_24, R.drawable.mute_off_24)
            if(values[position].isAffair) {
                this.visibility = INVISIBLE
                this.isEnabled = false
            } else {
                this.visibility = VISIBLE
                this.isEnabled = true
                setOnClickListener {
                    values[position].mute = !values[position].mute
                    toggleImageDrawable(this, values[position].mute, R.drawable.mute_on_24, R.drawable.mute_off_24)
                }
            }
        }

        val descHolder = holder.cardView.findViewById<LinearLayout>(R.id.mainPageList_cardDetailContainer)
        descHolder.removeAllViews()
        for(detail in values[position].detail) {
            Log.d("Populating Detail","title: ${detail.title}, content: ${detail.content}")
            val reminderItem = LayoutInflater.from(descHolder.context).inflate(R.layout.main_page_list_item_desc_item, descHolder, false)
            reminderItem.findViewById<TextView>(R.id.eventListItem_detailTitle).text = detail.title
            reminderItem.findViewById<TextView>(R.id.eventListItem_detailContent).text = detail.content
            descHolder.addView(reminderItem)
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        MainPageEventList.changePriority(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        notifyItemRangeChanged(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int): Boolean {
        MainPageEventList.removeEvent(position)
        notifyItemRemoved(position)
        return true
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.mainPageList_card)

        override fun toString(): String {
            return super.toString() + " '" + cardView.findViewById<TextView>(R.id.eventDetail_detailListItem_content).text
        }
    }
}