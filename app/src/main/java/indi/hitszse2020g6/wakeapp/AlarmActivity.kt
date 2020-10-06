package indi.hitszse2020g6.wakeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        findViewById<Button>(R.id.alarm_returnToMain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val intentFilter = IntentFilter().apply { addAction(ACTION_START_ALARM) }
        registerReceiver(object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("Alarm", "ALARRRRRRRRRRRRRRRRM")
            }
        }, intentFilter)
    }
}