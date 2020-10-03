package indi.hitszse2020g6.wakeapp.mainPage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.leinardi.android.speeddial.SpeedDialView
import indi.hitszse2020g6.wakeapp.AppRoomDB
import indi.hitszse2020g6.wakeapp.eventDetail.AffairDetailActivity
import indi.hitszse2020g6.wakeapp.eventDetail.ScheduleDetailActivity
import indi.hitszse2020g6.wakeapp.INTENT_AFFAIR_DETAIL
import indi.hitszse2020g6.wakeapp.INTENT_SCHEDULE_DETAIL
import indi.hitszse2020g6.wakeapp.R
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

/**
 * A fragment representing a list of Items.
 */
class MainPageFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainPageEventList.DAO = AppRoomDB.getDataBase(requireContext()).getDAO()
        MainPageEventList.getEventListFromDB()
        Log.d("MainPageFragment", "Attaching adapter")

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_page, container, false)
        // Set the adapter
        with(view.findViewById<RecyclerView>(R.id.mainPageRecyclerView)) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            GlobalScope.launch(Dispatchers.IO) {
                while(!MainPageEventList.initComplete) {
                    Thread.sleep(5)
                }
                Handler(Looper.getMainLooper()).post {
                    adapter = MyMainPageRecyclerViewAdapter(MainPageEventList.eventList)
                    ItemTouchHelper(MainPageItemTouchHelperCB(this@with.adapter as MyMainPageRecyclerViewAdapter)).attachToRecyclerView(this@with)
                    this@with.adapter!!.notifyDataSetChanged()
                    Log.d("MagePageFragment", "Attached to adapter and item touch helper")
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<SpeedDialView>(R.id.mainPage_speedDial).apply {
            inflate(R.menu.main_page_speed_dial_menu)
            setOnActionSelectedListener { actionItems ->
                when(actionItems.id) {
                    R.id.mainPage_speedDialNewAffair -> {
                        startActivityForResult(Intent(activity, AffairDetailActivity::class.java), INTENT_AFFAIR_DETAIL)
                        close()
                        true
                    }
                    R.id.mainPage_speedDialNewSchedule -> {
                        startActivityForResult(Intent(activity, ScheduleDetailActivity::class.java), INTENT_SCHEDULE_DETAIL)
                        close()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        view?.findViewById<RecyclerView>(R.id.mainPageRecyclerView)?.adapter?.notifyDataSetChanged()
        super.onResume()
    }

    override fun onPause() {
        MainPageEventList.applyModifyToDatabase()
        Log.d("MainPageFragment", "OnPause called, saving result")
        super.onPause()
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            MainPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}