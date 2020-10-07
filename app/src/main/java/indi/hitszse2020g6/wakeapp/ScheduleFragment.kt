package indi.hitszse2020g6.wakeapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.leinardi.android.speeddial.SpeedDialView
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream


// TODO:add address to the schedule table
//TODO:change the color to the table



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Schedule.newInstance] factory method to
 * create an instance of this fragment.
 */

const val INTENT_ID_GET_FILE = 1
const val INTENT_GRANT_PERMISSION = 2
const val INTENT_ADD_COURSE = 3
class Schedule : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (this.context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this.context as Activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // Do Nothing.
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    INTENT_GRANT_PERMISSION
                )
            }
        }
        view.findViewById<SpeedDialView>(R.id.addCourseBotton).apply {
            inflate(R.menu.schedule_fragment_speed_dial_menu)
            setOnActionSelectedListener {actionItems->
                when(actionItems.id){
                    R.id.scheduleFragment_speedDialNewExcel ->{
                        val chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        }
                        startActivityForResult(
                            chooseFile,
                            INTENT_ID_GET_FILE
                        )
                        close()
                        true
                    }
                    R.id.scheduleFragment_speedDialNewCourseAdded ->{
                        startActivityForResult(Intent(activity,CourseAddActivity::class.java),`INTENT_ADD_COURSE`)
                        close()
                        true
                    }
                    else ->{
                        false
                    }
                }
            }
        }
//        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
//            val chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
//            }
//            startActivityForResult(
//                chooseFile,
//                INTENT_ID_GET_FILE
//            )
//        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("resultCode",resultCode.toString())
        Log.d("requestCode",requestCode.toString())
        Log.d("data",data.toString())
        //从SD卡中返回结果 进行解析
        super.onActivityResult(requestCode, resultCode, data)
        val weekdays = Regex("星期.")
        val courseTime = Regex("第.*节")
        val courseRegex = Regex("""[^,].*?\[.*?周]\[.*?]""")
        Log.d("get in", "On resultActivity")
//        val mainHolder = findViewById<GridLayout>(R.id.content_holder)
//        val context = mainHolder.context
        if(requestCode == INTENT_ADD_COURSE){

            //直接更新
            GlobalScope.launch(Dispatchers.IO){
                requireActivity().setPerCourseColor()
                Handler(Looper.getMainLooper()).post {
                    val courseFragment = CourseFragment.newInstance()
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragment2, courseFragment)
                        .commit()
                }
            }

        } else if (data == null || resultCode == AppCompatActivity.RESULT_CANCELED) {
            return
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                val uri = data.data
                uri?.let {
                    val file = FileInputStream(
                        activity?.contentResolver?.openFileDescriptor(
                            uri,
                            "r"
                        )!!.fileDescriptor
                    )
                    val sheet = XSSFWorkbook(file).getSheetAt(0)
                    if (sheet != null){
                        //先清空数据库
                        context?.let { it1 -> AppRoomDB.getDataBase(it1).getDAO().deleteAllCourse() }
//                        val maxNum = context?.let { it1 -> AppRoomDB.getDataBase(it1).getDAO().getMaxWeek() }
//                        Log.d("maxnum",maxNum.toString())
                        for (row in 0 until 9) {
                            for (col in 0 until 8) {
                                val cell = sheet.getRow(row).getCell(col)

                                if (cell != null) {
                                    val coursetext = cell.toString()

                                    if (!(weekdays.matches(coursetext) || courseTime.matches(coursetext))) {
                                        //是课程
                                        var coursecontent = coursetext
                                        coursecontent = coursecontent.replace("\n", "")//将换行删除
                                        val courseList = courseRegex.findAll(coursecontent)//将其解析
                                        courseList.forEach { f ->
                                            val m = f.value
                                            //提取每个课程每个元素
                                            var element = m.split("][", "[", "]")
                                            //将其分为课程名字，课程老师，课程所在的时间以及课程地址四个部分
                                            //因为有一些课程没有老师，统一将老师舍去
                                            val courseName = element[0]
                                            val courseWeek = element[2]
                                            val courseAddress = element[3]
                                            val courseNotice = true
                                            val courseFocus = true
                                            val courseMute = true
                                            //将数字部分提取出来,比如1-3，4-6，3，9-14周，则提取出1-3,4-6,3,9-14
                                            val weekList =
                                                Regex("[0-9]*-[0-9]*|[0-9]*").findAll(courseWeek)
                                            weekList.forEach { t ->
                                                if (t.value != "") {
                                                    //对每个星期段进行分析
                                                    element = t.value.split("-")
                                                    if (element.size != 1) {
                                                        //是一个星期段
                                                        val start = element.first().toInt()
                                                        val end = element.last().toInt()

                                                        Log.d("start", start.toString())
                                                        Log.d("end", end.toString())
                                                        for (week in start..end) {
                                                            val course = Course(
                                                                courseName,
                                                                week,
                                                                col,
                                                                courseAddress,
                                                                (row - 2),
                                                                null,
                                                                courseNotice,
                                                                courseFocus,
                                                                courseMute
                                                            )
                                                            GlobalScope.launch(Dispatchers.IO) {
                                                                context?.let { it1 ->
                                                                    AppRoomDB.getDataBase(it1).getDAO()
                                                                        .insert(course)
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        //是单个星期
                                                        val course = Course(
                                                            courseName,
                                                            element.first().toInt(),
                                                            col,
                                                            courseAddress,
                                                            (row - 2),
                                                            null,
                                                            courseNotice,
                                                            courseFocus,
                                                            courseMute
                                                        )
                                                        context?.let { it1 ->
                                                            AppRoomDB.getDataBase(it1).getDAO()
                                                                .insert(course)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    file.close()
                    requireActivity().setPerCourseColor()
                    Handler(Looper.getMainLooper()).post {
                        val courseFragment = CourseFragment.newInstance()
                        childFragmentManager.beginTransaction()
                            .replace(R.id.fragment2, courseFragment)
                            .commit()
                    }
                }
            }
        }


    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Schedule.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Schedule().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
suspend fun Activity.setPerCourseColor(){
    //得到课程表中所有课程所对应的颜色
    Log.d("get color","get color")
    //todo 修改颜色：
    val colorList = arrayOf(
        getColor(R.color.CourseTableColor1),
        getColor(R.color.CourseTableColor2),
        getColor(R.color.CourseTableColor3),
        getColor(R.color.CourseTableColor4),
        getColor(R.color.CourseTableColor5),
        getColor(R.color.CourseTableColor6),
        getColor(R.color.CourseTableColor7),
        getColor(R.color.CourseTableColor8)
    )
    val courseAllList = arrayListOf<String>()
    val courseAll = AppRoomDB.getDataBase(this).getDAO().getAllCourse()
    for(i in 0 until courseAll.size){
        Log.d("ele",i.toString())
        val courseName = courseAll[i]
        //8是预设的颜色的数量，可以随便定义
        val couseColor = colorList[i%8]
        AppRoomDB.getDataBase(this).getDAO().InsertCourseColorIntoTable(couseColor,courseName)
    }

}
suspend fun Activity.getPerWeekCourse(search: Int): List<Course> {
    val courseForPerWeek = arrayListOf<Course>()

    val classALL = AppRoomDB.getDataBase(this).getDAO().findWeekCourse(search)
    for (ele in classALL) {
        courseForPerWeek.add(ele)
    }
    return courseForPerWeek
}

