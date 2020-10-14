package indi.hitszse2020g6.wakeapp

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import indi.hitszse2020g6.wakeapp.dummy.CourseWeek

import indi.hitszse2020g6.wakeapp.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyCourseTimeAddAdapter(
    private val values: List<CourseDate>,
    private val context: FragmentActivity?
) : RecyclerView.Adapter<MyCourseTimeAddAdapter.ViewHolder>(),
    CourseWeekItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_time_add, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.weekView.setOnClickListener {
            if (context != null) {
                WeekPickerFragment(position).show(context.supportFragmentManager,"WeekPickerFragment")
            }
        }
        holder.timeView.setOnClickListener {
            if (context != null) {
                TimePickFragment(position).show(context.supportFragmentManager,"WeekPickerFragment")
            }
        }
    }

    override fun getItemCount(): Int = values.size

    override fun onItemDismiss(position: Int): Boolean {
        CourseWeek.ITEMS.removeAt(position)
        notifyItemRemoved(position)
        return true
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weekView : TextView = view.findViewById(R.id.course_time_add_discription_week)
        val timeView : TextView = view.findViewById(R.id.courseDetail_time_add_discription_time)
//        override fun toString(): String {
//            return super.toString() + " '" + contentView.text + "'"
//        }
    }

}