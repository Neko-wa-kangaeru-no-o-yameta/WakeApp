package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val RESULT_ADD_NEW_COURSE = 5
const val UNIQUE_COURSE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_COURSE_DETAIL"
const val MY_REQUESET_CODE = 5

class CourseDetails {
    var courseWeek :Int = 0
    var courseTime: Int = 0
    var courseDayOfWeek:Int = 0
    var alarm = true
    var focus = true
    var mute = true
}

class CourseAddActivity : AppCompatActivity(),
        WeekPickerFragment.WeekPickerDialogListener{
    private var isNewCourse = true
    private var courseId: Long? = null
    private val chineseWeek = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

    companion object {

        var detail = CourseDetails()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.course_add_activity)

        this.lifecycleScope.launch(Dispatchers.Main) {
            if (intent.extras != null) {
                //是老事件
                isNewCourse = false
                courseId = intent.getLongExtra(UNIQUE_COURSE_DETAIL, -1)

                val courseDetail = withContext(Dispatchers.IO) {
                    AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().getCourseById(
                        courseId!!
                    ).first()
                }

                Log.d("get form database", "")

                val courseName = courseDetail.courseName
                val courseAddress = courseDetail.address

                detail.courseTime = courseDetail.time
                detail.courseWeek = courseDetail.week
                detail.courseDayOfWeek = courseDetail.dayOfWeek
                detail.alarm = courseDetail.notice
                detail.focus = courseDetail.focus
                detail.mute = courseDetail.mute
                //修改显示
                findViewById<EditText>(R.id.addCourseDetail_courseName).setText(courseName)
                findViewById<EditText>(R.id.addCourseDetail_courseAddress).setText(courseAddress)

                findViewById<TextView>(R.id.courseDetail_time).text = this@CourseAddActivity.getString(
                    R.string.courseDetail_timeContent
                ).format(
                    chineseWeek[detail.courseDayOfWeek - 1],
                    detail.courseTime
                )
                findViewById<TextView>(R.id.courseDetail_time_week).text = this@CourseAddActivity.getString(
                    R.string.courseDetail_timeContentWeek
                ).format(
                    detail.courseWeek
                )
            } else {
                detail.alarm = true
                detail.focus = true
                detail.mute = true
            }

            findViewById<ImageButton>(R.id.courseDetail_confirm).setOnClickListener {
                this@CourseAddActivity.lifecycleScope.launch(Dispatchers.Main) {
                    if (isNewCourse) {
                        Log.d("isNewCourse", isNewCourse.toString())
                        val courseName =
                            findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                        val courseAddress =
                            findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                        val course = Course(
                            0,
                            courseName, 1, 1, courseAddress, 3, getColor(R.color.CourseTableColor1),
                            detail.alarm, detail.focus, detail.mute
                        )
                        Log.d("courseName", courseName)
                        withContext(Dispatchers.IO) {
                            AppRoomDB.getDataBase(it.context).getDAO().insertCourse(course)
                        }
                        val data = Intent()
                        setResult(RESULT_ADD_NEW_COURSE, data)
                        finish()
                    } else {
                        //不是一个新的课程
                        val courseName =
                            findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                        val courseAddress =
                            findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                        if (courseId != null) {
                            withContext(Dispatchers.IO) {
                                AppRoomDB.getDataBase(this@CourseAddActivity).getDAO()
                                    .updateCourseDetails(
                                        courseName,
                                        courseAddress,
                                        detail.alarm,
                                        detail.focus,
                                        detail.mute,
                                        courseId!!
                                    )
                                val courseDetail =
                                    AppRoomDB.getDataBase(this@CourseAddActivity).getDAO()
                                        .getCourseById(
                                            courseId!!
                                        )
                                Log.d("courseDetail", courseDetail.toString())

                            }

                        }
                        val data = Intent()
                        setResult(RESULT_ADD_NEW_COURSE, data)
                        finish()
                    }
                }

            }

            findViewById<ImageButton>(R.id.courseDetail_cancel).setOnClickListener {
                finish()
            }
            //连上小闹钟

            findViewById<ImageButton>(R.id.course_alarm).apply {
                Log.d("alarm3", detail.alarm.toString())
                toggleImageDrawable(
                    this,
                    detail.alarm,
                    R.drawable.alarm_on_24,
                    R.drawable.alarm_off_24
                )
                setOnClickListener {
                    detail.alarm = !detail.alarm
                    toggleImageDrawable(
                        this,
                        detail.alarm,
                        R.drawable.alarm_on_24,
                        R.drawable.alarm_off_24
                    )
                }
            }
            //连上小手机
            findViewById<ImageButton>(R.id.course_focus).apply {
                Log.d("focus3", detail.focus.toString())
                toggleImageDrawable(
                    this,
                    detail.focus,
                    R.drawable.focus_on_24,
                    R.drawable.focus_off_24
                )
                setOnClickListener {
                    detail.focus = !detail.focus
                    toggleImageDrawable(
                        this,
                        detail.focus,
                        R.drawable.focus_on_24,
                        R.drawable.focus_off_24
                    )
                }
            }

            findViewById<ImageButton>(R.id.course_mute).apply {
                Log.d("mute3", detail.mute.toString())
                toggleImageDrawable(
                    this,
                    detail.mute,
                    R.drawable.mute_on_24,
                    R.drawable.mute_off_24
                )
                setOnClickListener {
                    detail.mute = !detail.mute
                    toggleImageDrawable(
                        this,
                        detail.mute,
                        R.drawable.mute_on_24,
                        R.drawable.mute_off_24
                    )
                }
            }
            //修改时间第几周
            findViewById<CardView>(R.id.courseDetail_timeAddCard_week).apply {
                setOnClickListener{


                }
            }
            //修改时间信息：星期几，第几节
            findViewById<CardView>(R.id.courseDetail_timeAddCard).apply {
                setOnClickListener {
                    WeekPickerFragment().show(supportFragmentManager, "WeekPickerFragment")

                }
            }

        }


    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        detail.courseTime = (dialog as WeekPickerFragment).time
        detail.courseDayOfWeek = (dialog as WeekPickerFragment).dayOfWeek
        findViewById<TextView>(R.id.courseDetail_time).text = this@CourseAddActivity.getString(
            R.string.courseDetail_timeContent
        ).format(
            chineseWeek[detail.courseDayOfWeek - 1],
            detail.courseTime
        )
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
    }
    private fun toggleImageDrawable(btn: ImageButton, on: Boolean, onID: Int, offID: Int) {
        with(btn) {
            setImageDrawable(
                if (on) {
                    ContextCompat.getDrawable(context, onID)
                } else {
                    ContextCompat.getDrawable(context, offID)
                }
            )
        }
    }

}