package indi.hitszse2020g6.wakeapp.eventDetail

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import indi.hitszse2020g6.wakeapp.*
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import java.util.*

const val UNIQUE_ID_TO_AFFAIR_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_ID_FOR_MAIN_TO_AFFAIR_DETAIL"

class AffairDetailActivity : AppCompatActivity() {

    private var isNewAffair = true
    private lateinit var entryToEdit: EventTableEntry

    companion object{
        private var c: Calendar = Calendar.getInstance()
        var year    : Int = c.get(Calendar.YEAR)
        var month   : Int = c.get(Calendar.MONTH)
        var date    : Int = c.get(Calendar.DAY_OF_MONTH)
        var hour    : Int = c.get(Calendar.HOUR_OF_DAY)
        var minute  : Int = c.get(Calendar.MINUTE)
        var alarm   : Boolean = true
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    EventDetailList.ITEMS = entryToEdit.detail.toMutableList()
                    EventReminderList.ITEMS = entryToEdit.reminder.toMutableList()
                    findViewById<EditText>(R.id.affairDetail_eventTitle).setText(entryToEdit.title)
                    Log.d("AffairDetailActivity ", "detail and reminder Loaded: ${entryToEdit.detail} & ${entryToEdit.detail}")
                    break
                }
            }

        }

        findViewById<ImageButton>(R.id.affairDetail_confirm).setOnClickListener {
            val stopTime = Calendar.getInstance()
            stopTime.set(year, month, date, hour, minute)

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
                    title       = findViewById<EditText>(R.id.affairDetail_eventTitle).text.toString(),
                    detail      = EventDetailList.ITEMS.toList(),
                    reminder    = EventReminderList.ITEMS.toList(),
                    stopTime    = stopTime.timeInMillis / 1000,
                    notice      = alarm,
                    isAutoGen   = false,
                    ruleId      = -1
                )
            } else {
                entryToEdit.title       = findViewById<EditText>(R.id.affairDetail_eventTitle).text.toString()
                entryToEdit.detail      = EventDetailList.ITEMS.toList()
                entryToEdit.reminder    = EventReminderList.ITEMS.toList()
                entryToEdit.stopTime    = stopTime.timeInMillis / 1000
                entryToEdit.notice      = alarm
                entryToEdit.isAutoGen   = false
                entryToEdit.ruleId      = -1
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
                    ring = false,
                    vibration = false,
                    notification = false,
                    ""
                ))
                findViewById<RecyclerView>(R.id.eventDetail_reminderListContainer).adapter?.notifyItemInserted(EventReminderList.ITEMS.size)
                findViewById<ScrollView>(R.id.affairDetail_mainContainer).apply { post {
                    fullScroll(ScrollView.FOCUS_DOWN)
                } }
            }
        }

        findViewById<CardView>(R.id.affairDetail_stopTimeCard).apply{
            if(!isNewAffair) {
                findViewById<TextView>(R.id.affairDetail_stopTimeText).text = "%d/%d/%d, %d:%d".format(year, month, date, hour, minute)
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

    class DatePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return DatePickerDialog(activity as Context,this, year, month, date)
        }

        override fun onDateSet(view: DatePicker?, setYear: Int, setMonth: Int, setDayOfMonth: Int) {
            year = setYear
            month = setMonth + 1
            date = setDayOfMonth
            Log.d("OnTimeSet", "$year : $month : $date ")
            activity?.supportFragmentManager?.let { TimePickerFragment().show(it, "timePicker") }
        }
    }


    class TimePickerFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return TimePickerDialog(activity, this, hour, minute, true)
        }

        override fun onTimeSet(view: TimePicker?, setHourOfDay: Int, setMinute: Int) {
            hour = setHourOfDay
            minute = setMinute
            Log.d("OnTimeSet", "$hour : $minute")
            activity?.findViewById<TextView>(R.id.affairDetail_stopTimeText)?.text = "%d/%d/%d, %d:%d".format(year, month, date, hour, minute)
        }
    }
}
