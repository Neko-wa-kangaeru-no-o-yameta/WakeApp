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
    private val chineseWeek = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course_time_add, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.weekView.apply {
            text = if((CourseWeek.ITEMS[position].weekBegin != 0) &&(CourseWeek.ITEMS[position].weekEnd != 0)){
                context.getString(R.string.courseDetail_timeContentWeek).format(
                    CourseWeek.ITEMS[position].weekBegin,
                    CourseWeek.ITEMS[position].weekEnd)
            } else{
                context.getString(R.string.courseDetail_timeAddWeekHint)
            }
            setOnClickListener {
                this@MyCourseTimeAddAdapter.context?.supportFragmentManager?.let { it1 ->
                    WeekPickerFragment(position).show(
                        it1,"WeekPickerFragment")
                }
            }
        }

        holder.timeView.apply {
            text = if((CourseWeek.ITEMS[position].dayOfWeek != 0)&&(CourseWeek.ITEMS[position].time != 0)){
                context.getString(R.string.courseDetail_timeContent).format(
                    chineseWeek[CourseWeek.ITEMS[position].dayOfWeek - 1],
                    CourseWeek.ITEMS[position].time
                )
            } else{
                context.getString(R.string.courseDetail_timeAddHint)
            }
            setOnClickListener {
                this@MyCourseTimeAddAdapter.context?.supportFragmentManager?.let { it1 ->
                    TimePickFragment(position).show(
                        it1,"WeekPickerFragment")
                }
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