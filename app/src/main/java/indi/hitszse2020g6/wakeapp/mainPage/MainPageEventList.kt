package indi.hitszse2020g6.wakeapp.mainPage

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import indi.hitszse2020g6.wakeapp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs

object MainPageEventList {
    lateinit var eventList: MutableList<EventTableEntry>
    lateinit var DAO:RoomDAO
    lateinit var context: Context
    lateinit var alarmManager: AlarmManager
    var initComplete = false

    fun getEventListFromDB() {
        GlobalScope.launch(Dispatchers.IO) {
            eventList = DAO.getEvents().toMutableList()
            initComplete = true
        }
    }

    fun addAffair(
        title       : String,
        detail      : List<Detail>,
        reminder    : List<Reminder>,
        stopTime    : Long,
        notice      : Boolean,
        isAutoGen   : Boolean,
        ruleId      : Long
    ) {
        val entry = EventTableEntry(
            uid         = 0,    // auto gen
            title       = title,
            detail      = detail.toList(),
            reminder    = reminder.toList(),
            isAffair    = true,
            startTime   = 0,
            stopTime    = stopTime,
            priority    = if (eventList.isNotEmpty()) (eventList.maxOf { event -> event.priority } + 1) else 0,
            focus       = false,
            mute        = false,
            notice      = notice,
            hasCustomWhiteList = false,
            customWhiteList = ArrayList<String>(),
            isAutoGen   = isAutoGen,
            isClass     = false,
            ruleId      = ruleId,
            classId     = -1
        )

        eventList.add(entry)        // uid should catch up in milliseconds

        GlobalScope.launch(Dispatchers.IO) {

            entry.uid = DAO.insertEvent(entry)

            Handler(Looper.getMainLooper()).post{
                configureAlarm(entry, 0)
            }
        }
    }

    fun updateEvent(
        entry       : EventTableEntry
    ) {
        eventList.replaceAll {
            if(it.uid == entry.uid) entry else it
        }

        configureAlarm(entry, FLAG_UPDATE_CURRENT)
        if(!entry.isAffair){
            configureFocus(entry, FLAG_UPDATE_CURRENT)
        }
    }

    fun addSchedule(
        title       : String,
        detail      : List<Detail>,
        reminder    : List<Reminder>,
        startTime   : Long,
        stopTime    : Long,
        focus       : Boolean,
        mute        : Boolean,
        notice      : Boolean,
        hasWL       : Boolean,
        whiteList   : List<String>,
        isAutoGen   : Boolean,
        isClass     : Boolean,
        ruleId      : Long,
        classId     : Long
    ) {
        val entry = EventTableEntry(
            uid         = 0,    // auto gen
            title       = title,
            detail      = detail.toList(),
            reminder    = reminder.toList(),
            isAffair    = false,
            startTime   = startTime,
            stopTime    = stopTime,
            priority    = if (eventList.isNotEmpty()) (eventList.maxOf { event -> event.priority } + 1) else 0,
            focus       = focus,
            mute        = mute,
            notice      = notice,
            hasCustomWhiteList = hasWL,
            customWhiteList = whiteList.toList(),
            isAutoGen   = isAutoGen,
            isClass     = isClass,
            ruleId      = ruleId,
            classId     = classId
        )
        eventList.add(entry)        // uid should catch up in milliseconds

        GlobalScope.launch(Dispatchers.IO) {
            entry.uid = DAO.insertEvent(entry)

            Handler(Looper.getMainLooper()).post{
                configureAlarm(entry, 0)
                Log.d("Alarm", "trying to set focus...")
                configureFocus(entry, 0)
            }
        }
    }

    fun changePriority(fromPosition: Int, toPosition: Int) {
        Log.d("MainPageEventList", "Priority changed: $fromPosition <> $toPosition")
        if(fromPosition < toPosition) {
            for(i in fromPosition until toPosition) {
                val temp = eventList[i]
                eventList[i] = eventList[i+1]
                eventList[i+1] = temp

                val tempP = eventList[i].priority
                eventList[i].priority = eventList[i+1].priority
                eventList[i+1].priority = tempP
            }
        } else {
            for(i in fromPosition downTo toPosition+1) {
                val temp = eventList[i]
                eventList[i] = eventList[i-1]
                eventList[i-1] = temp

                val tempP = eventList[i].priority
                eventList[i].priority = eventList[i-1].priority
                eventList[i-1].priority = tempP
            }
        }
        for(e in eventList) {
            Log.d("MainPageEventList", "After sort: ${e.title}, priority = ${e.priority}, key = ${e.uid}")
        }
    }

    fun applyModifyToDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            eventList.forEach { event ->
                DAO.updateEvent(event)
            }
        }
    }

    fun removeEvent(position: Int) {
        configureAlarm(eventList[position], FLAG_NO_CREATE)
        val uid = eventList[position].uid   // no reference
        GlobalScope.launch(Dispatchers.IO) {
            DAO.deleteEvent(uid)
        }
        eventList.removeAt(position)

    }

    fun configureAlarm(entry: EventTableEntry, flag: Int) {
        val currentTimeInSecond = System.currentTimeMillis()/1000 + 1
        val minReminderTime = entry.reminder.map { if(it.time < currentTimeInSecond) Long.MAX_VALUE else it.time*1000 }.maxOrNull()
        if(minReminderTime != null) {
            val intentToReceiver = Intent(context, AlarmReceiver::class.java).let{
                PendingIntent.getBroadcast(context, alarmHash(entry.uid), it, 0)
            }
            val intentToActivity = Intent(context, MainActivity::class.java).let {
                PendingIntent.getActivity(context, alarmHash(entry.uid), it, 0)
            }
            val alarmInfo = AlarmManager.AlarmClockInfo(minReminderTime, intentToActivity)
            alarmManager.setAlarmClock(alarmInfo, intentToReceiver)
        } else {
            deleteAlarm(entry)
        }
    }

    fun deleteAlarm(entry: EventTableEntry) {
        Intent(context, AlarmReceiver::class.java).let{
            PendingIntent.getBroadcast(context, alarmHash(entry.uid), it, 0)
        }?.let{
            alarmManager.cancel(it)
        }
    }

    fun configureFocus(entry: EventTableEntry, flag: Int) {
        if(entry.startTime*1000 > System.currentTimeMillis() && entry.focus) {
            val intentToReceiver = Intent(context, FocusReceiver::class.java).let{
                PendingIntent.getBroadcast(context, alarmHash(entry.uid), it, 0)
            }
            val intentToActivity = Intent(context, MainActivity::class.java).let {
                PendingIntent.getActivity(context, alarmHash(entry.uid), it, 0)
            }
            val alarmInfo = AlarmManager.AlarmClockInfo(entry.startTime*1000, intentToActivity)
            alarmManager.setAlarmClock(alarmInfo, intentToReceiver)
            Log.d("Alarm", "set focus at ${entry.startTime*1000}, current ${System.currentTimeMillis()}")
        } else {
            deleteFocus(entry)
        }
    }

    fun deleteFocus(entry: EventTableEntry) {
        Intent(context, FocusReceiver::class.java).let{
            PendingIntent.getBroadcast(context, alarmHash(entry.uid), it, 0)
        }?.let{
            alarmManager.cancel(it)
        }
    }

    fun alarmHash(uid: Long): Int {
        return abs(uid.toInt())
    }

    fun focusHash(uid: Long): Int {
        return abs((Int.MAX_VALUE - uid).toInt())
    }
}