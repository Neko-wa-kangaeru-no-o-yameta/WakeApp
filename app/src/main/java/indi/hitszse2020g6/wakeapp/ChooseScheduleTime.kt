package indi.hitszse2020g6.wakeapp

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_choose_schedule_time.*
import java.util.*


class ChooseScheduleTime : AppCompatActivity() {
    private lateinit var dateStartPickerDialog:DatePickerDialog
    private lateinit var dateEndPickerDialog:DatePickerDialog
    private var startYear:Int = 2020
    private var startMonth:Int = 1
    private var startDay:Int = 1
    private var endYear:Int = 2020
    private var endMonth:Int = 12
    private var endDay:Int = 31

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_choose_schedule_time)

        var mySharedPreferences:SharedPreferences = getSharedPreferences("schedule_start_time",
            Context.MODE_PRIVATE)
        if(mySharedPreferences.getInt("startYear",-1)!=-1){
            startYear = mySharedPreferences.getInt("startYear",-1)
            startMonth = mySharedPreferences.getInt("startMonth",-1)
            startDay = mySharedPreferences.getInt("startDay",-1)
            endYear = mySharedPreferences.getInt("endYear",-1)
            endMonth = mySharedPreferences.getInt("endMonth",-1)
            endDay = mySharedPreferences.getInt("endDay",-1)
            schedule_start_time.text = "${startYear}-${startMonth}-${startDay}"
            schedule_end_time.text = "${endYear}-${endMonth}-${endDay}"
        }

        schedule_cancel.setOnClickListener {
            finish()
        }

        schedule_confirm.setOnClickListener {
            var mySharedPreferences:SharedPreferences = getSharedPreferences("schedule_start_time",
                Context.MODE_PRIVATE)
            var editor = mySharedPreferences.edit()
            editor.putInt("startYear",startYear)
            editor.putInt("startMonth",startMonth)
            editor.putInt("startDay",startDay)
            editor.putInt("endYear",endYear)
            editor.putInt("endMonth",endMonth)
            editor.putInt("endDay",endDay)
            editor.commit()
            finish()
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
            startMonth = month+1
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
            endMonth = month+1
            endDay = dayOfMonth
        }

        edit_start_time.setOnClickListener {
            dateStartPickerDialog.show()
        }

        edit_end_time.setOnClickListener {
            dateEndPickerDialog.show()
        }
    }
}