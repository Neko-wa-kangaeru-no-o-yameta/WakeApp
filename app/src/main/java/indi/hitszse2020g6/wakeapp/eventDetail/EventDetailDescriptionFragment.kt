package indi.hitszse2020g6.wakeapp.eventDetail

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
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.dummy.DummyContent

/**
 * A fragment representing a list of Items.
 */
class EventDetailDescriptionFragment() : Fragment() {

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
        val view =
            inflater.inflate(R.layout.fragment_event_detail_description_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = object: LinearLayoutManager(context) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }
                adapter = MyDescriptionRecyclerViewAdapter(EventDetailList.ITEMS).apply {
                    notifyDataSetChanged()
                }
                Log.d("Detail", "adapter attached, ${EventDetailList.ITEMS}")
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
            EventDetailDescriptionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}