package indi.hitszse2020g6.wakeapp.eventDetail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.binioter.guideview.Component
import com.binioter.guideview.GuideBuilder
import indi.hitszse2020g6.wakeapp.*
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlinx.android.synthetic.main.activity_schedule_detail.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.ArrayList

const val UNIQUE_ID_TO_SCHEDULE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_ID_FOR_MAIN_TO_SCHEDULE_DETAIL"

const val REQUEST_SCHEDULE_DETAIL_TO_WHITELIST = 1001

const val PARAM_SCHEDULE_DETAIL_TO_WHITELIST_JSON = "indi.hitszse2020g6.wakeapp.PARAM_SCHEDULE_DETAIL_TO_WHITELIST_JSON"
const val PARAM_WHITELIST_TO_SCHEDULE_DETAIL_JSON = "indi.hitszse2020g6.wakeapp.PARAM_WHITELIST_TO_SCHEDULE_DETAIL_JSON"

class MyTime {
    var year    : Int
    var month   : Int
    var date    : Int
    var hour    : Int
    var minute  : Int

    init {
        val c = Calendar.getInstance()
        year    = c.get(Calendar.YEAR)
        month   = c.get(Calendar.MONTH)
        date    = c.get(Calendar.DAY_OF_MONTH)
        hour    = c.get(Calendar.HOUR_OF_DAY)
        minute  = c.get(Calendar.MINUTE)
    }
}

