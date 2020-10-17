package indi.hitszse2020g6.wakeapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.text.Layout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.db.williamchart.view.DonutChartView
import com.db.williamchart.view.HorizontalBarChartView
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlinx.android.synthetic.main.fragment_focus_statistic.*
import java.util.*
import kotlin.collections.LinkedHashMap


//@ExperimentalFeature
class FocusStatisticFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_focus_statistic, container, false)
    }

    @SuppressLint("Range")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        readTodayStatistic(myDao)
        //以下所使用的参数均为全局变量，写入参数列表只是为了明确函数的定义
        showTodayStatistic(focusFrequency, breakTimes, focusTime)
        val lineSet = linkedMapOf<String,Float>()
        val donutPairSet = linkedMapOf<String,Float>()
        getChart(myDao,7L,lineSet,donutPairSet)
        setLineChart(lineSet)
        setDonutChart(donutPairSet, donutColorSet)
        switch1.setOnCheckedChangeListener {_ , isChecked ->
            if(isChecked){
                val lineSetSw = linkedMapOf<String,Float>()
                val donutPairSetSw = linkedMapOf<String,Float>()
                getChart(myDao,31L,lineSetSw,donutPairSetSw)
                lineChart.animate(lineSetSw)
                setDonutChart(donutPairSetSw, donutColorSet)
            }
            else{
                val lineSetSw = linkedMapOf<String,Float>()
                val donutPairSetSw = linkedMapOf<String,Float>()
                getChart(myDao,7L,lineSetSw,donutPairSetSw)
                lineChart.animate(lineSetSw)
                setDonutChart(donutPairSetSw, donutColorSet)
            }
        }
    }


    //today's statistic
    private fun readTodayStatistic(myDao:RoomDAO){
        val nowDate = Calendar.getInstance().apply{
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }.timeInMillis
        val statisticList:List<MyFocusEntry> = myDao.findFocusData(nowDate)
        //计算计时取消的次数和总共的时长
        var count = 0
        var totalTime = 0L
        for(data in statisticList){
            if(data.isCanceled) count++
            else totalTime += data.totalFocusTime
        }
        focusFrequency = statisticList.size//计时的总次数即为总的表项数
        breakTimes = count//计时取消的次数已经计算完成
        focusTime = (totalTime/60).toInt()
    }


    private fun showTodayStatistic(focusFrequency:Int,breakTimes:Int,focusTime: Int) {
        requireView().findViewById<TextView>(R.id.focusFrequency1)!!.text =
            (focusFrequency / 10).toString()
        requireView().findViewById<TextView>(R.id.focusFrequency0)!!.apply {
            text = (focusFrequency % 10).toString()
        }
        requireView().findViewById<TextView>(R.id.breakTimes1)!!.apply {
            text = (breakTimes / 10).toString()
        }
        requireView().findViewById<TextView>(R.id.breakTimes0)!!.apply {
            text = (breakTimes % 10).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime3)!!.apply {
            text = (focusTime / 1000).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime2)!!.apply {
            text = (focusTime / 100 % 10).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime1)!!.apply {
            text = (focusTime / 10 % 10).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime0)!!.apply {
            text = (focusTime % 10).toString()
        }
    }



    //chart week statistic
    //产生近7天统计数据列表
    private fun getChart(myDao: RoomDAO, days:Long ,lineSet: LinkedHashMap<String,Float>,donutPairSet: LinkedHashMap<String, Float>) {
        lineChartTitle.text = "近 $days 天专注时长统计(分钟)"
        val dayTime = (24 * 60 * 60000).toLong()
        val beginDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis - (days-1) * dayTime
        val statisticList: List<MyFocusEntry> = myDao.findFocusData(beginDate)
        //计算计时取消的次数和总共的时长,对lineChart使用
        var index = 0
        if(days == 7L){
            for (count in 1..7) {
                var totalTime = 0L
                while (index < statisticList.size && statisticList[index].focusDate < beginDate + count * dayTime) {
                    if (!statisticList[index].isCanceled) totalTime += statisticList[index].totalFocusTime
                    index++
                }
                lineSet[String.format("%d", count)] = (totalTime.toFloat() / 60)
            }
        }
        else{
            var tempString = " "
            for (count in 1..31) {
                var totalTime = 0L
                while (index < statisticList.size && statisticList[index].focusDate < beginDate + count * dayTime) {
                    if (!statisticList[index].isCanceled) totalTime += statisticList[index].totalFocusTime
                    index++
                }
                if(count%2 == 1){
                    lineSet[String.format("%d", count)] = (totalTime.toFloat() / 60)
                }
                else{
                    lineSet[tempString] = (totalTime.toFloat() / 60)
                    tempString = "$tempString "
                }
            }
        }
        //先统计各标题时长，对donutChart使用
        val donutSet = linkedMapOf<String,Float>()
        for (data in statisticList) {
            if (!data.isCanceled) {
                if (donutSet.containsKey(data.focusTitle)){
                    val temp = donutSet.remove(data.focusTitle)
                    if (temp != null) {
                        donutSet[data.focusTitle] = temp + data.totalFocusTime.toFloat()
                    }
                }
                else donutSet[data.focusTitle] =
                    data.totalFocusTime.toFloat()
            }
        }
        donutSet.toSortedMap()//转化为降序
        //饼状图最大显示7个项，如果大于7个则要将后面的数据算作“其他”
        if(donutSet.size > 7){
            var counter = 1
            var otherTotal = 0f
            for(data in donutSet){
                if(counter >= 7)
                {
                    otherTotal += data.value
                }
                else donutPairSet[setLabel(data.key)] = data.value
                counter++
            }
            donutPairSet["其他    "] = otherTotal
        }
        else{
            for(data in donutSet) donutPairSet[setLabel(data.key)] = data.value
        }
    }

    private fun setLabel(label:String):String{
        var returnLabel = ""
        if(label.length == 1)
            returnLabel = "  $label  "
        else if(label.length == 2)
            returnLabel = "  $label "
        else if(label.length == 3)
            returnLabel = " $label "
        else if(label.length == 4)
            returnLabel = " $label"
        else if(label.length == 5)
            returnLabel = label
        else
            returnLabel = label.substring(0,4)+"..."
        return returnLabel
    }

    //lineChart
    private fun setLineChart(Set:LinkedHashMap<String,Float>){
        lineChart.animation.duration = animationDuration
        lineChart.animate(Set)
        lineChart.labelsFormatter = {a:Float->String.format("%-3.1f",a)}
    }

    //donutChart
    private fun setDonutTotal(Set:List<Float>): Float {
        var donutTotal = 0f
        for (data in Set)
            donutTotal += data
        return donutTotal
    }

    private fun setDonutChart(Set: LinkedHashMap<String, Float>,ColorSet:IntArray) {
        val legendCreator = requireView().findViewById<LinearLayout>(R.id.legendLayout)
        legendCreator.removeAllViews()
        val donutSet = Set.map { it.value }
        if (Set.size == 0) {
            val emptyItem = LayoutInflater.from(legendCreator.context).inflate((R.layout.focus_statistic_empty_item),legendCreator,false)
            legendCreator.addView(emptyItem)
        }
        else {
            var counter = 0
            for (data in Set) {
                val tempLegendData = linkedMapOf(" " to 0f, data.toPair().first to 10f)
                val legendItem = LayoutInflater.from(legendCreator.context)
                    .inflate((R.layout.focus_statistic_legend_item), legendCreator, false)
                val legend = legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart)
                legend.barsColor = ColorSet[ColorSet.size - counter - 1]
                legend.animation.duration = legendAnimationDuration
                legend.animate(tempLegendData)
                legendCreator.addView(legendItem)
                counter++
            }
        }
        val donutChart = requireView().findViewById<DonutChartView>(R.id.donutChart)
        donutChart.animation.duration = animationDuration
        donutChart.donutColors = ColorSet
        donutChart.donutTotal = setDonutTotal(donutSet)
        donutChart.animate(donutSet)
    }

    companion object {
        //constant DAO
        private var myDao:RoomDAO = MainPageEventList.DAO
        //constant variable
        private const val animationDuration = 1000L
        private const val legendAnimationDuration = 0L
        private val donutColorSet = intArrayOf(
            Color.parseColor("#70977F"),
            Color.parseColor("#95B596"),
            Color.parseColor("#EEEEDD"),
            Color.parseColor("#E8D7FF"),
            Color.parseColor("#CCCCFF"),
            Color.parseColor("#AAAAFF"),
            Color.parseColor("#8D8DEF")
        )

        //read from database
        private var focusFrequency = 0
        private var breakTimes = 0
        private var focusTime = 0
    }
}