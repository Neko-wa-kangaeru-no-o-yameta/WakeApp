package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import indi.hitszse2020g6.wakeapp.AppRoomDB
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlinx.android.synthetic.main.activity_choose_schedule_time.*
import kotlinx.android.synthetic.main.fragment_course.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [CourseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CourseFragment : Fragment() {
    private var param1: Int? = null
    var weekForVp :Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout) ?: return
        val viewPager2 = view.findViewById<ViewPager2>(R.id.viewPager2) ?: return
        var week:Int = 0


        //为ViewPager2设置适配器
        Log.d("CourseFragment", "got view")

        var mySharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(
            "schedule_time",
            Context.MODE_PRIVATE
        )


        if(mySharedPreferences.getLong("startTime", -1).toInt() !=-1){
            val startTime = mySharedPreferences.getLong("startTime", -1)
            Log.d("startTime1", startTime.toString())
            val calendar = Calendar.getInstance().apply { timeInMillis =  startTime}
            val stopTime = System.currentTimeMillis()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)
            Log.d("timeIntMills1",calendar.timeInMillis.toString())
//            calendar.firstDayOfWeek = Calendar.MONDAY
//
//            val startWeekMonday = calendar.timeInMillis
            week = ((stopTime - calendar.timeInMillis)/(7*24*60*60*1000)).toInt()
            Log.d("WEEK1",week.toString())


        }
        val maxWeek = 18
        val myAdapter =  MyViewPageAdapter(this@CourseFragment)
        if (maxWeek != null) {
            myAdapter.maxWeek = maxWeek
        }
        viewPager2.adapter = myAdapter
        viewPager2.currentItem = week

            //TODO 这个适配器好像有点毛病那个，设置了currentItem之后有点问题

            TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                tab.text = "第${position + 1}周"

            }.attach()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment showFragment.
         */
        @JvmStatic
        fun newInstance() =
            CourseFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

class MyViewPageAdapter(val fragment: CourseFragment):FragmentStateAdapter(fragment){
    var maxWeek:Int = 0
    override fun getItemCount(): Int {
        return maxWeek
    }
    override fun createFragment(position: Int): Fragment {
        return WeekCourseFragment.newInstance(position + 1)
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val tag = "f" + holder.itemId
        Log.d("tag",tag)
        val fragment:WeekCourseFragment? = fragment.childFragmentManager?.findFragmentByTag(tag) as WeekCourseFragment?
        if (fragment != null) {
            Log.d("tag-onBindViewHolder",tag)
            fragment.updateCourseCardView()
        }else{
            Log.d("tag-onBindViewHolder","null")
            super.onBindViewHolder(holder, position, payloads)
        }
    }

}