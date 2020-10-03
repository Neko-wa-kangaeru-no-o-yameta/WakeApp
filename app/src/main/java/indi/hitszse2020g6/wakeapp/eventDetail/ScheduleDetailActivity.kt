package indi.hitszse2020g6.wakeapp.eventDetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import indi.hitszse2020g6.wakeapp.R

const val UNIQUE_ID_TO_SCHEDULE_DETAIL = "indi.hitszse2020g6.wakeapp.UNIQUE_ID_FOR_MAIN_TO_SCHEDULE_DETAIL"

class ScheduleDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_detail)
    }
}