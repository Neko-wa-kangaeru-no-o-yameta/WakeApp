package indi.hitszse2020g6.wakeapp

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [CourseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CourseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Int? = null

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

        // todo 读入课程数据库Room


        //为ViewPager2设置适配器
        Log.d("CourseFragment", "got view")

        GlobalScope.launch (Dispatchers.IO){
            var maxWeek = context?.let { AppRoomDB.getDataBase(it).getDAO().getMaxWeek() }
            Handler(Looper.getMainLooper()).post {
                viewPager2.adapter = object : FragmentStateAdapter(this@CourseFragment) {
                    override fun getItemCount(): Int = if(maxWeek == null) 0 else maxWeek
                    override fun createFragment(position: Int): Fragment {
                        return WeekCourseFragment.newInstance(position + 1)
                    }
                }
                // todo 根据具体课程表初始化周次tab
                TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                    tab.text = "第${position + 1}周"
                }.attach()
            }
        }
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CourseFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}