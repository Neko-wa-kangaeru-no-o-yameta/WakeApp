package indi.hitszse2020g6.wakeapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import indi.hitszse2020g6.wakeapp.dummy.CourseWeek
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.dummy.DummyContent
import indi.hitszse2020g6.wakeapp.eventDetail.MyReminderRecyclerViewAdapter

/**
 * A fragment representing a list of Items.
 */
class ItemFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.course_time_add_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = object :LinearLayoutManager(context){
                    override fun canScrollHorizontally(): Boolean {
                        return false
                    }
                }
                adapter = MyCourseTimeAddAdapter(CourseWeek.ITEMS,activity).apply {
                    notifyDataSetChanged()
                }
                ItemTouchHelper(CourseWeekItemTouchHelperCB(adapter as MyCourseTimeAddAdapter)).attachToRecyclerView(this)
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}