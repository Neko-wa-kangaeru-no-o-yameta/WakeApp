package indi.hitszse2020g6.wakeapp

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_choose_schedule_time.*
import java.util.*


class ChooseScheduleTime : AppCompatActivity() {
    private lateinit var dateStartPickerDialog:DatePickerDialog
    private lateinit var dateEndPickerDialog:DatePickerDialog
    private var startYear:Int = 2020
    private var startMonth:Int = 0
    private var startDay:Int = 1
    private var endYear:Int = 2020
    private var endMonth:Int = 11
    private var endDay:Int = 31
    private var startTime:Long = System.currentTimeMillis()
    private var endTime:Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_choose_schedule_time)

        var mySharedPreferences:SharedPreferences = getSharedPreferences(
            "schedule_time",
            Context.MODE_PRIVATE
        )
        if(mySharedPreferences.getLong("startTime", -1).toInt() !=-1){
            val calendar = Calendar.getInstance()
            calendar.time = Date(mySharedPreferences.getLong("startTime", -1))
            startYear = calendar.get(Calendar.YEAR)
            startMonth = calendar.get(Calendar.MONTH)
            startDay = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.time = Date(mySharedPreferences.getLong("endTime", -1))
            endYear = calendar.get(Calendar.YEAR)
            endMonth = calendar.get(Calendar.MONTH)
            endDay = calendar.get(Calendar.DAY_OF_MONTH)
            schedule_start_time.text = "${startYear}-${startMonth+1}-${startDay}"
            schedule_end_time.text = "${endYear}-${endMonth+1}-${endDay}"
        }

        schedule_cancel.setOnClickListener {
            finish()
        }

        schedule_confirm.setOnClickListener {
            val mySharedPreferences:SharedPreferences = getSharedPreferences(
                "schedule_time",
                Context.MODE_PRIVATE
            )
            val editor = mySharedPreferences.edit()
            startTime = getTimeInMills(startYear,startMonth,startDay)
            endTime = getTimeInMills(endYear,endMonth,endDay)
            Log.d("ChooseScheduleTime",startTime.toString())
            Log.d("ChooseScheduleTime",endTime.toString())
            //判一下
            if(endTime<=startTime){
                Toast.makeText(this,"学期结束时间不能早于学期开始时间!",Toast.LENGTH_SHORT).show()
            }else{
                editor.putLong("startTime",startTime)
                editor.putLong("endTime",endTime)
                editor.apply()
                finish()
            }
        }

        dateStartPickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->
                startYear = year
                startMonth = month
                startDay = dayOfMonth
                val data = month.toString() + "月-" + dayOfMonth + "日 "
            },
            startYear, startMonth, startDay
        )

        dateStartPickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            schedule_start_time.text = "${year}-${month+1}-${dayOfMonth}"
            startYear = year
            startMonth = month
            startDay = dayOfMonth
        }

        dateEndPickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->
                endYear = year
                endMonth = month
                endDay = dayOfMonth
                val data = month.toString() + "月-" + dayOfMonth + "日 "
            },
            endYear, endMonth, endDay
        )

        dateEndPickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            schedule_end_time.text = "${year}-${month+1}-${dayOfMonth}"
            endYear = year
            endMonth = month
            endDay = dayOfMonth
        }

        edit_start_time.setOnClickListener {
            dateStartPickerDialog.show()
        }

        edit_end_time.setOnClickListener {
            dateEndPickerDialog.show()
        }
    }

    private fun getTimeInMills(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        Log.d("ChooseScheduleTime", "get Time in millis: y:m:d = $year, $month, $day, ${calendar.timeInMillis}")
        return calendar.timeInMillis
    }
}