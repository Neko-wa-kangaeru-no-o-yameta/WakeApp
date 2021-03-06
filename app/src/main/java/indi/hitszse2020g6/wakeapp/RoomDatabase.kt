package indi.hitszse2020g6.wakeapp

import android.content.Context
import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val MONDAY    : Int = 1 shl 0
const val TUESDAY   : Int = 1 shl 1
const val WEDNESDAY : Int = 1 shl 2
const val THURSDAY  : Int = 1 shl 3
const val FRIDAY    : Int = 1 shl 4
const val SATURDAY  : Int = 1 shl 5
const val SUNDAY    : Int = 1 shl 6

@Serializable
data class Detail (
    var title: String,
    var content: String
)

@Serializable
data class Reminder (
    var delta: Long,     // UNIX style
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
    var hasDescendant       : Boolean,          // generated from...
    var classId             : Long,             // generated from...
    var repeatAt            : Int               // repeat
){
    @Ignore fun clone(): EventTableEntry {
        return EventTableEntry(
            uid                ,
            ///////////////////,
            title              ,
            detail             ,
            reminder           ,
            isAffair           ,
            startTime          ,
            stopTime           ,
            priority           ,
            focus              ,
            mute               ,
            notice             ,
            hasCustomWhiteList ,
            customWhiteList    ,
            isAutoGen          ,
            isClass            ,
            hasDescendant      ,
            classId            ,
            repeatAt
        )
    }
}

@Entity(tableName = "course_table")
class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "course_id")         var courseId: Long = 0,
    @ColumnInfo(name = "course_name")       var courseName: String,
    @ColumnInfo(name = "week")              var week: Int,
    @ColumnInfo(name = "day_of_week")       var dayOfWeek: Int,
    @ColumnInfo(name = "class_address")     var address: String,
    @ColumnInfo(name = "class_time")        var time: Int,
    @ColumnInfo(name = "course_color")      var color: Int?,
    @ColumnInfo(name = "course_notice")     var notice: Boolean,
    @ColumnInfo(name = "course_focus")      var focus: Boolean,
    @ColumnInfo(name = "course_mute")       var mute: Boolean,
    @ColumnInfo(name = "course_detail")     var detail: List<Detail>,
    @ColumnInfo(name = "course_reminder")     var reminder: List<Reminder>,
    @ColumnInfo(name = "isAllreadyGenerated")     var isGenerate:Boolean
)


@Database(entities = [EventTableEntry::class, Course::class,MyFocusEntry::class], version = 1, exportSchema = false)
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

    //SceduleTable
    @Query("SELECT * FROM course_table WHERE course_id = (:courseId)")
    fun getCourseById(courseId: Long): List<Course>

    @Query("SELECT * FROM course_table")
    fun getAll(): List<Course>

    @Query("SELECT * FROM course_table WHERE course_name IN (:course_name)")
    fun loadAllCourseNames(course_name: String): List<Course>

    @Query("SELECT * FROM course_table WHERE (week = (:week) and day_of_week = (:dayOfWeek) and class_time =(:time))")
    fun selectCourseByTime(
        week: Int,
        dayOfWeek: Int,
        time: Int
    ):List<Course>

    @Query("SELECT * FROM course_table WHERE week = :weekNo order by class_time, day_of_week")
    fun findWeekCourse(weekNo: Int): List<Course>

    @Query("SELECT MAX(CAST(week AS INT)) FROM course_table")
    fun getMaxWeek(): Int

    @Query("SELECT DISTINCT course_name FROM course_table")
    fun getAllCourse(): List<String>

    @Transaction
    fun importClass(courseList: List<Course>) :List<Long> {
        deleteAllCourse()
        return insertCourses(courseList)
    }


    @Query("DELETE FROM course_table")
    fun deleteAllCourse()

    @Insert
    fun insertCourses(courseList: List<Course>) :   List<Long>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse( course: Course) :Long

    @Query("UPDATE course_table SET course_color = (:course_color) WHERE course_name = (:course_name)")
    fun insertCourseColorIntoTable(course_color: Int, course_name: String)
    @Query("UPDATE course_table SET isAllreadyGenerated = (:generate) WHERE course_id = (:uid)")
    fun updateCourseIsGeneratedFlag(
        uid:Long,
        generate:Boolean
    )
    //通过id进行更新
    @Query("UPDATE course_table SET course_name = (:name),class_address = (:address),course_notice = (:notice),course_focus = (:focus), course_mute = (:mute),course_detail=(:detail),course_reminder = (:reminder) WHERE course_id = (:course_id)")
    fun updateCourseDetailById(
        name: String,
        address: String,
        notice: Boolean,
        focus: Boolean,
        mute: Boolean,
        detail: List<Detail>,
        reminder: List<Reminder>,
        course_id: Long
    )

    //修改这一时间段的courseDetails
    @Query("UPDATE course_table SET course_name = (:name),class_address = (:address),course_notice = (:notice),course_focus = (:focus), course_mute = (:mute),course_detail=(:detail),course_reminder = (:reminder) WHERE (class_time = (:time) and course_name = (:oldName) and day_of_week = (:TheDayOfWeek))")
    fun updateCourseDetailByTime(
        name: String,
        address: String,
        notice: Boolean,
        focus: Boolean,
        mute: Boolean,
        detail: List<Detail>,
        reminder: List<Reminder>,
        time :Int,
        oldName :String,
        TheDayOfWeek: Int
    )

    //修改这门课所有时间的CourseDetails
    @Query("UPDATE course_table SET course_name = (:name),class_address = (:address),course_notice = (:notice),course_focus = (:focus), course_mute = (:mute),course_detail=(:detail),course_reminder = (:reminder) WHERE  course_name = (:oldName)")
    fun updateCourseDetailByName(
        name: String,
        address: String,
        notice: Boolean,
        focus: Boolean,
        mute: Boolean,
        detail: List<Detail>,
        reminder: List<Reminder>,
        oldName: String
    )

    @Query("DELETE FROM course_table WHERE course_id = (:course_id)")
    fun deleteCourseById(course_id: Long)

    @Query("UPDATE course_table SET course_name = (:course_name),class_address = (:course_address),week = (:course_week) ,day_of_week = (:course_dayOFWeek),class_time = (:course_time),course_notice = :alarm,course_focus = :focus ,course_mute = :mute WHERE course_id = (:course_id)")
    fun updateCourseDetails(
        course_name: String,
        course_address: String,
        course_week : Int,
        course_dayOFWeek :Int,
        course_time :Int,
        alarm: Boolean,
        focus: Boolean,
        mute: Boolean,
        course_id: Long
    )


    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCourse(course: Course)

    @Delete
    fun deleteCourse(vararg course: Course)

    @Query("SELECT * FROM focusTable WHERE focusDate>=:d ORDER BY focusDate ASC")
    fun findFocusData(d: Long): List<MyFocusEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFocusData(mf: MyFocusEntry)

    @Query("SELECT * FROM focusTable WHERE (focusDate in (SELECT MAX(focusDate) FROM focusTable))")
    fun getNearestFocusData():List<MyFocusEntry>

    @Query("UPDATE focusTable SET isCanceled = (:set_boolean) WHERE uid = (:before_id)")
    fun updateFocusData(
        before_id:Long,
        set_boolean:Boolean
    )
}