class ScheduleDetailActivity :
    AppCompatActivity(),
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    RepeatTypeDialog.RepeatTypeListener,
    RepeatWeekdayDialog.RepeatWeekDayDialogListener,
    ReminderChooseDialog.ReminderChooseListener{

    private var isNewSchedule = true
    private lateinit var entryToEdit: EventTableEntry

    var settingStartTime = true

    var startTime = MyTime()
    var stopTime = MyTime()

    var alarm = true
    var focus = true
    var mute = true

    var hasWL = false
    var whiteList: List<String> = ArrayList()

    var repeatAt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_schedule_detail)
        setSupportActionBar(findViewById(R.id.scheduleDetail_actionBar))

        EventDetailList.ITEMS.clear()
        EventReminderList.ITEMS.clear()

        if(intent.extras != null) {
            val uid = intent.getLongExtra(UNIQUE_ID_TO_SCHEDULE_DETAIL, -1)
            for(entry in MainPageEventList.eventList) {
                if(entry.uid == uid) {
                    entryToEdit = entry

                    alarm = entryToEdit.notice
                    focus = entryToEdit.focus
                    mute = entryToEdit.mute

                    val start = Calendar.getInstance().apply {
                        timeInMillis = entryToEdit.startTime * 1000
                    }
                    startTime.year  = start.get(Calendar.YEAR)
                    startTime.month = start.get(Calendar.MONTH)
                    startTime.date  = start.get(Calendar.DAY_OF_MONTH)
                    startTime.hour  = start.get(Calendar.HOUR_OF_DAY)
                    startTime.minute= start.get(Calendar.MINUTE)

                    val stop = Calendar.getInstance().apply {
                        timeInMillis = entryToEdit.stopTime * 1000
                    }
                    stopTime.year  = stop.get(Calendar.YEAR)
                    stopTime.month = stop.get(Calendar.MONTH)
                    stopTime.date  = stop.get(Calendar.DAY_OF_MONTH)
                    stopTime.hour  = stop.get(Calendar.HOUR_OF_DAY)
                    stopTime.minute= stop.get(Calendar.MINUTE)

                    repeatAt = entryToEdit.repeatAt

                    EventDetailList.ITEMS = entryToEdit.detail.toMutableList()
                    EventReminderList.ITEMS = entryToEdit.reminder.toMutableList()
                    findViewById<EditText>(R.id.scheduleDetail_eventTitle).setText(entryToEdit.title)
                    Log.d("ScheduleDetailActivity ", "detail and reminder Loaded: ${entryToEdit.detail} & ${entryToEdit.detail}")
                    isNewSchedule = false
                    break
                }
            }
        }
        setRepeatList()

        findViewById<ImageButton>(R.id.scheduleDetail_confirm).setOnClickListener {
            val stop = Calendar.getInstance().apply {
                set(stopTime.year, stopTime.month, stopTime.date, stopTime.hour, stopTime.minute, 0)
            }
            
            val start = Calendar.getInstance().apply {
                set(startTime.year, startTime.month, startTime.date, startTime.hour, startTime.minute, 0)
            }

            if(isNewSchedule) {
                MainPageEventList.addSchedule(
                    title       = findViewById<EditText>(R.id.scheduleDetail_eventTitle).text.toString(),
                    detail      = EventDetailList.ITEMS.toList(),
                    reminder    = EventReminderList.ITEMS.toList(),
                    startTime   = start.timeInMillis /1000,
                    stopTime    = stop.timeInMillis / 1000,
                    focus       = focus,
                    mute        = mute,
                    notice      = alarm,
                    hasWL       = hasWL,
                    whiteList   = whiteList,
                    isAutoGen   = false,
                    isClass     = false,
                    classId     = -1,
                    repeatAt    = repeatAt
                )
            } else {
                entryToEdit.title       = findViewById<EditText>(R.id.scheduleDetail_eventTitle).text.toString()
                entryToEdit.detail      = EventDetailList.ITEMS.toList()
                entryToEdit.reminder    = EventReminderList.ITEMS.toList()
                entryToEdit.startTime   = start.timeInMillis / 1000
                entryToEdit.stopTime    = stop.timeInMillis / 1000
                entryToEdit.focus       = focus
                entryToEdit.mute        = mute
                entryToEdit.notice      = alarm
                entryToEdit.hasCustomWhiteList = hasWL
                entryToEdit.customWhiteList = whiteList
                entryToEdit.isAutoGen   = false
                entryToEdit.isClass     = false
                entryToEdit.classId     = -1
                MainPageEventList.updateEvent(entryToEdit)
            }
            finish()
        }

        findViewById<ImageButton>(R.id.scheduleDetail_cancel).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.scheduleDetail_addDetail).setOnClickListener {
            if(EventDetailList.ITEMS.size < 10) {
                EventDetailList.ITEMS.add(Detail("", ""))
                findViewById<RecyclerView>(R.id.eventDetail_descriptionListContainer).adapter?.notifyItemInserted(EventDetailList.ITEMS.size)
//                if(EventDetailList.ITEMS.size == 10) {
//                    disableAddButton(it as ImageButton)
//                }
            }
        }

        findViewById<ImageButton>(R.id.scheduleDetail_addReminder).setOnClickListener {
            if(EventReminderList.ITEMS.size < 10) {
                EventReminderList.ITEMS.add(Reminder(0, ring = false, vibration = false, notification = false, ""))
                findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter?.notifyItemInserted(EventReminderList.ITEMS.size)
                findViewById<NestedScrollView>(R.id.scheduleDetail_mainContainer).apply { post {
                    fullScroll(NestedScrollView.FOCUS_DOWN)
                } }
            }
        }

        findViewById<CardView>(R.id.scheduleDetail_startTimeCard).apply{
            if(!isNewSchedule) {
                findViewById<TextView>(R.id.scheduleDetail_startTimeText).text =  context.getString(
                    R.string.eventList_startTimeTVContent
                ).format(
                    startTime.month + 1,
                    startTime.date,
                    startTime.hour,
                    startTime.minute
                )
            }
            setOnClickListener {
                settingStartTime=true
                DatePickerFragment().show(supportFragmentManager, "DatePicker start")
            }
        }

        findViewById<CardView>(R.id.scheduleDetail_stopTimeCard).apply{
            if(!isNewSchedule) {
                findViewById<TextView>(R.id.scheduleDetail_stopTimeText).text =  context.getString(
                    R.string.eventList_stopTimeTVContent
                ).format(
                    stopTime.month + 1,
                    stopTime.date,
                    stopTime.hour,
                    stopTime.minute
                )
            }
            setOnClickListener {
                settingStartTime=false
                DatePickerFragment().show(supportFragmentManager, "DatePicker stop")
            }
        }


        findViewById<ImageButton>(R.id.scheduleDetail_alarm).apply {
            toggleImageDrawable(this, alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            setOnClickListener {
                alarm = !alarm
                toggleImageDrawable(this, alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            }
        }

        findViewById<ImageButton>(R.id.scheduleDetail_focus).apply {
            toggleImageDrawable(this, focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            setOnClickListener {
                focus = !focus
                toggleImageDrawable(this, focus, R.drawable.focus_on_24, R.drawable.focus_off_24)
            }
        }

        findViewById<ImageButton>(R.id.scheduleDetail_mute).apply {
            toggleImageDrawable(this, mute, R.drawable.mute_on_24, R.drawable.mute_off_24)
            setOnClickListener {
                mute = !mute
                toggleImageDrawable(this, mute, R.drawable.mute_on_24, R.drawable.mute_off_24)
            }
        }

        findViewById<CardView>(R.id.scheduleDetail_repeatCard).setOnClickListener {
            RepeatTypeDialog().show(supportFragmentManager, "RepeatTypeDialog")
        }

        findViewById<Button>(R.id.scheduleDetail_EditWhiteList).setOnClickListener {
            Log.d("ScheduleDetailActivity", "Starting White list selection")
            Log.d("ScheduleDetailActivity", "Initial value: $whiteList")
            val str = Json.encodeToString(whiteList)
            Log.d("ScheduleDetailActivity", "Json value: $str")
            val intent = Intent(this, ChooseWhiteListActivity::class.java)
            intent.putExtra(PARAM_SCHEDULE_DETAIL_TO_WHITELIST_JSON, str)
            startActivityForResult(intent, REQUEST_SCHEDULE_DETAIL_TO_WHITELIST)
        }

        val mySharedPreferences = getSharedPreferences("new_user", Context.MODE_PRIVATE)
        if (mySharedPreferences.getBoolean("isNewAddScheduleFragment", true)) {
            scheduleDetail_eventTitle.post { showTitleGuideView() }
            val editor = mySharedPreferences.edit()
            editor.putBoolean("isNewAddScheduleFragment", false)
            editor.apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ScheduleDetailActivity", "onActivityResult for requestCode = $requestCode and resultCode = $resultCode.")
        if(requestCode == REQUEST_SCHEDULE_DETAIL_TO_WHITELIST && resultCode == RESULT_OK) {
            val selected = data?.getStringExtra(PARAM_WHITELIST_TO_SCHEDULE_DETAIL_JSON)
            if(selected != null) {
                Log.d("ScheduleDetailActivity", "Got $selected.")
                whiteList = Json.decodeFromString(selected)
                Log.d("ScheduleDetailActivity", "Parse result: $whiteList.")
                for (item in whiteList){
                    Log.d("ScheduleDetailActivity",item)
                }
            } else {
                Log.d("ScheduleDetailActivity", "EMPTY RETURN FOR $REQUEST_SCHEDULE_DETAIL_TO_WHITELIST.")
            }
        }
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

    override fun onDateSet(view: DatePicker?, setYear: Int, setMonth: Int, setDayOfMonth: Int) {
        if(settingStartTime) {
            startTime.year = setYear
            startTime.month = setMonth
            startTime.date = setDayOfMonth
        } else {
            stopTime.year = setYear
            stopTime.month = setMonth
            stopTime.date = setDayOfMonth
        }
        Log.d("OnTimeSet", "$setYear : ${setMonth+1} : $setDayOfMonth ")
        supportFragmentManager.let { TimePickerFragment().show(it, "timePicker") }
    }

    override fun onTimeSet(view: TimePicker?, setHourOfDay: Int, setMinute: Int) {
        if(settingStartTime) {
            startTime.hour = setHourOfDay
            startTime.minute = setMinute
            findViewById<TextView>(R.id.scheduleDetail_startTimeText)?.text = getString(
                R.string.eventList_startTimeTVContent
            ).format(
                startTime.month + 1,
                startTime.date,
                startTime.hour,
                startTime.minute
            )
        } else {
            stopTime.hour = setHourOfDay
            stopTime.minute = setMinute
            findViewById<TextView>(R.id.scheduleDetail_stopTimeText)?.text = getString(
                R.string.eventList_stopTimeTVContent
            ).format(
                stopTime.month + 1,
                stopTime.date,
                stopTime.hour,
                stopTime.minute
            )
        }
        Log.d("OnTimeSet", "$setHourOfDay : $setMinute")
    }

    fun setRepeatList() {
        val repeatList = findViewById<LinearLayout>(R.id.scheduleDetail_repeatContent)
        repeatList.removeAllViews()
        if(repeatAt == 0) {
            val tv = TextView(this)
            tv.text = "只触发一次"
            repeatList.addView(tv)
        } else {
            for (i in 0 until 7) {
                if(repeatAt and (1 shl i) != 0) {
                    val tv = TextView(this)
                    tv.text = resources.getStringArray(R.array.repeat_weekday_types)[i]
                    repeatList.addView(tv)
                }
            }
        }
    }

    override fun onRepeatTypeSet(doRepeat: Boolean) {
        if(!doRepeat) {
            repeatAt = 0
            setRepeatList()
        } else {
            RepeatWeekdayDialog(repeatAt).show(supportFragmentManager, "Weekday picker")
        }
    }

    override fun onRepeatWeekdaySet(repeatAt: Int) {
        this.repeatAt = repeatAt
        setRepeatList()
    }

    private fun showTitleGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_eventTitle).setAlpha(150).setHighTargetPadding(10)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showScheduleMuteGuideView()
            }
        })
        builder.addComponent(AffairDetailActivity.TitleComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    private fun showScheduleMuteGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_mute).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showScheduleFocusGuideView()
            }
        })
        builder.addComponent(MuteComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class MuteComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_mute, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_LEFT
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return -20
        }

        override fun getYOffset(): Int {
            return -20
        }
    }

    private fun showScheduleFocusGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_focus).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showScheduleAlarmGuideView()
            }
        })
        builder.addComponent(ScheduleFocusComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class ScheduleFocusComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_focus, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_LEFT
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return -20
        }

        override fun getYOffset(): Int {
            return -20
        }
    }

    private fun showScheduleAlarmGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_alarm).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showSetStartTimeGuideView()
            }
        })
        builder.addComponent(AffairDetailActivity.AlarmComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    private fun showSetStartTimeGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_startTimeCard).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showSetEndTimeGuideView()
            }
        })
        builder.addComponent(SetStartTimeComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class SetStartTimeComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_starttime, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_TOP
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 0
        }

        override fun getYOffset(): Int {
            return -10
        }
    }

    private fun showSetEndTimeGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_stopTimeCard).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showAddScheduleDetailGuideView()
            }
        })
        builder.addComponent(AffairDetailActivity.SetEndTimeComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    private fun showAddScheduleDetailGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_addDetail).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showAddScheduleReminderGuideView()
            }
        })
        builder.addComponent(AffairDetailActivity.AddDetailComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    private fun showAddScheduleReminderGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_addReminder).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showScheduleRepeatGuideView()
            }
        })
        builder.addComponent(AffairDetailActivity.AddReminderComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    private fun showScheduleRepeatGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_repeatCard).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showEditWhiteListGuideView()
            }
        })
        builder.addComponent(AffairDetailActivity.AddRepeatComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    private fun showEditWhiteListGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(scheduleDetail_EditWhiteList).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {}
        })
        builder.addComponent(EditWhiteListComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class EditWhiteListComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_edit_whitelist, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_TOP
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 0
        }

        override fun getYOffset(): Int {
            return -10
        }
    }

    override fun onReminderChosen(delta: Long, pos: Int) {
        EventReminderList.ITEMS[pos].delta = delta
        findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter!!.notifyItemChanged(pos)
    }
}