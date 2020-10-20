package indi.hitszse2020g6.wakeapp.eventDetail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
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
import kotlinx.android.synthetic.main.activity_affair_detail.*
import java.util.*

const val UNIQUE_ID_TO_AFFAIR_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_ID_FOR_MAIN_TO_AFFAIR_DETAIL"

class AffairDetailActivity :
    AppCompatActivity(),
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    RepeatTypeDialog.RepeatTypeListener,
    RepeatWeekdayDialog.RepeatWeekDayDialogListener,
    ReminderChooseDialog.ReminderChooseListener{

    private var isNewAffair = true
    private lateinit var entryToEdit: EventTableEntry

    private var c: Calendar = Calendar.getInstance()
    var year    : Int = c.get(Calendar.YEAR)
    var month   : Int = c.get(Calendar.MONTH)
    var date    : Int = c.get(Calendar.DAY_OF_MONTH)
    var hour    : Int = c.get(Calendar.HOUR_OF_DAY)
    var minute  : Int = c.get(Calendar.MINUTE)
    var alarm   : Boolean = true

    var repeatAt: Int = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_affair_detail)
        setSupportActionBar(findViewById(R.id.affairDetail_actionBar))

        EventDetailList.ITEMS.clear()
        EventReminderList.ITEMS.clear()

        if(intent.extras != null) {
            isNewAffair = false
            val uid = intent.getLongExtra(UNIQUE_ID_TO_AFFAIR_DETAIL, -1)
            for(entry in MainPageEventList.eventList) {
                if(entry.uid == uid) {
                    entryToEdit = entry
                    alarm = entryToEdit.notice
                    c = Calendar.getInstance().apply {
                        timeInMillis = entryToEdit.stopTime * 1000
                    }
                    year   = c.get(Calendar.YEAR)
                    month  = c.get(Calendar.MONTH)
                    date   = c.get(Calendar.DAY_OF_MONTH)
                    hour   = c.get(Calendar.HOUR_OF_DAY)
                    minute = c.get(Calendar.MINUTE)
                    alarm  = entryToEdit.notice
                    repeatAt = entryToEdit.repeatAt
                    EventDetailList.ITEMS = entryToEdit.detail.toMutableList()
                    EventReminderList.ITEMS = entryToEdit.reminder.toMutableList()
                    findViewById<EditText>(R.id.affairDetail_eventTitle).setText(entryToEdit.title)
                    Log.d("AffairDetailActivity ", "detail and reminder Loaded: ${entryToEdit.detail} & ${entryToEdit.detail}")
                    break
                }
            }

        }

        setRepeatList()

        findViewById<ImageButton>(R.id.affairDetail_confirm).setOnClickListener {

            val title= findViewById<EditText>(R.id.affairDetail_eventTitle).text.toString()
            if(title == ""){
                Toast.makeText(this, "标题尚未设置", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stopTime = Calendar.getInstance()
            stopTime.set(year, month-1, date, hour, minute, 0)

//            val descHolder = findViewById<RecyclerView>(R.id.eventDetail_descriptionListContainer)
//            for(i in 0 until EventDetailList.ITEMS.size) {
//                val holder = descHolder.findViewHolderForLayoutPosition(i) as MyDescriptionRecyclerViewAdapter.ViewHolder
//                EventDetailList.ITEMS[i].title = holder.titleView.text.toString()
//                EventDetailList.ITEMS[i].content = holder.contentView.text.toString()
//            }
//
//            val reminderHolder = findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer)
//            for(i in 0 until EventReminderList.ITEMS.size) {
//                val holder = reminderHolder.findViewHolderForLayoutPosition(i) as MyReminderRecyclerViewAdapter.ViewHolder
//                EventReminderList.ITEMS[i].description = holder.cardView.findViewById<EditText>(R.id.eventDetail_reminderListItem_detailContent).text.toString()
//            }

            if(isNewAffair) {
                MainPageEventList.addAffair(
                    title       = title,
                    detail      = EventDetailList.ITEMS.toList(),
                    reminder    = EventReminderList.ITEMS.toList(),
                    stopTime    = stopTime.timeInMillis / 1000,
                    notice      = alarm,
                    isAutoGen   = false,
                    repeatAt
                )
            } else {
                entryToEdit.title       = title
                entryToEdit.detail      = EventDetailList.ITEMS.toList()
                entryToEdit.reminder    = EventReminderList.ITEMS.toList()
                entryToEdit.stopTime    = stopTime.timeInMillis / 1000
                entryToEdit.notice      = alarm
                entryToEdit.isAutoGen   = false
                entryToEdit.repeatAt    = repeatAt
                MainPageEventList.updateEvent(entryToEdit)
            }
            finish()
        }

        findViewById<ImageButton>(R.id.affairDetail_cancel).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.affairDetail_addDetail).setOnClickListener {
            if(EventDetailList.ITEMS.size < 10) {
                EventDetailList.ITEMS.add(Detail("", ""))
                findViewById<RecyclerView>(R.id.eventDetail_descriptionListContainer).adapter?.notifyItemInserted(EventDetailList.ITEMS.size)
//                if(EventDetailList.ITEMS.size == 10) {
//                    disableAddButton(it as ImageButton)
//                }
            }
        }

        findViewById<ImageButton>(R.id.affairDetail_addReminder).setOnClickListener {
            if(EventReminderList.ITEMS.size < 10) {
                EventReminderList.ITEMS.add(Reminder(
                    0,
                    ring = true,
                    vibration = true,
                    notification = true,
                    ""
                ))
                findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter?.notifyItemInserted(EventReminderList.ITEMS.size)
                findViewById<NestedScrollView>(R.id.affairDetail_mainContainer).apply { post {
                    fullScroll(NestedScrollView.FOCUS_DOWN)
                } }
            }
        }

        findViewById<CardView>(R.id.affairDetail_stopTimeCard).apply{
            if(!isNewAffair) {
                findViewById<TextView>(R.id.affairDetail_stopTimeText).text =  context.getString(
                    R.string.eventList_stopTimeTVContent
                ).format(month, date, hour, minute)
            }
            setOnClickListener {
                DatePickerFragment().show(supportFragmentManager, "dataPicker")
            }
        }


        findViewById<ImageButton>(R.id.affairDetail_alarm).apply {
            toggleImageDrawable(this, alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            setOnClickListener {
                alarm = !alarm
                toggleImageDrawable(this, alarm, R.drawable.alarm_on_24, R.drawable.alarm_off_24)
            }
        }

        findViewById<CardView>(R.id.affairDetail_repeatCard).setOnClickListener {
            RepeatTypeDialog().show(supportFragmentManager, "RepeatTypeDialog")
        }

        val mySharedPreferences = getSharedPreferences("new_user", Context.MODE_PRIVATE)
        if (mySharedPreferences.getBoolean("isNewAddAffairFragment", true)) {
            affairDetail_eventTitle.post { showTitleGuideView() }
            val editor = mySharedPreferences.edit()
            editor.putBoolean("isNewAddAffairFragment", false)
            editor.apply()
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

    private fun disableAddButton(view: ImageButton) {
        view.isEnabled = false
        view.setImageDrawable(view.drawable.mutate().apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                colorFilter = BlendModeColorFilter(Color.GRAY, BlendMode.DST_IN)
            }
        })
    }

    fun enableAddButton(view: ImageButton) {
        view.isEnabled = true
        view.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.add_circle_outline_24))
    }

    override fun onDateSet(view: DatePicker?, setYear: Int, setMonth: Int, setDayOfMonth: Int) {
        year = setYear
        month = setMonth + 1
        date = setDayOfMonth
        Log.d("OnTimeSet", "$year : $month : $date ")
        supportFragmentManager.let { TimePickerFragment().show(it, "timePicker") }
    }

    override fun onTimeSet(view: TimePicker?, setHourOfDay: Int, setMinute: Int) {
        hour = setHourOfDay
        minute = setMinute
        Log.d("OnTimeSet", "$hour : $minute")
        findViewById<TextView>(R.id.affairDetail_stopTimeText).text = getString(
            R.string.eventList_stopTimeTVContent
        ).format(month, date, hour, minute)
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

    fun setRepeatList() {
        val repeatList = findViewById<LinearLayout>(R.id.affairDetail_repeatContent)
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

    private fun showTitleGuideView() {
        val builder = GuideBuilder()
        builder.setTargetView(affairDetail_eventTitle).setAlpha(150).setHighTargetPadding(10)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showAffairAlarmGuideView()
            }
        })
        builder.addComponent(TitleComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class TitleComponent : Component {
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_affair_title, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_RIGHT
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 20
        }

        override fun getYOffset(): Int {
            return -20
        }

    }

    private fun showAffairAlarmGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(affairDetail_alarm).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showSetTimeGuideView()
            }
        })
        builder.addComponent(AlarmComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class AlarmComponent:Component {
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_alarm, null) as LinearLayout
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

    private fun showSetTimeGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(affairDetail_stopTimeCard).setAlpha(150).setHighTargetPadding(10)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showAddAffairDetailGuideView()
            }
        })
        builder.addComponent(SetEndTimeComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }


    override fun onReminderChosen(delta: Long, pos: Int) {
        EventReminderList.ITEMS[pos].delta = delta
        findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter!!.notifyItemChanged(pos)
    }

    class SetEndTimeComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_set_endtime, null) as LinearLayout
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

    private fun showAddAffairDetailGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(affairDetail_addDetail).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showAddAffairReminderGuideView()
            }
        })
        builder.addComponent(AddDetailComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class AddDetailComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_add_detail, null) as LinearLayout
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

    private fun showAddAffairReminderGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(affairDetail_addReminder).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showAffairRepeatGuideView()
            }
        })
        builder.addComponent(AddReminderComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class AddReminderComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_add_reminder, null) as LinearLayout
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

    private fun showAffairRepeatGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(affairDetail_repeatCard).setAlpha(150).setHighTargetPadding(5)
            .setHighTargetGraphStyle(Component.ROUNDRECT)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {}
        })
        builder.addComponent(AddRepeatComponent())
        val guide = builder.createGuide()
        guide.show(this)
    }

    class AddRepeatComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_add_repeat, null) as LinearLayout
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
}
