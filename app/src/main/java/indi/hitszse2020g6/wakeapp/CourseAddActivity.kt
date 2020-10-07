package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val RESULT_ADD_NEW_COURSE = 5
const val UNIQUE_COURSE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_COURSE_DETAIL"
class CourseDetails{
    var courseTime : Int = 0
    var alarm = true
    var focus = true
    var mute = true
}
class CourseAddActivity : AppCompatActivity() {
    private var isNewCourse = true
    private var courseId : Long? = null

    companion object{

        var detail = CourseDetails()


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_add)
        if(intent.extras != null){
            //是老事件
            isNewCourse = false
            courseId = intent.getLongExtra(UNIQUE_COURSE_DETAIL,-1)
            val courseDetail = AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().getCourseWithId(
                courseId!!
            )
            val courseName = courseDetail[0].courseName
            val courseAddress = courseDetail[0].address
            Log.d("get form databese","")
            detail.courseTime = courseDetail[0].time
            Log.d("detail.coursetime1", detail.courseTime.toString())
            detail.alarm = courseDetail[0].notice
            detail.focus = courseDetail[0].focus
            detail.mute = courseDetail[0].mute
            findViewById<EditText>(R.id.addCourseDetail_courseName).setText(courseName)
            findViewById<EditText>(R.id.addCourseDetail_courseAddress).setText(courseAddress)
        }else{
            detail.alarm = true
            detail.focus = true
            detail.mute = true
        }


        findViewById<ImageButton>(R.id.courseDetail_confirm).setOnClickListener{
            if(isNewCourse){
                Log.d("isNewCourse", isNewCourse.toString())
                val courseName = findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                val courseAddress = findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                val course  = Course(
                    courseName, 1, 1, courseAddress, 3, getColor(R.color.CourseTableColor1),
                    detail.alarm, detail.focus, detail.mute
                )
                Log.d("courseName", courseName)
                AppRoomDB.getDataBase(this).getDAO().insert(course)
                val data = Intent()
                setResult(RESULT_ADD_NEW_COURSE,data)
                finish()
            }else{
                //不是一个新的课程
                val courseName = findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                val courseAddress = findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                GlobalScope.launch(Dispatchers.IO){
                    courseId?.let { it1 ->
                        AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().updateCourseDetails(
                            courseName,courseAddress, detail.alarm, detail.focus, detail.mute,
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

        findViewById<ImageButton>(R.id.courseDetail_cancel).setOnClickListener {
            finish()
        }
        //连上小闹钟
        findViewById<ImageButton>(R.id.course_alarm).apply {
            Log.d("alarm3", detail.alarm.toString())
            toggleImageDrawable(this, detail.alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            setOnClickListener{
                detail.alarm = !detail.alarm
                toggleImageDrawable(this, detail.alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            }
        }
        //连上小手机
        findViewById<ImageButton>(R.id.course_focus).apply{
            Log.d("focus3", detail.focus.toString())
            toggleImageDrawable(this, detail.focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            setOnClickListener {
                detail.focus = !detail.focus
                toggleImageDrawable(this, detail.focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            }
        }

        findViewById<ImageButton>(R.id.course_mute).apply {
            Log.d("mute3", detail.mute.toString())
            Log.d("detail.coursetime3", detail.courseTime.toString())
            toggleImageDrawable(this,detail.mute,R.drawable.mute_on_24,R.drawable.mute_off_24)
            setOnClickListener{
                detail.mute = !detail.mute
                toggleImageDrawable(this,detail.mute,R.drawable.mute_on_24,R.drawable.mute_off_24)
            }
        }
        findViewById<CardView>(R.id.courseDetail_timeAddCard).apply {
            if(!isNewCourse){
                if(detail.courseTime != null){
                    findViewById<TextView>(R.id.courseDetail_time).text = context.getString(
                        R.string.courseDetail_timeContent
                    ).format(
                        detail.courseTime
                    )
                }
            }
            setOnClickListener {

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