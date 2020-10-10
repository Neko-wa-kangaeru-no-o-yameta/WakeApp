package indi.hitszse2020g6.wakeapp

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//宝贝你被废弃了

object CourseList {
    lateinit var courseList: MutableList<Course>
    lateinit var DAO: RoomDAO
    lateinit var context: Context

    var initCompleteCourse = false


    fun getCourseListFromDB() {
        GlobalScope.launch(Dispatchers.IO) {
            courseList = DAO.getAll().toMutableList()
            initCompleteCourse = true
        }
    }

    //将excel导入的数据库批量插入
    fun importCourseToDB(resultList: List<Course>) {
        val idList = DAO.importClass(resultList)
        //将idList更新
        for (ele in idList.indices) {
            courseList[ele].courseId = idList[ele]
        }
    }

    fun updateColorForCourseList(courseColor: Int, courseName: String) {
        DAO.insertCourseColorIntoTable(courseColor, courseName)
        for (ele in courseList) {
            if (ele.courseName == courseName) {
                ele.color = courseColor
            }
        }
    }

    //新添一门课程
    fun addCourse(
        courseName: String,
        week: Int,
        dayOfWeek: Int,
        address: String,
        time: Int,
        color: Int?,
        notice: Boolean,
        focus: Boolean,
        mute: Boolean,
    ) {
        val course = Course(
            0, courseName, week, dayOfWeek, address, time, color, notice, focus, mute
        )
        courseList.add(course)
        GlobalScope.launch(Dispatchers.IO) {
            DAO.insertCourse(course)
        }
    }


}