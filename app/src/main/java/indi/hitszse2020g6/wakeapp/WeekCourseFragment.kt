package indi.hitszse2020g6.wakeapp

import android.content.Intent
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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
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
        updateCourseCardView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("get in","get in")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INTENT_ADD_COURSE){
            //直接更新
            Log.d("get in2","get in")
            updateCourseCardView()
        }
    }

    private fun WeekCourseFragment.updateCourseCardView() {
        view?.findViewById<GridLayout>(R.id.GridLayout).apply {
            GlobalScope.launch(Dispatchers.IO) {
                val weekCourse = activity?.getPerWeekCourse(param1!!)!!
                Handler(Looper.getMainLooper()).post {
                    for (ele in weekCourse) {
                        val couseName = ele.courseName
                        val courseTime = ele.time
                        val couseDayOfWeek = ele.dayOfWeek
                        val courseAddress = ele.address
                        val courseColor = ele.color
                        val cardTag = "${couseDayOfWeek.toString()}${courseTime.toString()}"
                        Log.d("cardTag", cardTag)
                        val cardView = view?.findViewWithTag<CardView>(cardTag)

                        if (cardView == null) {
                            Log.d("cardView", "is empty")
                        }
                        Log.d("cardView", "cardView is getted")
                        //TODO 字体的调整问题
                        //TODO 在cardView中加入LinearLayout
                        with(cardView) {
                            //对于一个CardView，先设置其颜色
                            this?.removeAllViews()
                            if (courseColor != null) {
                                this?.setCardBackgroundColor(courseColor)
                            } else {
                                this?.setCardBackgroundColor(
                                    resources.getColor(
                                        R.color.colorPrimary, context.theme
                                    )
                                )
                            }
                            //然后对这个CardView，创建Linearlayout
                            val layout = LinearLayout(context)
                            layout.setLayoutParams(
                                LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            )
                            layout.orientation = LinearLayout.VERTICAL // 所有组件垂直摆放
                            val textCourse = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ) // 定义文本显示组件
                            val textViewCourse = TextView(context)
                            with(textViewCourse) {
                                Log.d("textViewCourse", "get in textViewCourse")

                                text = couseName
                                Log.d("text", couseName)
                                setLines(3)
                                layoutParams = textCourse
                                setEllipsize(TextUtils.TruncateAt.valueOf("END"))
                                setEms(1)
                                setTextColor(
                                    resources.getColor(
                                        R.color.design_default_color_on_primary,
                                        context.theme
                                    )
                                )
                            }
                            layout.addView(textViewCourse)
                            val textViewCourseAddress = TextView(context)
                            with(textViewCourseAddress) {
                                text = courseAddress
                                setLines(2)
                                layoutParams = textCourse
                                setEllipsize(TextUtils.TruncateAt.valueOf("END"))
                                setEms(1)
                                setTextColor(
                                    resources.getColor(
                                        R.color.design_default_color_on_primary,
                                        context.theme
                                    )
                                )
                            }
                            layout.addView(textViewCourseAddress)
                            this?.addView(layout)
                            val height = this?.height
                            Log.d("height", height.toString())
                            this?.setOnClickListener {
                                val courseId = ele.courseId
                                val intent = Intent(
                                    this@WeekCourseFragment.context,
                                    CourseAddActivity::class.java
                                )
                                intent.putExtra(UNIQUE_COURSE_DETAIL, courseId)
                                startActivityForResult(intent, INTENT_ADD_COURSE)
                            }
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
