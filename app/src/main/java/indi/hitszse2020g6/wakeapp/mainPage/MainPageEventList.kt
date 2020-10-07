package indi.hitszse2020g6.wakeapp.mainPage

import android.util.Log
import indi.hitszse2020g6.wakeapp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object MainPageEventList {
    lateinit var eventList: MutableList<EventTableEntry>
    lateinit var DAO:RoomDAO
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
            entry.uid = DAO.insertCourse(entry)
        }
    }

    fun updateEvent(
        entry       : EventTableEntry
    ) {
        eventList.replaceAll {
            if(it.uid == entry.uid) entry else it
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
            entry.uid = DAO.insertCourse(entry)
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
                DAO.update(event)
            }
        }
    }

    fun removeEvent(position: Int) {
        Log.d("MainPageEventList", "Removing $position")
        val uid = eventList[position].uid   // no reference
        GlobalScope.launch(Dispatchers.IO) {
            DAO.delete(uid)
        }
        eventList.removeAt(position)
    }
}