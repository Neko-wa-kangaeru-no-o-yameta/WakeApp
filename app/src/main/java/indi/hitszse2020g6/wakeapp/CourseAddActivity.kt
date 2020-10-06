package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

const val RESULT_ADD_NEW_COURSE = 5
class CourseAddActivity : AppCompatActivity() {
    private var isNewCourse = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_add)
        if(intent.extras != null){
            //是老事件
            isNewCourse = true
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
                }
            }
            finish()
        }
        findViewById<ImageButton>(R.id.courseDetail_cancel).setOnClickListener {
            finish()
        }
    }
}