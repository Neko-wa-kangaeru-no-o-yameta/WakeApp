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
import androidx.lifecycle.lifecycleScope
import indi.hitszse2020g6.wakeapp.databinding.CourseAddActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val RESULT_ADD_NEW_COURSE = 5
const val UNIQUE_COURSE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_COURSE_DETAIL"

class CourseDetails {
    var courseTime: Int = 0
    var alarm = true
    var focus = true
    var mute = true
}

class CourseAddActivity : AppCompatActivity() {
    private var isNewCourse = true
    private var courseId: Long? = null

    private lateinit var viewBinding: CourseAddActivityBinding

    companion object {

        var detail = CourseDetails()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = CourseAddActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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
                Log.d("detail.courseTime1", detail.courseTime.toString())

                detail.alarm = courseDetail.notice
                detail.focus = courseDetail.focus
                detail.mute = courseDetail.mute

                viewBinding.addCourseDetailCourseName.setText(courseName)
                viewBinding.addCourseDetailCourseAddress.setText(courseAddress)
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
                Log.d("detail.courseTime3", detail.courseTime.toString())
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
            findViewById<CardView>(R.id.courseDetail_timeAddCard).apply {
                if (!isNewCourse) {
                    findViewById<TextView>(R.id.courseDetail_time).text = context.getString(
                        R.string.courseDetail_timeContent
                    ).format(
                        detail.courseTime
                    )
                }
                setOnClickListener {

                }
            }
        }


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