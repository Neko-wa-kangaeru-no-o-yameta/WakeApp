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
import android.text.TextUtils
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.findFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_focus.view.*
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
        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            }
            startActivityForResult(
                chooseFile,
                INTENT_ID_GET_FILE
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //从SD卡中返回结果 进行解析
        super.onActivityResult(requestCode, resultCode, data)
        val weekdays = Regex("星期.")
        val courseTime = Regex("第.*节")
        val courseRegex = Regex("""[^,].*?\[.*?周]\[.*?]""")
        Log.d("get in", "On resultActivity")
//        val mainHolder = findViewById<GridLayout>(R.id.content_holder)
//        val context = mainHolder.context
        if (data == null || resultCode == AppCompatActivity.RESULT_CANCELED) {
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
                    for (row in 0 until 9) {
                        for (col in 0 until 8) {
                            val cell = sheet.getRow(row).getCell(col)

                            if (cell != null) {
                                val coursetext = cell.toString()
                                //                            Log.d("coursetext", coursetext)
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
                                        //                                        Log.d("courseName", courseName)
                                        //                                        Log.d("courseWeek", courseWeek.toString())
                                        //                                        Log.d("courseAdress2", courseAdress)

                                        //将数字部分提取出来,比如1-3，4-6，3，9-14周，则提取出1-3,4-6,3,9-14
                                        val weekList =
                                            Regex("[0-9]*-[0-9]*|[0-9]*").findAll(courseWeek)
                                        weekList.forEach { t ->
                                            if (t.value != "") {
                                                //                                            println("week list:" + t.value)
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
                                                            (row - 2)
                                                        )
                                                        //                                                    Log.d("insertCourse", course.toString())
                                                        GlobalScope.launch(Dispatchers.IO) {
                                                            context?.let { it1 ->
                                                                AppRoomDB.getDataBase(it1).getDAO()
                                                                    .insert(course)
                                                            }
                                                        }
                                                        //                                                    Log.d("insertAppdatabase?", "ok")
                                                    }
                                                } else {
                                                    //是单个星期
                                                    val course = Course(
                                                        courseName,
                                                        element.first().toInt(),
                                                        col,
                                                        courseAddress,
                                                        (row - 2)
                                                    )
                                                    //                                                Log.d("insertCourse", course.toString())
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
                            //                        d("$r $c", cell?.toString() ?: "")
                        }
                    }
                    file.close()
                    Handler(Looper.getMainLooper()).post {
                        val courseFragment = CourseFragment.newInstance()
                        childFragmentManager.beginTransaction()
                            .replace(R.id.fragment2, courseFragment)
                            .commit()
                    }
                }
            }
        }

//        val courseFragment = CourseFragment.newInstance()
//        childFragmentManager.beginTransaction()
//            .replace(R.id.fragment2, courseFragment)
//            .commit()
        //Log.d("sche", "replaced")
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
suspend fun Activity.getPerWeekCourse(search: Int): List<Course> {
    val courseForPerWeek = arrayListOf<Course>()

    val classALL = AppRoomDB.getDataBase(this).getDAO().findWeekCourse(search)
    for (ele in classALL) {
        courseForPerWeek.add(ele)
    }
    return courseForPerWeek
}

