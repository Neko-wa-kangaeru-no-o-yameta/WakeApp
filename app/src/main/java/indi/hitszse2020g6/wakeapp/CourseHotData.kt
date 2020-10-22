package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.descriptors.PrimitiveKind

object CourseList{
    lateinit var courseList: MutableList<Course>
    lateinit var DAO:RoomDAO
    lateinit var context: Context
    var initComplete = false
    fun getDatefromDB(){
        GlobalScope.launch(Dispatchers.IO){
            courseList = DAO.getAll().toMutableList()
            initComplete = true
        }
    }

    fun deleteAllCourse(){
        //删除所有课程
        //删除热数据
        courseList.clear()
        //删除数据库
        GlobalScope.launch(Dispatchers.IO){
            DAO.deleteAllCourse()
        }
    }

    fun importClassWithoutRepeat(resultList: List<Course>,repeatList : List<Course>){
        //将重复的删掉
        val resultList = resultList.toMutableList()
        courseList.removeAll(repeatList)
        resultList.addAll(courseList)
        courseList.clear()
        courseList = resultList
        GlobalScope.launch(Dispatchers.IO){
            val idList = DAO.importClass(resultList)
            for (i in idList.indices){
                courseList[i].courseId = idList[i]

            }

        }
    }

    fun getAllCourse():List<String>{
        val courseNameList = arrayListOf<String>()
        for(item in courseList){
            if(!courseNameList.contains(item.courseName)){
                courseNameList.add(item.courseName)
            }
        }
        return  courseNameList
    }

    fun insertCourseColorIntoTable(course_color: Int, course_name: String){
        for(item in courseList){
            if(item.courseName == course_name){
                item.color = course_color
            }
        }
        GlobalScope.launch(Dispatchers.IO){
            DAO.insertCourseColorIntoTable(course_color, course_name)
        }
    }

    fun findWeekCourse(week: Int) :List<Course>{
        val courseWeekList = arrayListOf<Course>()
        for(item in courseList){
            if(item.week == week){
                courseWeekList.add(item)
            }
        }
        return courseWeekList
    }

    fun getCourseById(Id: Long) :List<Course>{
        val courseFindById = arrayListOf<Course>()
        for(item in courseList){
            if(item.courseId == Id){
                courseFindById.add(item)
            }
        }
        return courseFindById
    }

      fun selectCourseByTime(week: Int, dayOfWeek: Int, time: Int):List<Course>{
        val courseFindByTime = arrayListOf<Course>()
        for(item in courseList){
            if((item.time == time)&&(item.dayOfWeek == dayOfWeek)&&(item.week == week)){
                courseFindByTime.add(item)
            }
        }
        return courseFindByTime
    }

    fun insertCourse(course: Course){
        courseList.add(course)
        GlobalScope.launch(Dispatchers.IO){
            courseList.last().courseId =  DAO.insertCourse(course)
            Log.d("insetCourseId",courseList.last().courseId.toString())
        }
    }

    fun deleteCourseById(courseId: Long){
        var pos = -1
        for(i in 0 until courseList.size){
            if(courseList[i].courseId == courseId){
                pos = i
            }
        }

        courseList.removeAt(pos)
        GlobalScope.launch(Dispatchers.IO){
            DAO.deleteCourseById(courseId)
        }

//        DAO.deleteCourseById(courseId)
//        courseList.clear()
//        courseList = DAO.getAll().toMutableList()
    }


    fun updateCourseDetailById(
        name: String, address: String, notice: Boolean,
        focus: Boolean, mute: Boolean,
        detail: List<Detail>,reminder: List<Reminder>, course_id: Long
    ){
        for(item in courseList){
            if(item.courseId == course_id){
                item.courseName = name
                item.address = address
                item.notice = notice
                item.focus = focus
                item.mute = mute
                item.detail = detail
                item.reminder = reminder
            }
        }
        GlobalScope.launch(Dispatchers.IO){
            DAO.updateCourseDetailById(name, address, notice, focus, mute, detail,reminder, course_id)
        }
    }

    fun updateCourseDetailByTime(
        name: String,
        address: String,
        notice: Boolean,
        focus: Boolean,
        mute: Boolean,
        detail: List<Detail>,
        reminder: List<Reminder>,
        time: Int,
        oldName: String,
        dayOfWeek: Int
    ){
        for(item in courseList){
            if((item.time == time)&&(item.courseName == oldName)&&(item.dayOfWeek == dayOfWeek)){
                item.courseName = name
                item.address = address
                item.notice = notice
                item.focus = focus
                item.mute = mute
                item.detail = detail
                item.reminder = reminder
            }
        }
        GlobalScope.launch(Dispatchers.IO){
            DAO.updateCourseDetailByTime(
                name,
                address,
                notice,
                focus,
                mute,
                detail,
                reminder,
                time,
                oldName,
                dayOfWeek
            )
        }
    }

    fun updateCourseDetailByName(
        name: String,
        address: String,
        notice: Boolean,
        focus: Boolean,
        mute: Boolean,
        detail: List<Detail>,
        reminder: List<Reminder>,
        oldName: String
    ){
        for(item in courseList){
            if(item.courseName == oldName){
                item.courseName = name
                item.address = address
                item.notice = notice
                item.focus = focus
                item.mute = mute
                item.detail = detail
                item.reminder = reminder
            }
        }
        GlobalScope.launch(Dispatchers.IO){
            DAO.updateCourseDetailByName(
                name,
                address,
                notice,
                focus,
                mute,
                detail,
                reminder,
                oldName
            )
        }
    }
    // TODO 给庚宝的接口
    fun updateCourseIsGeneratedFlag(uid:Long,generate : Boolean){
        for(item in courseList){
            if(item.courseId == uid){
                //两个id相等
                item.isGenerate = generate
            }
        }
        GlobalScope.launch(Dispatchers.IO){
            DAO.updateCourseIsGeneratedFlag(uid,generate)
        }
    }
}