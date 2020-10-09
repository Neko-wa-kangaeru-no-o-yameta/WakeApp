package indi.hitszse2020g6.wakeapp

import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object CourseList{
    lateinit var courseList: MutableList<Course>
    lateinit var DAO:RoomDAO

    var initCompleteCourse = false


    fun getCourseListFromDB(){
        GlobalScope.launch(Dispatchers.IO){
            courseList = MainPageEventList.DAO.getAll().toMutableList()
            initCompleteCourse = true
        }
    }

    fun getCourse(courseId:Int){

    }


    //新添一门课程
    fun addCourse(
        courseName      : String,
        week            : Int,
        dayOfWeek       : Int,
        address         : String,
        time            : Int,
        color           : Int?,
        notice          : Boolean,
        focus           : Boolean,
        mute            : Boolean,
    ){
        val course = Course(
            courseName,week,dayOfWeek,address,time,color,notice,focus,mute
        )
        courseList.add(course)
        GlobalScope.launch(Dispatchers.IO){
            DAO.insertCourse(course)
        }
    }

    //

}