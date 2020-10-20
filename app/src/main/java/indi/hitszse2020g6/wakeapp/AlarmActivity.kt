package indi.hitszse2020g6.wakeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlin.math.abs

class AlarmActivity : AppCompatActivity() {
    lateinit var entry: EventTableEntry
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
//            setTurnScreenOn(true)
//        } else {
//            window.addFlags(
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//            )
//        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        Log.d("Alarm activity", "Waking up...")

        for(e in MainPageEventList.eventList) {
            if (e.uid == intent.getLongExtra(PARAM_ALARM_UID, -1)) {
                entry = e
            }
        }

        val reminder = entry.reminder.minByOrNull {
            abs(System.currentTimeMillis()/100 - it.delta)
        }

        findViewById<TextView>(R.id.alarm_activity_event_title).text = "${entry.title}:"
        findViewById<TextView>(R.id.alarm_activity_alarm_title).text = reminder!!.description

        if(!reminder.notification) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        val mp = MediaPlayer()
        if(reminder.ring) {
             mp.apply {
                setDataSource(this@AlarmActivity, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                prepare()
                start()
                isLooping = true
            }
        }
        val vb = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if(reminder.vibration) {
            vb.vibrate(VibrationEffect.createWaveform(longArrayOf(500, 500), intArrayOf(127, 255), 1))
        }

        findViewById<Button>(R.id.alarm_returnToMain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            mp.stop()
            vb.cancel()
        }
    }
}