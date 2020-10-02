package indi.hitszse2020g6.wakeapp.mainPage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import indi.hitszse2020g6.wakeapp.AppRoomDB
import indi.hitszse2020g6.wakeapp.EventDetailActivity
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.INTENT_EVENT_DETAIL

/**
 * A fragment representing a list of Items.
 */
class MainPageFragment : Fragment() {

    private var columnCount = 1

    private val viewModel: MainPageListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.init(AppRoomDB.getDataBase(requireContext()).getDAO())

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
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyMainPageRecyclerViewAdapter(viewModel.eventList)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<FloatingActionButton>(R.id.mainPageAddEvent).setOnClickListener {
            startActivity(Intent(this.activity, EventDetailActivity::class.java))
        }
        super.onViewCreated(view, savedInstanceState)
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