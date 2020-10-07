package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import indi.hitszse2020g6.wakeapp.eventDetail.ScheduleDetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val RESULT_ADD_NEW_COURSE = 5
const val UNIQUE_COURSE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_COURSE_DETAIL"
class CourseAddActivity : AppCompatActivity() {
    private var isNewCourse = true
    private var courseId : Long? = null

    companion object{
        var alarm = true
        var focus = true
        var mute = true

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_add)
        if(intent.extras != null){
            //是老事件
            isNewCourse = false
            courseId = intent.getLongExtra(UNIQUE_COURSE_DETAIL,-1)
            GlobalScope.launch(Dispatchers.IO){
                val courseDetail = AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().getCourseWithId(
                    courseId!!
                )

                Handler(Looper.getMainLooper()).post {
                    val courseName = courseDetail[0].courseName
                    val courseAddress = courseDetail[0].address
                    Log.d("get form databese","")
                    alarm = courseDetail[0].notice
                    focus = courseDetail[0].focus
                    mute = courseDetail[0].mute
                    Log.d("alarm",alarm.toString())
                    Log.d("focus",focus.toString())
                    Log.d("focus",focus.toString())
                    findViewById<EditText>(R.id.addCourseDetail_courseName).setText(courseName)
                    findViewById<EditText>(R.id.addCourseDetail_courseAddress).setText(courseAddress)
                }
            }
        }
        findViewById<ImageButton>(R.id.courseDetail_confirm).setOnClickListener{
            if(isNewCourse){
                Log.d("isNewCourse", isNewCourse.toString())
                val courseName = findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                val courseAddress = findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                Log.d("alarm2", alarm.toString())
                Log.d("focus2", focus.toString())
                Log.d("mute2", mute.toString())
                if(courseName != null){
                    val course  = Course(
                        courseName, 1, 1, courseAddress, 3, getColor(R.color.CourseTableColor1),
                        alarm, focus, mute
                    )
                        Log.d("courseName", courseName)
                        AppRoomDB.getDataBase(this).getDAO().insert(course)
                        val data = Intent()
                        setResult(RESULT_ADD_NEW_COURSE,data)
                        finish()
                }
            }else{
                //不是一个新的课程
                Log.d("isNotNewCourseadd",isNewCourse.toString())
                val courseName = findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                val courseAddress = findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                Log.d("courseName",courseName.toString())
                Log.d("courseAddress",courseAddress.toString())
                if(courseName != null){
                    GlobalScope.launch(Dispatchers.IO){
                        courseId?.let { it1 ->

                            AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().updateCourseDetails(
                                courseName,courseAddress, alarm, focus, mute,
                                courseId!!
                            )
                            val courseDetail = AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().getCourseWithId(
                                courseId!!
                            )
                            Log.d("courseDetail",courseDetail.toString())

                        }
                        Handler(Looper.getMainLooper()).post{
                            val data = Intent()
                            setResult(RESULT_ADD_NEW_COURSE,data)
                            finish()
                        }
                    }
                }
            }

        }
        findViewById<ImageButton>(R.id.courseDetail_cancel).setOnClickListener {
            finish()
        }
        //连上小闹钟
        findViewById<ImageButton>(R.id.course_alarm).apply {
            Log.d("alarm3", alarm.toString())
            toggleImageDrawable(this, alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            setOnClickListener{
                alarm = !alarm
                toggleImageDrawable(this, alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            }
        }
        //连上小手机
        findViewById<ImageButton>(R.id.course_focus).apply{
            Log.d("focus3", focus.toString())
            toggleImageDrawable(this, focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            setOnClickListener {
                focus = !focus
                toggleImageDrawable(this, focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            }
        }
        findViewById<ImageButton>(R.id.course_mute).apply {
            Log.d("mute3", mute.toString())
            toggleImageDrawable(this,mute,R.drawable.mute_on_24,R.drawable.mute_off_24)
            setOnClickListener{
                mute = !mute
                toggleImageDrawable(this,mute,R.drawable.mute_on_24,R.drawable.mute_off_24)
            }
        }
    }
    private fun toggleImageDrawable(btn: ImageButton, on: Boolean, onID: Int, offID: Int) {
        with(btn) {
            setImageDrawable(
                if(on) {
                    ContextCompat.getDrawable(context, onID)
                } else {
                    ContextCompat.getDrawable(context, offID)
                }
            )
        }
    }
}