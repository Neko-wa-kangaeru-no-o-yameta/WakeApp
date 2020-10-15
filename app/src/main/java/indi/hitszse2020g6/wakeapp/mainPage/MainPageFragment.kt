package indi.hitszse2020g6.wakeapp.mainPage

import android.content.Context
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
import android.widget.LinearLayout
import androidx.recyclerview.widget.ItemTouchHelper
import com.binioter.guideview.Component
import com.binioter.guideview.GuideBuilder
import com.leinardi.android.speeddial.SpeedDialView
import indi.hitszse2020g6.wakeapp.*
import indi.hitszse2020g6.wakeapp.eventDetail.AffairDetailActivity
import indi.hitszse2020g6.wakeapp.eventDetail.ScheduleDetailActivity
import kotlinx.android.synthetic.main.fragment_focus_timer.*
import kotlinx.android.synthetic.main.fragment_main_page.*
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
        var mySharedPreferences =
            requireContext().getSharedPreferences("new_user", Context.MODE_PRIVATE)
        if (mySharedPreferences.getBoolean("isNewMainPageFragment", true)) {
            mainPage_speedDial.post { showGuideView() }
            var editor = mySharedPreferences.edit()
            editor.putBoolean("isNewMainPageFragment", false)
            editor.apply()
        }
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

    private fun showGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(mainPage_speedDial).setAlpha(150).setHighTargetPadding(-20).setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener{
            override fun onDismiss() {}
            override fun onShown() {}
        })
        builder.addComponent(AddThingsComponent())
        val guide = builder.createGuide()
        guide.show((activity as MainActivity))
    }

    class AddThingsComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            var ll:LinearLayout = inflater?.inflate(R.layout.layer_addthingsbtn,null) as LinearLayout
            return ll
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_TOP
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 0
        }

        override fun getYOffset(): Int {
            return -10
        }
    }
}