package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import indi.hitszse2020g6.wakeapp.dummy.CourseWeek
import indi.hitszse2020g6.wakeapp.eventDetail.EventDetailList
import indi.hitszse2020g6.wakeapp.eventDetail.EventReminderList
import indi.hitszse2020g6.wakeapp.eventDetail.MyDescriptionRecyclerViewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
        TimePickFragment.TimePickerDialogListener,
        WeekPickerFragment.WeekPickerDialogListner,
        CourseChangeSelectFragment.CourseChangeSelectDailogListner{
    private var isNewCourse = true
    private var courseId: Long? = null
    private val chineseWeek = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    private var courseName:String = ""
    private var HaveFlag:Int = 0
    private var listSize: Int = 0
    private var repeatCourse:String = ""
    private var repeatCourseDate = CourseDate()
    private val resultList = arrayListOf<CourseDate>()
    companion object {
        var detail = CourseDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.course_add_activity)

        EventDetailList.ITEMS.clear()
        EventReminderList.ITEMS.clear()
        CourseWeek.ITEMS.clear()


        if (intent.extras != null) {
            //是老事件
            //对于老事件，只能够进行修改备注，时间不能被修改
            isNewCourse = false
            courseId = intent.getLongExtra(UNIQUE_COURSE_DETAIL, -1)
            //将其设置为不可见
            findViewById<ConstraintLayout>(R.id.canBeHide).visibility = ViewGroup.GONE
            findViewById<TextView>(R.id.CourseDetailAdd_title).visibility = ViewGroup.GONE
            val courseDetail = CourseList.getCourseById(courseId!!).first()
            courseName = courseDetail.courseName    //用气作为查找数据库的标识
            detail.courseName = courseDetail.courseName
            detail.courseAddress = courseDetail.address
            detail.courseTime = courseDetail.time
            detail.courseWeekBegin = courseDetail.week
            detail.courseWeekEnd = courseDetail.week
            detail.courseDayOfWeek = courseDetail.dayOfWeek
            detail.alarm = courseDetail.notice
            detail.focus = courseDetail.focus
            detail.mute = courseDetail.mute
            EventDetailList.ITEMS = courseDetail.detail.toMutableList()
            EventReminderList.ITEMS =courseDetail.reminder.toMutableList()
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
            //对于新事件，啥都是新的
            findViewById<TextView>(R.id.CourseDetailChange_title).visibility = ViewGroup.GONE
            detail.courseWeekEnd = 0
            detail.dateList.clear()
            detail.courseName = ""
            detail.courseWeekBegin = 0
            detail.courseTime = 0
            detail.courseDayOfWeek = 0
            detail.alarm = true
            detail.focus = true
            detail.mute = true
        }

        findViewById<ImageButton>(R.id.courseDetail_confirm).setOnClickListener {
            if (isNewCourse) {
                    //是新课程，输入的正确性检查
                    resultList.clear()
                    HaveFlag = 0
                    listSize = 0
                    repeatCourse = ""

                    if (detail.courseWeekBegin > detail.courseWeekEnd){
                        Toast.makeText(this@CourseAddActivity,"小猫咪说你的课程时间的设置错了啦",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        for(ele in 0 until CourseWeek.ITEMS.size){
                            if(CourseWeek.ITEMS[ele].weekBegin > CourseWeek.ITEMS[ele].weekEnd){
                                Toast.makeText(this@CourseAddActivity,"小猫咪说你第${(ele+1)}个时间设置错了噢",Toast.LENGTH_SHORT).show()
                                HaveFlag = 1
                            }
                        }
                        if(HaveFlag == 0){
                            //时间设置上没有错误，查看会不会和课程表的冲突
                            for(week in detail.courseWeekBegin .. detail.courseWeekEnd){
                                if((week != 0) && (detail.courseDayOfWeek != 0 ) &&(detail.courseTime) != 0){//表示有设置
                                    val list = CourseList.selectCourseByTime(
                                        week,detail.courseDayOfWeek,detail.courseTime
                                    )
                                    if(list.isNotEmpty()){
                                        listSize = list.size
                                        repeatCourse = list.first().courseName
                                        repeatCourseDate.weekBegin = list.first().week
                                        repeatCourseDate.dayOfWeek = list.first().dayOfWeek
                                        repeatCourseDate.time = list.first().time
                                        HaveFlag = 1
                                    }
                                    else{
                                        val result = CourseDate()
                                        result.time = detail.courseTime
                                        result.weekEnd = week
                                        result.weekBegin = week
                                        result.dayOfWeek = detail.courseDayOfWeek
                                        resultList.add(result)
                                    }
                                }
                            }
                            if(HaveFlag == 1){
                                Toast.makeText(this@CourseAddActivity,
                                    "小猫咪说在你的第1个课程时间设置中，已经有第${repeatCourseDate.weekBegin}周星期${chineseWeek[repeatCourseDate.dayOfWeek - 1]}第${repeatCourseDate.time}节的${repeatCourse}等${listSize}门课程啦，请检查",
                                    Toast.LENGTH_SHORT).show()
                            } else if(HaveFlag == 0){
                                for(ele in 0 until CourseWeek.ITEMS.size){
                                    for(week in CourseWeek.ITEMS[ele].weekBegin ..CourseWeek.ITEMS[ele].weekEnd){
                                        if((week != 0)&&(CourseWeek.ITEMS[ele].dayOfWeek != 0)&&(CourseWeek.ITEMS[ele].time != 0)){
                                            val list = CourseList.selectCourseByTime(
                                                week,CourseWeek.ITEMS[ele].dayOfWeek,CourseWeek.ITEMS[ele].time
                                            )
                                            if(list.isNotEmpty()){
                                                listSize = list.size
                                                repeatCourse = list.first().courseName
                                                HaveFlag = 1
                                                repeatCourseDate.weekBegin = list.first().week
                                                repeatCourseDate.dayOfWeek = list.first().dayOfWeek
                                                repeatCourseDate.time = list.first().time
                                            }
                                            else{
                                                val result = CourseDate()
                                                result.time = CourseWeek.ITEMS[ele].time
                                                result.weekEnd = week
                                                result.weekBegin = week
                                                result.dayOfWeek = CourseWeek.ITEMS[ele].dayOfWeek
                                                resultList.add(result)
                                                HaveFlag = 0
                                            }

                                        }
                                        if(HaveFlag == 1)   break
                                    }
                                    if(HaveFlag == 1)   break
                                }
                            }
                            if(HaveFlag == 1){
                                Toast.makeText(this@CourseAddActivity,
                                    "小猫咪说在你的第1个课程时间设置中，已经有第${repeatCourseDate.weekBegin}周星期${chineseWeek[repeatCourseDate.dayOfWeek - 1]}第${repeatCourseDate.time}节的${repeatCourse}等课程啦，请检查",
                                    Toast.LENGTH_SHORT).show()
                            } else if(HaveFlag == 0){
                                //表示没有找到跟设置时间段冲突的课程
                                for(ele in resultList){
                                    for(week in ele.weekBegin..ele.weekEnd){
                                        val course = Course(
                                            0,//id
                                            detail.courseName,
                                            week,
                                            ele.dayOfWeek,
                                            detail.courseAddress,
                                            ele.time,
                                            getColor(R.color.CourseTableColor1),
                                            detail.alarm,
                                            detail.focus,
                                            detail.mute,
                                            EventDetailList.ITEMS.toList(),
                                            EventReminderList.ITEMS.toList()
                                        )
                                        CourseList.insertCourse(course)
                                    }
                                }
                                val data = Intent()
                                setResult(RESULT_ADD_NEW_COURSE, data)
                                finish()
                            }
                        }
                    }
                } else {
                    //不是一个新的课程
                    //只能修改课程名称/课程地点/备注/以及提醒我
                    //TODO 提醒我还要等带庚宝，所以暂时留个坑给提醒我，一起填上就好了

                    //先准备好要存的数据，EventDetailList会自动更新，所需不需要特地读取，之后将其直接存入即可
                    detail.courseName =
                        findViewById<EditText>(R.id.addCourseDetail_courseName).text.toString()
                    detail.courseAddress =
                        findViewById<EditText>(R.id.addCourseDetail_courseAddress).text.toString()
                    //弹出选择范围的弹窗
                    CourseChangeSelectFragment().show(supportFragmentManager,"WeekPickerFragment")


                }

        }

        findViewById<ImageButton>(R.id.courseDetail_cancel).setOnClickListener {
            finish()
        }
        //连上小闹钟

        findViewById<ImageButton>(R.id.course_alarm).apply {
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
        findViewById<TextView>(R.id.courseDetail_time_week).apply {
            setOnClickListener{
                WeekPickerFragment(-1).show(supportFragmentManager,"WeekPickerFragment")

            }
        }
        //修改时间信息：星期几，第几节
        findViewById<TextView>(R.id.courseDetail_time).apply {
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
                EventReminderList.ITEMS.add(Reminder(0, ring = false, vibration = false, notification = false, ""))
                findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter?.notifyItemInserted(EventReminderList.ITEMS.size)
                findViewById<NestedScrollView>(R.id.addCourseDetail_mainContainer).apply { post {
                    fullScroll(ScrollView.FOCUS_DOWN)
                } }
            }
        }

        //删除按钮
        findViewById<CardView>(R.id.deleteCard).setOnClickListener {
            //删掉这门课程
            courseId?.let { it1 -> CourseList.deleteCourseById(it1) }
        //删掉之后返回
            val data = Intent()
            setResult(RESULT_ADD_NEW_COURSE, data)
            finish()
        }
    }
    //选择星期的dialog
    override fun onDialogPositiveClickForWeek(dialog: DialogFragment) {
        val pos = (dialog as WeekPickerFragment).position
        if(pos == -1){
            detail.courseWeekBegin = (dialog as WeekPickerFragment).weekBegin
            detail.courseWeekEnd = (dialog as WeekPickerFragment).weekEnd
            findViewById<TextView>(R.id.courseDetail_time_week).text = this@CourseAddActivity.getString(
                R.string.courseDetail_timeContentWeek
            ).format(
                detail.courseWeekBegin,
                detail.courseWeekEnd,
            )
        }
        else{
            CourseWeek.ITEMS[pos].weekBegin = (dialog as WeekPickerFragment).weekBegin
            CourseWeek.ITEMS[pos].weekEnd = (dialog as WeekPickerFragment).weekEnd
            findViewById<RecyclerView>(R.id.course_time_add_list_container).adapter?.notifyItemChanged(pos)

        }
    }

    //选择星期几/第几节的dialog
    override fun onDialogPositiveClickForTime(dialog: DialogFragment) {
        val pos = (dialog as TimePickFragment).position
        if(pos == -1){
            //表示是从主页传进去的
            detail.courseTime = (dialog as TimePickFragment).time
            detail.courseDayOfWeek = (dialog as TimePickFragment).dayOfWeek
            findViewById<TextView>(R.id.courseDetail_time).text = this@CourseAddActivity.getString(
                R.string.courseDetail_timeContent
            ).format(
                chineseWeek[detail.courseDayOfWeek - 1],
                detail.courseTime
            )
        }
        else{
            //表示是从recycleView里面传进去的
            CourseWeek.ITEMS[pos].dayOfWeek = (dialog as TimePickFragment).dayOfWeek
            CourseWeek.ITEMS[pos].time = (dialog as TimePickFragment).time
            findViewById<RecyclerView>(R.id.course_time_add_list_container).adapter?.notifyItemChanged(pos)
        }
    }



    //TODO 提醒我 还要等带庚宝，所以暂时留个坑
    override fun onDialogPositiveClickForCourseChangeSelect(dialog: DialogFragment) {
        val select = (dialog as CourseChangeSelectFragment).selectItem

        if(select == 0){
            courseId?.let {
                CourseList.updateCourseDetailById(
                    detail.courseName,
                    detail.courseAddress,
                    detail.alarm,
                    detail.focus,
                    detail.mute,
                    EventDetailList.ITEMS.toList(),
                    EventReminderList.ITEMS.toList(),
                    it
                )
            }
        }
        else if(select == 1){
            CourseList.updateCourseDetailByTime(
                detail.courseName,
                detail.courseAddress,
                detail.alarm,
                detail.focus,
                detail.mute,
                EventDetailList.ITEMS.toList(),
                EventReminderList.ITEMS.toList(),
                detail.courseTime,
                courseName
            )
        }
        else if(select == 2){
            CourseList.updateCourseDetailByName(
                detail.courseName,
                detail.courseAddress,
                detail.alarm,
                detail.focus,
                detail.mute,
                EventDetailList.ITEMS.toList(),
                EventReminderList.ITEMS.toList(),
                courseName
            )
        }
        val data = Intent()
        setResult(RESULT_ADD_NEW_COURSE, data)
        finish()


    }

    override fun onDialogNegativeClickForCourseChangeSelect(dialog: DialogFragment) {

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



    override fun onDialogNegativeClickForTime(dialog: DialogFragment) {
        //Of Course Do noting
    }

    override fun onDialogNegativeClickForWeek(dialog: DialogFragment) {
        //Of Course Do noting
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("changeTheme", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("changed",false)){
            val tmp = getSharedPreferences("redGreenBlue", Context.MODE_PRIVATE)
            val red = tmp.getInt("red",43)
            val green = tmp.getInt("green",44)
            val blue = tmp.getInt("blue",48)
            val editor = sharedPreferences.edit()
            editor.putBoolean("changed",false)
            editor.apply()
            ThemeColors.setNewThemeColor(this,red,green,blue)
        }
    }
}
