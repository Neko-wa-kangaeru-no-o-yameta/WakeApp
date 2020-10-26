package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_course.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        Log.d("这是第几周:",param1.toString())

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
        Log.d("get in", "get in")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INTENT_ADD_COURSE) {
            //直接更新
            Log.d("get in2", "get in")
            param1?.toInt()?.let {
                requireActivity().findViewById<ViewPager2>(R.id.viewPager2).adapter?.notifyDataSetChanged()
//                requireActivity().findViewById<ViewPager2>(R.id.viewPager2).currentItem = it
            }
            updateCourseCardView()
            if(resultCode == RESULT_ADD_NEW_COURSE){
                Toast.makeText(context,"小猫咪帮你更新课程表啦", Toast.LENGTH_SHORT).show()
            }else{
//                Toast.makeText(context,"小猫咪很乖的没动你的课表啦", Toast.LENGTH_SHORT).show()
            }

        }


    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume",param1.toString())
    }

    fun updateCourseCardView() {
        view?.findViewById<GridLayout>(R.id.GridLayout).apply {


        val weekCourse = this@WeekCourseFragment.requireContext().getCourseOfTheWeek(param1!!)

        for (courseDayOfWeek in 1..7) {
            for (courseTime in 1..6) {
                val cardTag = "$courseDayOfWeek$courseTime"
                val cardView = view?.findViewWithTag<CardView>(cardTag)!!
                cardView.removeAllViews()
                cardView.setCardBackgroundColor(resources.getColor(R.color.colorBackground,
                    context?.theme
                ))
            }
        }

        for (ele in weekCourse) {
                    val courseName = ele.courseName
                    val courseTime = ele.time
                    val courseDayOfWeek = ele.dayOfWeek
                    val courseAddress = ele.address
                    val courseColor = ele.color
                    val cardTag = "$courseDayOfWeek$courseTime"
                    val cardView = view?.findViewWithTag<CardView>(cardTag)

                    if (cardView == null) {
                        Log.d("cardView", "not found cardView of tag $cardTag")
                        continue
                    } else {
                        Log.d("cardView", "cardView is gotten")
                    }
                    //TODO 字体的调整问题
                    //TODO 在cardView中加入LinearLayout
                    with(cardView) {
                        //对于一个CardView，先设置其颜色
                        this.removeAllViews()
                        if (courseColor != null) {
                            this.setCardBackgroundColor(courseColor)
                        } else {
                            this.setCardBackgroundColor(
                                resources.getColor(
                                    R.color.colorPrimary, context.theme
                                )
                            )
                        }
                        //然后对这个CardView，创建LinearLayout
                        val layout = LinearLayout(context)
                        layout.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        layout.orientation = LinearLayout.VERTICAL // 所有组件垂直摆放
                        val textCourse = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ) // 定义文本显示组件
                        val textViewCourse = TextView(context)
                        with(textViewCourse) {
                            Log.d("textViewCourse", "get in textViewCourse")

                            text = courseName
                            Log.d("text", courseName)
                            setLines(3)
                            layoutParams = textCourse
                            ellipsize = TextUtils.TruncateAt.valueOf("END")
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
                            ellipsize = TextUtils.TruncateAt.valueOf("END")
                            setEms(1)
                            setTextColor(
                                resources.getColor(
                                    R.color.design_default_color_on_primary,
                                    context.theme
                                )
                            )
                        }
                        layout.addView(textViewCourseAddress)
                        this.addView(layout)
                        Log.d("height", this.height.toString())
                        this.setOnClickListener {
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
