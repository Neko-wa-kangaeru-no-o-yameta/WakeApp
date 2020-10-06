package indi.hitszse2020g6.wakeapp

import android.content.Context
import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    var beforeSysTime       :Long,               //System time when timing starts
    var before_title        :String             //the title of the focus
)

@Entity(tableName = "focusTable")
class MyFocusEntry(
    @PrimaryKey(autoGenerate = true)val uid                 : Long,
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var totalFocusTime      :Long,              //the total time of each focus
    var focusDate           :Long,              //System.currentTimeMillis(),can be changed into date
    var focusTitle          :String,            //the title of foucs
    var isCanceled          :Boolean            //true->cancel  false->not cancel
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


@Entity(tableName = "course_table")
data class Course(
    @ColumnInfo(name = "course_name") val courseName: String,
    @ColumnInfo(name = "week") val week: Int,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "class_address") val address: String,
//    var detail              : List<Detail>,     // detail, Serialized Json
    @ColumnInfo(name = "class_time") val time: Int,
    @ColumnInfo(name = "course_color") val color: Int?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var courseId: Long = 0
}

@Database(entities = [EventTableEntry::class, GenRuleEntry::class,MyTimeEntry::class,Course::class,MyFocusEntry::class], version = 1)
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
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

@Dao
interface RoomDAO {

    @Query("SELECT * FROM EventTable ORDER BY priority ASC")
    fun getEvents(): List<EventTableEntry>

    @Query("SELECT * FROM EventTable WHERE uid=:uid")
    fun getEvent(uid: Long): EventTableEntry

    @Query("SELECT * FROM EventTable WHERE isAffair=1")
    fun getAffairs(): List<EventTableEntry>

    @Query("SELECT * FROM EventTable WHERE isAffair=0")
    fun getSchedule(): List<EventTableEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(re: EventTableEntry) : Long

    @Query("DELETE FROM EventTable")
    fun deleteAllEvents()

    @Query("DELETE FROM EventTable WHERE uid=:uid")
    fun deleteEvent(uid: Long)

    @Update
    fun updateEvent(vararg re: EventTableEntry)

    @Query("SELECT * FROM timeTable WHERE id = 1")
    fun findFromTimeTable():List<MyTimeEntry>

    @Update
    fun updateMyTime(mt:MyTimeEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyTime(mt:MyTimeEntry)

    //SceduleTable
    @Query("SELECT * FROM course_table")
    fun getAll(): List<Course>

    @Query("SELECT * FROM course_table WHERE course_name IN (:course_name)")
    fun loadAllCourseNames(course_name: String): List<Course>

    @Query("SELECT * FROM course_table WHERE week = :weekNo order by class_time, day_of_week")
    fun findWeekCourse(weekNo: Int): List<Course>

    @Query("SELECT MAX(CAST(week AS INT)) FROM course_table")
    fun getMaxWeek(): Int

    @Query("SELECT DISTINCT course_name FROM course_table")
    fun getAllCourse(): List<String>

    @Query("DELETE FROM course_table")
    fun deleteAllCourse()

    @Query("UPDATE course_table SET course_color = (:course_color) WHERE course_name = (:course_name)")
    fun InsertCourseColorIntoTable(course_color:Int,course_name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(vararg course: Course)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCourse(course: Course)

    @Delete
    fun deleteCourse(vararg course: Course)

    @Query("SELECT * FROM focusTable WHERE focusDate>=:d ORDER BY focusDate ASC")
    fun findFocusData(d:Long):List<MyFocusEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFocusData(mf:MyFocusEntry)

}