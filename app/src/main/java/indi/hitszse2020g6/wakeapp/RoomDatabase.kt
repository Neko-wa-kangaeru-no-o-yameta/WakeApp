package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.icu.util.TimeZone
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class Detail (
    var title: String,
    var content: String
)

@Serializable
data class Reminder (
    var time: Long,     // UNIX style
    var ring: Boolean,
    var vibration: Boolean,
    var notification: Boolean,
    var description: String
)

class Converters {
    @TypeConverter
    fun stringToReminderList(value: String?): List<Reminder>? {
        return if(value != null) Json.decodeFromString(value) else null
    }

    @TypeConverter
    fun reminderListToString(value: List<Reminder>?) : String? {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun stringToDetailList(value: String?): List<Detail>? {
        return if(value != null) Json.decodeFromString(value) else null
    }

    @TypeConverter
    fun detailListToString(value: List<Detail>?): String? {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun stringToWhiteList(value: String?): List<String>? {
        return if(value != null) Json.decodeFromString(value) else null
    }

    @TypeConverter
    fun whiteListToString(value: List<String>?): String? {
        return Json.encodeToString(value)
    }
}

@Entity(tableName = "timeTable")
class MyTimeEntry(
    @PrimaryKey(autoGenerate = true)val id                  : Int,
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var totalTime           :Long,              //The total time of the last count setting
    var conditionFlag       :Int,               //1->counting,-1->counting over,0->waiting
    var beforeSysTime       :Long               //System time when timing starts
)

@Entity(tableName = "EventTable")
class EventTableEntry (
    @PrimaryKey(autoGenerate = true)var uid                 : Long,             // unique id
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var title               : String,           // title
    var detail              : List<Detail>,     // detail, Serialized Json
    var reminder            : List<Reminder>,   // reminder, Serialized Json
    var isAffair            : Boolean,          // affair or schedule?
    var startTime           : Long,             // Unix style, not available when isAffair
    var stopTime            : Long,             // Unix style
    var priority            : Long,             // lower = more important
    var focus               : Boolean,          // focus on this event? not available when isAffair
    var mute                : Boolean,          // mute on this event? not available when isAffair
    var notice              : Boolean,          // override reminder notice
    var hasCustomWhiteList  : Boolean,          // has custom white list?
    var customWhiteList     : List<String>,     // Not available when !hasCustomWhiteList
    var isAutoGen           : Boolean,          // is auto generated
    var isClass             : Boolean,          // is class?
    var ruleId              : Long,             // generated from...
    var classId             : Long              // generated from...
)

@Entity(tableName = "GenRuleTable")
class GenRuleEntry (
    @PrimaryKey(autoGenerate = true)
    var uid                 : Long,             // unique id
)

@Database(entities = [EventTableEntry::class, GenRuleEntry::class,MyTimeEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppRoomDB: RoomDatabase() {
    abstract fun getDAO(): RoomDAO

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDB? = null

        fun getDataBase(context: Context): AppRoomDB {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDB::class.java,
                    "AppRoomDB"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

@Dao
interface RoomDAO {

    @Query("SELECT * FROM EventTable ORDER BY priority ASC")
    suspend fun getEvents(): List<EventTableEntry>

    @Query("SELECT * FROM EventTable WHERE uid=:uid")
    suspend fun getEvent(uid: Long): EventTableEntry

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(re: EventTableEntry) : Long

    @Query("DELETE FROM EventTable")
    suspend fun deleteAll()

    @Query("DELETE FROM EventTable WHERE uid=:uid")
    suspend fun delete(uid: Long)

    @Update
    suspend fun update(vararg re: EventTableEntry)

    @Query("SELECT * FROM timeTable WHERE id = 1")
    fun findFromTimeTable():List<MyTimeEntry>

    @Update
    fun updateMyTime(mt:MyTimeEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyTime(mt:MyTimeEntry)

}