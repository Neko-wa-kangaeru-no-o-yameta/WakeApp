package indi.hitszse2020g6.wakeapp

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import indi.hitszse2020g6.wakeapp.R
import indi.hitszse2020g6.wakeapp.getPerWeekCourse
import kotlinx.android.synthetic.main.fragment_week_course.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [WeekCourseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeekCourseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Int? = null

    @RequiresApi(Build.VERSION_CODES.M)
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
        activity?.title = param1.toString()
        return inflater.inflate(R.layout.fragment_week_course, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<GridLayout>(R.id.GridLayout).apply {
            GlobalScope.launch(Dispatchers.IO){
                val weekCourse = activity?.getPerWeekCourse(param1!!)!!
                Handler(Looper.getMainLooper()).post {
                    for (ele in weekCourse) {
                        val couseName = ele.courseName
                        val courseTime = ele.time
                        val couseDayOfWeek = ele.dayOfWeek
                        val coursAddress = ele.address
                        val cardTag = "${couseDayOfWeek.toString()}${courseTime.toString()}"
                        Log.d("cardTag",cardTag)
                        var cardView = view.findViewWithTag<CardView>(cardTag)
                        if(cardView  == null){
                            Log.d("cardView","is empty")
                        }
                        //TO DO 字体的调整问题
                        with(cardView){
                            setCardBackgroundColor(resources.getColor(R.color.colorPrimary,context.theme))
                            val textView = TextView(context)
                            with(textView){
                                text = couseName
                                setSingleLine(true)
                                setEllipsize(TextUtils.TruncateAt.valueOf("END"))
                                setEms(1)
                                setTextColor(resources.getColor(R.color.design_default_color_on_primary,context.theme))
                            }
                            addView(textView)
                            val textView2 = TextView(context)
                            with(textView2){
                                text = coursAddress
                                Log.d("printAddress",text.toString())
                                setSingleLine(true)
                                setEllipsize(TextUtils.TruncateAt.valueOf("END"))
                                setEms(1)
                                setTextColor(resources.getColor(R.color.design_default_color_on_primary,context.theme))
                            }
                            addView(textView2)
                        }
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment WeekCourseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            WeekCourseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}