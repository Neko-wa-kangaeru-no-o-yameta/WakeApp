package indi.hitszse2020g6.wakeapp.mainPage

import android.app.AlarmManager
import android.app.PendingIntent
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
import java.util.*
import kotlin.collections.ArrayList

const val PARAM_START_FOCUS_FROM_BACKGROUND = "indi.hitszse2020g6.wakeapp.PARAM_START_FOCUS_FROM_BACKGROUND"

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
            updateStatus()
        }
    }

    fun addAffair(
        title       : String,
        detail      : List<Detail>,
        reminder    : List<Reminder>,
        stopTime    : Long,
        notice      : Boolean,
        isAutoGen   : Boolean,
        repeatAt    : Int
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
            hasDescendant      = false,
            classId     = -1,
            repeatAt    = repeatAt
        )

        eventList.add(entry)        // uid should catch up in milliseconds

        GlobalScope.launch(Dispatchers.IO) {

            entry.uid = DAO.insertEvent(entry)

            Handler(Looper.getMainLooper()).post{
                configureAlarm(entry, FLAG_UPDATE_CURRENT)
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
        classId     : Long,
        repeatAt    : Int
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
            hasDescendant      = false,
            classId     = classId,
            repeatAt    = repeatAt
        )
        eventList.add(entry)        // uid should catch up in milliseconds

        GlobalScope.launch(Dispatchers.IO) {
            entry.uid = DAO.insertEvent(entry)

            Handler(Looper.getMainLooper()).post{
                configureAlarm(entry, FLAG_UPDATE_CURRENT)
                Log.d("Alarm", "trying to set focus...")
                configureFocus(entry, FLAG_UPDATE_CURRENT)
            }
        }
    }

    fun addEvent(entry: EventTableEntry) {
        eventList.add(entry)        // uid should catch up in milliseconds

        GlobalScope.launch(Dispatchers.IO) {
            entry.uid = DAO.insertEvent(entry)

            Handler(Looper.getMainLooper()).post{
                configureAlarm(entry, FLAG_UPDATE_CURRENT)
                if(!entry.isAffair){
                    Log.d("Alarm", "trying to set focus...")
                    configureFocus(entry, FLAG_UPDATE_CURRENT)
                }
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

    fun removeEvent(position: Int): Boolean {
        var flag = false
        val entry = eventList[position]
        if(!entry.hasDescendant && entry.repeatAt != 0) {
            val c = Calendar.getInstance().apply {
                timeInMillis = entry.stopTime * 1000
            }
            for(i in 1 until 8) {
                val weekday = (c.get(Calendar.DAY_OF_WEEK) + i) % 7
                if(((1 shl weekday) and (entry.repeatAt)) != 0) {
                    val clone = entry.clone()
                    if(!clone.isAffair) {
                        clone.startTime = entry.startTime + i * 24 * 60 * 60
                    }
                    clone.stopTime = entry.stopTime + i * 24 * 60 * 60
                    addEvent(clone)
                    flag = true
                    break
                }
            }
        }
        deleteAlarm(entry)
        val uid = entry.uid   // no reference
        GlobalScope.launch(Dispatchers.IO) {
            DAO.deleteEvent(uid)
        }
        eventList.removeAt(position)
        return flag
    }

    fun configureAlarm(entry: EventTableEntry, flag: Int) {
        val currentTimeInSecond = System.currentTimeMillis()/1000 + 1
        Log.d("ALARM", "$currentTimeInSecond, ${entry.reminder.map { it.delta }}")
        val minReminderTime = entry.reminder.map {
            val st = if(entry.isAffair) entry.stopTime else entry.startTime
            if((st - it.delta) < currentTimeInSecond)
                Long.MAX_VALUE
            else
                (st-it.delta)*1000
        }.minOrNull()
        if(minReminderTime != null) {
            Log.d("ALARM", "setting alarm for $minReminderTime")
            val intentToReceiver = Intent(context, AlarmReceiver::class.java).let{
                it.putExtra(PARAM_ALARM_UID, entry.uid)
                PendingIntent.getBroadcast(context, alarmHash(entry.uid), it, flag)
            }
            val intentToActivity = Intent(context, MainActivity::class.java).let {
                PendingIntent.getActivity(context, alarmHash(entry.uid), it, flag)
            }
            val alarmInfo = AlarmManager.AlarmClockInfo(minReminderTime, intentToActivity)
            alarmManager.setAlarmClock(alarmInfo, intentToReceiver)
        } else {
            deleteAlarm(entry)
        }
    }

    fun deleteAlarm(entry: EventTableEntry) {
        Intent(context, AlarmReceiver::class.java).let{
            it.putExtra(PARAM_ALARM_UID, entry.uid)
            PendingIntent.getBroadcast(context, alarmHash(entry.uid), it, 0)
        }?.let{
            alarmManager.cancel(it)
        }
    }

    fun configureFocus(entry: EventTableEntry, flag: Int) {
        if(entry.startTime*1000 > System.currentTimeMillis() && entry.focus) {
            val intentToReceiver = Intent(context, FocusReceiver::class.java).let{
                it.putExtra(PARAM_START_FOCUS_FROM_BACKGROUND, entry.uid)
                PendingIntent.getBroadcast(context, focusHash(entry.uid), it, flag)
            }
            val intentToActivity = Intent(context, MainActivity::class.java).let {
                PendingIntent.getActivity(context, focusHash(entry.uid), it, flag)
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
            it.putExtra(PARAM_START_FOCUS_FROM_BACKGROUND, entry.uid)
            PendingIntent.getBroadcast(context, focusHash(entry.uid), it, 0)
        }?.let{
            alarmManager.cancel(it)
        }
    }

    fun alarmHash(uid: Long): Int {
        return uid.toInt()
    }

    fun focusHash(uid: Long): Int {
        return uid.toInt()
    }

    fun updateStatus() {
        val newEventList = ArrayList<EventTableEntry>()
        for(entry in eventList) {
            if(entry.isAffair && entry.repeatAt != 0 && !entry.hasDescendant) {
                if(System.currentTimeMillis() / 1000 > entry.stopTime) {
                    newEventList.add(entry.clone().apply {
                        val ca = Calendar.getInstance().apply {
                            timeInMillis = entry.stopTime
                        }
                        for(i in 1 until 8) {
                            val weekday = (i + ca.get(Calendar.DAY_OF_WEEK)) % 7
                            if((1 shl weekday) and entry.repeatAt != 0) {
                                stopTime += i * 24 * 60 * 60
                                break
                            }
                        }
                    })
                    entry.hasDescendant = true
                }
            } else if(!entry.isAffair && entry.repeatAt != 0 && !entry.hasDescendant) {
                if(System.currentTimeMillis() / 1000 > entry.startTime) {
                    newEventList.add(entry.clone().apply {
                        val ca = Calendar.getInstance().apply {
                            timeInMillis = entry.startTime
                        }
                        for(i in 1 until 8) {
                            val weekday = (i + ca.get(Calendar.DAY_OF_WEEK)) % 7
                            if((1 shl weekday) and entry.repeatAt != 0) {
                                startTime += i * 24 * 60 * 60
                                stopTime += i * 24 * 60 * 60
                                break
                            }
                        }
                    })
                    entry.hasDescendant = true
                }
            }
        }
        for(newEntry in newEventList) {
            addEvent(newEntry)
        }
        for(entry in eventList) {
            if(!entry.isAffair){
                configureFocus(entry, FLAG_UPDATE_CURRENT)
            }
            configureAlarm(entry, FLAG_UPDATE_CURRENT)
        }
    }
}