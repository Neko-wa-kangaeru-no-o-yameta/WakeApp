package indi.hitszse2020g6.wakeapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FocusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FocusFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_focus, container, false)
        view.setOnTouchListener { v, event ->
            v.performClick()
            detector.onTouchEvent(event)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        detector = GestureDetectorCompat(this.context, GestureListener(view))
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FocusFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FocusFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private inner class GestureListener(val v: View): GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.d("Gesture Listener", "onFling, speed = ${velocityY.toDouble()}")
            val current = Navigation.findNavController(v).currentDestination!!.id
            return when {
                (velocityY.toDouble() > 200 && current != R.id.focusTimerFragment)-> {
                    Log.d("Gesture Listener", "Going to Statistic")
                    Navigation.findNavController(v).navigate(R.id.action_focusStatisticFragment_to_focusTimerFragment)
                    true
                }
                (velocityY.toDouble() < -200  && current != R.id.focusStatisticFragment) -> {
                    Log.d("Gesture Listener", "Going to Timer")
                    Navigation.findNavController(v).navigate(R.id.action_focusTimerFragment_to_focusStatisticFragment)
                    true
                }
                else -> {
                    true
                }
            }
        }
    }
}