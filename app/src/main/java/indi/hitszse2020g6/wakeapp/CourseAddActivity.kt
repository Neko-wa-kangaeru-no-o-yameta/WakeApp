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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val RESULT_ADD_NEW_COURSE = 5
const val UNIQUE_COURSE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_COURSE_DETAIL"
class CourseAddActivity : AppCompatActivity() {
    private var isNewCourse = true
    private var courseId : Long? = null
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
                    Log.d("courseId2", courseId.toString())
                    val courseName = courseDetail[0].courseName
                    val courseAddress = courseDetail[0].address
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
                if(courseName != null){
                    val course  = Course(
                        courseName, 1, 1, courseAddress, 3, getColor(R.color.CourseTableColor1)
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
                            Log.d("courseId2", courseId.toString())
                            AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().updateCourseDetails(courseName,courseAddress,
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
    }
}