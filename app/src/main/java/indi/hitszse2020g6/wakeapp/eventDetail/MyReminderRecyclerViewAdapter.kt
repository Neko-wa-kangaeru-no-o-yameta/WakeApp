package indi.hitszse2020g6.wakeapp.eventDetail

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.Reminder

import indi.hitszse2020g6.wakeapp.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyReminderRecyclerViewAdapter(
    private val values: List<Reminder>,
    private val context: FragmentActivity?,
) : RecyclerView.Adapter<MyReminderRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_event_detail_reminder, parent, false)
        return ViewHolder(view)
    }

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
//        holder.idView.text = item.description
//        holder.contentView.text = item.time.toString()
        holder.cardView.findViewById<TextView>(R.id.eventDetail_reminderListItem_content).apply{
            setOnClickListener {
                Toast.makeText(holder.cardView.context, "TODO for position $position", Toast.LENGTH_SHORT).show()
                // TODO: Number Picker Dialog
            }
            val day = item.time / (24*60*60)
            val hour = item.time / (60*60) - day * 24
            val minute = item.time / 60 - (day*24*60 + hour*+60)
            text = resources.getText(R.string.eventDetail_reminderListItem_timeContent).toString().format(day, hour, minute)
        }

        holder.cardView.findViewById<ImageButton>(R.id.eventDetail_reminderListItem_ringBtn).apply {
            toggleImageDrawable(this, item.ring, R.drawable.ring_on_24, R.drawable.ring_off_24)
            setOnClickListener{
                item.ring = !item.ring
                toggleImageDrawable(this, item.ring, R.drawable.ring_on_24, R.drawable.ring_off_24)
            }
        }

        holder.cardView.findViewById<ImageButton>(R.id.eventDetail_reminderListItem_vibrateBtn).apply {
            toggleImageDrawable(this, item.vibration, R.drawable.vibration_on_24, R.drawable.vibration_off_24)
            setOnClickListener{
                item.vibration = !item.vibration
                toggleImageDrawable(this, item.vibration, R.drawable.vibration_on_24, R.drawable.vibration_off_24)
            }
        }

        holder.cardView.findViewById<ImageButton>(R.id.eventDetail_reminderListItem_notificationBtn).apply {
            toggleImageDrawable(this, item.notification, R.drawable.notification_on_24, R.drawable.notification_off_24)
            setOnClickListener{
                item.notification = !item.notification
                toggleImageDrawable(this, item.notification, R.drawable.notification_on_24, R.drawable.notification_off_24)
            }
        }

        holder.cardView.findViewById<TextView>(R.id.eventDetail_reminderListItem_content).setOnClickListener {
            Toast.makeText(context, "还没想好怎么表示", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = values.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.eventDetail_reminderListItem)

        override fun toString(): String {
            return super.toString()
        }
    }
}