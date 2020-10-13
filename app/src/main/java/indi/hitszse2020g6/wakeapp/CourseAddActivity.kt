package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import indi.hitszse2020g6.wakeapp.dummy.CourseWeek
import indi.hitszse2020g6.wakeapp.eventDetail.EventDetailList
import indi.hitszse2020g6.wakeapp.eventDetail.EventReminderList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val RESULT_ADD_NEW_COURSE = 5
const val UNIQUE_COURSE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_COURSE_DETAIL"
const val MY_REQUESET_CODE = 5

class CourseDate{
    var weekBegin:Int = 0
    var weekEnd :Int = 0
    var time : Int = 0
    var dayOfWeek :Int = 0
}
class CourseDetails {
    var courseName: String = ""
    var courseAddress: String = ""
    var dateList = arrayListOf<CourseDate>()
    var courseWeekBegin :Int = 0
    var courseWeekEnd :Int = 0
    var courseTime: Int = 0
    var courseDayOfWeek:Int = 0
    var alarm = true
    var focus = true
    var mute = true
}

class CourseAddActivity : AppCompatActivity(),
        TimePickFragment.TimePickerDialogListener,WeekPickerFragment.WeekPickerDialogListner{
    private var isNewCourse = true
    private var courseId: Long? = null
    private val chineseWeek = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    
    companion object {
        var detail = CourseDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_add_activity)

        EventDetailList.ITEMS.clear()
        EventReminderList.ITEMS.clear()
        CourseWeek.ITEMS.clear()

        this.lifecycleScope.launch(Dispatchers.Main) {
            if (intent.extras != null) {
                //是老事件
                isNewCourse = false
                courseId = intent.getLongExtra(UNIQUE_COURSE_DETAIL, -1)
                //将其设置为不可见
                findViewById<ConstraintLayout>(R.id.canBeHide).visibility = ViewGroup.GONE
                val courseDetail = withContext(Dispatchers.IO) {
                    AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().getCourseById(
                        courseId!!
                    ).first()
                }
                detail.courseName = courseDetail.courseName
                detail.courseAddress = courseDetail.address
                detail.courseTime = courseDetail.time
                detail.courseWeekBegin = courseDetail.week
                detail.courseWeekEnd = courseDetail.week
                detail.courseDayOfWeek = courseDetail.dayOfWeek
                detail.alarm = courseDetail.notice
                detail.focus = courseDetail.focus
                detail.mute = courseDetail.mute
                //修改显示
                findViewById<EditText>(R.id.addCourseDetail_courseName).setText(detail.courseName)
                findViewById<EditText>(R.id.addCourseDetail_courseAddress).setText(detail.courseAddress)

                findViewById<TextView>(R.id.courseDetail_time).text = this@CourseAddActivity.getString(
                    R.string.courseDetail_timeContent
                ).format(
                    chineseWeek[detail.courseDayOfWeek - 1],
                    detail.courseTime
                )
                findViewById<TextView>(R.id.courseDetail_time_week).text = this@CourseAddActivity.getString(
                    R.string.courseDetail_timeContentWeek
                ).format(
                    detail.courseWeekBegin,
                    detail.courseWeekEnd,
                )
            } else {
                detail.alarm = true
                detail.focus = true
                detail.mute = true
            }

            findViewById<ImageButton>(R.id.courseDetail_confirm).setOnClickListener {
                this@CourseAddActivity.lifecycleScope.launch(Dispatchers.Main) {
                    if (isNewCourse) {
                        if((detail.courseTime != 0)&&(detail.courseWeekBegin != 0)
                            &&(detail.courseWeekEnd != 0)&&(detail.courseDayOfWeek != 0)){//对时间有做修改
                            detail.courseName =
                                findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                            detail.courseAddress =
                                findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                            //根据时间段将时间拆分
                            for(week in detail.courseWeekBegin ..detail.courseWeekEnd){
                                val course = Course(
                                    0,
                                    detail.courseName,
                                    week,
                                    detail.courseDayOfWeek,
                                    detail.courseAddress,
                                    detail.courseTime,
                                    getColor(R.color.CourseTableColor1),
                                    detail.alarm,
                                    detail.focus,
                                    detail.mute
                                )
                                withContext(Dispatchers.IO) {
                                    AppRoomDB.getDataBase(it.context).getDAO().insertCourse(course)
                                }
                            }
                            val data = Intent()
                            setResult(RESULT_ADD_NEW_COURSE, data)
                            finish()
                        }
                    } else {
                        //不是一个新的课程
                        detail.courseName =
                            findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                        detail.courseAddress =
                            findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                        if (courseId != null) {
                            if(detail.courseWeekBegin <= detail.courseWeekEnd){
                                withContext(Dispatchers.IO){
                                    AppRoomDB.getDataBase(this@CourseAddActivity).getDAO().
                                    deleteCourseById(courseId!!)
                                }
                                for(week in detail.courseWeekBegin ..detail.courseWeekEnd){
                                    Log.d("week:",week.toString())
                                    withContext(Dispatchers.IO) {
                                        val course = Course(
                                            0,
                                            detail.courseName,
                                            week,
                                            detail.courseDayOfWeek,
                                            detail.courseAddress,
                                            detail.courseTime,
                                            getColor(R.color.CourseTableColor1),
                                            detail.alarm,
                                            detail.focus,
                                            detail.mute
                                        )
                                        withContext(Dispatchers.IO) {
                                            AppRoomDB.getDataBase(it.context).getDAO().insertCourse(course)
                                        }
                                    }
                                }
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
                    WeekPickerFragment(-1).show(supportFragmentManager,"WeekPickerFragment")

                }
            }
            //修改时间信息：星期几，第几节
            findViewById<CardView>(R.id.courseDetail_timeAddCard).apply {
                setOnClickListener {
                    TimePickFragment(-1).show(supportFragmentManager, "TimePickFragment")

                }
            }
            findViewById<ImageButton>(R.id.CourseAddDetail_addCourseTime).setOnClickListener {
                val courseDate = CourseDate()
                courseDate.time = 0
                courseDate.weekBegin = 0
                courseDate.weekEnd = 0
                courseDate.dayOfWeek = 0
                CourseWeek.ITEMS.add(courseDate)
                findViewById<RecyclerView>(R.id.course_time_add_list_container).adapter?.notifyItemInserted(
                    CourseWeek.ITEMS.size
                )

            }
            findViewById<ImageButton>(R.id.CourseDescription_Add).setOnClickListener {
                if(EventDetailList.ITEMS.size < 10) {
                    EventDetailList.ITEMS.add(Detail("", ""))
                    findViewById<RecyclerView>(R.id.eventDetail_descriptionListContainer).adapter?.notifyItemInserted(EventDetailList.ITEMS.size)
                }
            }
            findViewById<ImageButton>(R.id.CourseReminder_Add).setOnClickListener {
                if(EventReminderList.ITEMS.size < 10) {
                    EventReminderList.ITEMS.
                    add(Reminder(0, ring = false, vibration = false, notification = false, ""))
                    findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter?.notifyItemInserted(EventReminderList.ITEMS.size)
                    findViewById<ScrollView>(R.id.addCourseDetail_mainContainer).apply { post {
                        fullScroll(ScrollView.FOCUS_DOWN)
                    } }
                }
            }

        }


    }

    override fun onDialogPositiveClickForTime(dialog: DialogFragment) {
        // User touched the dialog's positive button
        detail.courseTime = (dialog as TimePickFragment).time
        detail.courseDayOfWeek = (dialog as TimePickFragment).dayOfWeek
        findViewById<TextView>(R.id.courseDetail_time).text = this@CourseAddActivity.getString(
            R.string.courseDetail_timeContent
        ).format(
            chineseWeek[detail.courseDayOfWeek - 1],
            detail.courseTime
        )
    }

    override fun onDialogNegativeClickForTime(dialog: DialogFragment) {
        //Of Course Do noting
    }

    override fun onDialogPositiveClickForWeek(dialog: DialogFragment) {
        detail.courseWeekBegin = (dialog as WeekPickerFragment).weekBegin
        detail.courseWeekEnd = (dialog as WeekPickerFragment).weekEnd
        Log.d("pos",(dialog as WeekPickerFragment).position.toString())
        Log.d("courseWeekBegin",detail.courseWeekBegin.toString())
        Log.d("courseWeekEnd",detail.courseWeekEnd.toString())

        findViewById<TextView>(R.id.courseDetail_time_week).text = this@CourseAddActivity.getString(
            R.string.courseDetail_timeContentWeek
        ).format(
            detail.courseWeekBegin,
            detail.courseWeekEnd,
        )
    }

    override fun onDialogNegativeClickForWeek(dialog: DialogFragment) {
        //Of Course Do noting
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
