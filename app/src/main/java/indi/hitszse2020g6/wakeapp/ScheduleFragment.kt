package indi.hitszse2020g6.wakeapp

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.binioter.guideview.Component
import com.binioter.guideview.GuideBuilder
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.fragment_course.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList


// TODO:add address to the schedule table
//TODO:change the color to the table


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

const val INTENT_ID_GET_FILE = 1
const val INTENT_GRANT_PERMISSION = 2
const val INTENT_ADD_COURSE = 3


val weekdays = Regex("星期.")
val courseTime = Regex("第.*节")
val courseRegex = Regex("""[^,].*?\[.*?周]\[.*?]""")
val weekPattern = Regex("[0-9]*-[0-9]*|[0-9]*")

class ScheduleFragment : Fragment(),
    SelectCoursePickFragment.SelectCoursePickDailogListner{
    private val repeatList = arrayListOf<Course>()
    private var resultCourse = ArrayList<Course>()
    private val chineseWeek = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
//    private val getCourseFile =
//        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//            parseCourse(uri)
//
//        }

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
            setOnActionSelectedListener { actionItems ->
                when (actionItems.id) {
                    R.id.scheduleFragment_speedDialNewExcel -> {
                        val chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        }
                        startActivityForResult(
                            chooseFile,
                            INTENT_ID_GET_FILE
                        )
//                        getCourseFile.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        close()
                        true
                    }
                    R.id.scheduleFragment_speedDialNewCourseAdded -> {
                        startActivityForResult(
                            Intent(activity, CourseAddActivity::class.java),
                            `INTENT_ADD_COURSE`
                        )
                        close()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
        var mySharedPreferences =
            requireContext().getSharedPreferences("new_user", Context.MODE_PRIVATE)
        if (mySharedPreferences.getBoolean("isNewScheduleFragment", true)) {
            addCourseBotton.post { showGuideView() }
            var editor = mySharedPreferences.edit()
            editor.putBoolean("isNewScheduleFragment", false)
            editor.apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("123123","123123")
        var week:Int = 0
        super.onActivityResult(requestCode, resultCode, data)
        var mySharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(
            "schedule_time",
            Context.MODE_PRIVATE
        )


        if(mySharedPreferences.getLong("startTime", -1).toInt() !=-1){
            val calendar = Calendar.getInstance()

            val startTime = mySharedPreferences.getLong("startTime", -1)
            val stopTime = System.currentTimeMillis()
            calendar.time = Date(startTime)
            val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
//            calendar.firstDayOfWeek = Calendar.MONDAY
//
//            val startWeekMonday = calendar.timeInMillis
            week = ((stopTime - startTime + (startDayOfWeek-2)*24*60*60*1000)/7/(24*60*60*1000)).toInt()


        }
        Log.d("item===123123","get in vp2")
        Log.d("requestCode",requestCode.toString())
        if (requestCode == INTENT_ADD_COURSE) {
            //直接更新
            requireActivity().setPerCourseColor()
            val vp2 = view?.findViewById<ViewPager2>(R.id.viewPager2)

            if (vp2 != null) {
                vp2.adapter?.notifyDataSetChanged()
                vp2.post {
                    viewPager2.setCurrentItem(week,true)
                }
            }
            if(resultCode == RESULT_ADD_NEW_COURSE){
                Toast.makeText(context,"小猫咪帮你更新课程表啦", Toast.LENGTH_SHORT).show()
            }else{

                Toast.makeText(context,"小猫咪没敢动你的课表噢", Toast.LENGTH_SHORT).show()
            }

        }else if(data == null || resultCode == RESULT_CANCELED){
            return
        }else{
            Log.d("123123123123123123","22222")
            val uri = data?.data
            if (uri != null) {
                parseCourse(uri)
            }
            val vp2 = view?.findViewById<ViewPager2>(R.id.viewPager2)
            if (vp2 != null) {
                vp2.adapter?.notifyDataSetChanged()
                vp2.post {
                    viewPager2.setCurrentItem(week,true)
                }


            }
        }

    }

    private  fun parseCourse(uri: Uri) {

        val resultList = arrayListOf<Course>()
        FileInputStream(
            context?.contentResolver?.openFileDescriptor(
                uri,
                "r"
            )!!.fileDescriptor
        ).use { file ->
            val sheet = XSSFWorkbook(file).getSheetAt(0)
            if (sheet != null) {
                for (row in 0 until 9) {
                    for (col in 0 until 8) {
                        val courseText = sheet.getRow(row).getCell(col)?.toString()?:continue

                        val cellCourse = parseCell(courseText, col, row)

                        resultList.addAll(cellCourse)
                    }
                }
            }
        }
        repeatList.clear()
        //对resultList做是否冲突排查
        resultCourse = resultList
        for (item in resultList){
            val repeat= CourseList.selectCourseByTime(item.week,item.dayOfWeek,item.time)
            repeatList.addAll(repeat)
        }
        if(repeatList.isNotEmpty()){
            Toast.makeText(context,
                "小猫咪说你已经有已经有" +
                        "第${repeatList.first().week}周${chineseWeek[repeatList.first().dayOfWeek - 1]}" +
                        "第${repeatList.first().time}节的${repeatList.first().courseName}等${repeatList.size}门课程啦，" +
                        "请检查无误后再添加噢",Toast.LENGTH_LONG).show()
        }else{
            CourseList.importClassWithoutRepeat(resultList,repeatList)
            requireActivity().setPerCourseColor()
            val courseFragment = CourseFragment.newInstance()
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment2, courseFragment)
                .commit()
        }


    }

    private fun parseCell(cellText: String, col: Int, row: Int): List<Course> {
        val resultList = arrayListOf<Course>()
        if (!(weekdays.matches(cellText) ||
                    courseTime.matches(cellText))
        ) {
            //是课程
            val cellText = cellText.replace("\n", "")//将换行删除
            //将其解析
            val courseList = courseRegex.findAll(cellText)

            for (courseMatch in courseList) {
                val element = courseMatch.value.split("][", "[", "]")
                //提取每个课程每个元素
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
                    weekPattern.findAll(courseWeek)

                for (weekMatch in weekList) {
                    val weekRange = weekMatch.value
                    if (weekRange == "") {
                        continue
                    }
                    //对每个星期段进行分析
                    val element = weekRange.split("-")
                    if (element.size != 1) {
                        //是一个星期段
                        val start = element.first().toInt()
                        val end = element.last().toInt()
                        val detail = arrayListOf<Detail>(Detail("",""))
                        val reminder = arrayListOf<Reminder>(Reminder(-1,true,true,true,""))
                        for (week in start..end) {
                            val course = Course(
                                0,
                                courseName,
                                week,
                                col,
                                courseAddress,
                                (row - 2),
                                null,
                                courseNotice,
                                courseFocus,
                                courseMute,
                                detail.toList(),
                                reminder
                            )
                            resultList.add(course)
                        }
                    } else {
                        //是单个星期
                        val detail = arrayListOf<Detail>(Detail("",""))
                        val reminder = arrayListOf<Reminder>(Reminder(-1,true,true,true,""))
                        val course = Course(
                            0,
                            courseName,
                            element.first().toInt(),
                            col,
                            courseAddress,
                            (row - 2),
                            null,
                            courseNotice,
                            courseFocus,
                            courseMute,
                            detail.toList(),
                            reminder
                        )
                        resultList.add(course)
                    }
                }
            }
        }
        return resultList
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
            ScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun showGuideView(){
        val builder = GuideBuilder()
        builder.setTargetView(addCourseBotton).setAlpha(150).setHighTargetPadding(-20).setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener{
            override fun onDismiss() {}
            override fun onShown() {}
        })
        builder.addComponent(addCourseBtnComponent())
        val guide = builder.createGuide()
        guide.show((activity as MainActivity))
    }

    class addCourseBtnComponent:Component{
        override fun getView(inflater: LayoutInflater?): View {
            val ll:LinearLayout = inflater?.inflate(R.layout.layer_addcourse_btn,null) as LinearLayout
            return ll
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_TOP
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 0
        }

        override fun getYOffset(): Int {
            return -10
        }

    }

    override fun onDialogPositiveClickForSelectCoursePick(dialog: DialogFragment) {
        val pos = (dialog as SelectCoursePickFragment).selectItem
        if(pos == 1){
            //决定将它覆盖掉
            CourseList.importClassWithoutRepeat(resultCourse,repeatList)
        }
        requireActivity().setPerCourseColor()
        val courseFragment = CourseFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment2, courseFragment)
            .commit()
    }

    override fun onDialogNegativeClickForSelectCoursePick(dialog: DialogFragment) {

    }
}

fun Activity.setPerCourseColor() {
    //得到课程表中所有课程所对应的颜色
    Log.d("===get color===", "===get color===")
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
    val courseAll = CourseList.getAllCourse()
    for (i in courseAll.indices) {
        Log.d("ele", i.toString())
        val courseName = courseAll[i]
        //8是预设的颜色的数量，可以随便定义
        val courseColor = colorList[i % 8]
        CourseList.insertCourseColorIntoTable(courseColor,courseName)
    }

}

fun Context.getCourseOfTheWeek(search: Int): List<Course> {
    val courseForPerWeek = arrayListOf<Course>()

    val classALL = CourseList.findWeekCourse(search)
    for (ele in classALL) {
        courseForPerWeek.add(ele)
    }
    return courseForPerWeek
}

