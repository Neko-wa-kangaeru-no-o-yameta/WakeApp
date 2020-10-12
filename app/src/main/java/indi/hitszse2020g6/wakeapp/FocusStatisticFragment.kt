package indi.hitszse2020g6.wakeapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.LinearLayout
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
        getLineChartDataWeek(myDao,lineSet)
        setLineChart(lineSet)
        setDonutChart(donutPairSet, donutColorSet)
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
            totalTime += data.totalFocusTime
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
        requireView().findViewById<TextView>(R.id.focusTime2)!!.apply {
            text = (focusTime / 100).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime1)!!.apply {
            text = (focusTime / 10 % 10).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime0)!!.apply {
            text = (focusTime % 10).toString()
        }
    }


    //lineChart
    //week statistic
    private fun getLineChartDataWeek(myDao: RoomDAO,lineSet: LinkedHashMap<String,Float>){
        val dayTime = (24*60*60000).toLong()
        val beginDate = Calendar.getInstance().apply{
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }.timeInMillis - 6*dayTime
        val statisticList:List<MyFocusEntry> = myDao.findFocusData(beginDate)
        //计算计时取消的次数和总共的时长
        var index = 0
        for(count in 1..7){
            var totalTime = 0L
            while(index < statisticList.size && statisticList[index].focusDate < beginDate + count*dayTime){
                totalTime += statisticList[index].totalFocusTime
                index++
            }
            lineSet[String.format("%d",count)] = (totalTime.toFloat()/60)
        }
    }

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

    @SuppressLint("CutPasteId")
    private fun setDonutChart(Set: LinkedHashMap<String, Float>,ColorSet:IntArray){
        val legendCreator = requireView().findViewById<LinearLayout>(R.id.legendLayout)
        legendCreator.removeAllViews()
        val donutSet = Set.map { it.value }
        var counter = 0

        for (data in Set) {
            val tempLegendData = linkedMapOf(" " to 0f, data.toPair().first to 10f)
            val legendItem = LayoutInflater.from(legendCreator.context)
                .inflate((R.layout.focus_statistic_legend_item), legendCreator, false)
            if(counter < Set.size-1)
                legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart).barsColor = ColorSet[ColorSet.size - counter-1]
            else
                legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart).barsColor = ColorSet[0]
            legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart).animation.duration = legendAnimationDuration
            legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart).animate(tempLegendData)
            legendCreator.addView(legendItem)
            counter++
        }
        val donutChartCreator = requireView().findViewById<LinearLayout>(R.id.donutChartLayout)
        val chartItem = LayoutInflater.from(donutChartCreator.context)
            .inflate((R.layout.focus_statistic_donutchart_item), donutChartCreator, false)
        chartItem.findViewById<DonutChartView>(R.id.donutChart).animation.duration = animationDuration
        chartItem.findViewById<DonutChartView>(R.id.donutChart).donutColors = ColorSet
        chartItem.findViewById<DonutChartView>(R.id.donutChart).donutTotal = setDonutTotal(donutSet)
        chartItem.findViewById<DonutChartView>(R.id.donutChart).animate(donutSet)
        donutChartCreator.addView(chartItem)
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
//        private var lineSet = linkedMapOf(
//            "label1" to 5f,
//            "label2" to 4.5f,
//            "label3" to 4.7f,
//            "label4" to 3.5f,
//            "label5" to 3.6f,
//            "label6" to 7.5f,
//            "label7" to 7.5f,
//            "label8" to 10f,
//            "label9" to 5f,
//            "label10" to 6.5f,
//            "label11" to 3f,
//            "label12" to 4f
//        )
        private var donutPairSet = linkedMapOf<String, Float>(
            "A" to 100f,
            "B" to 100f,
            "C" to 200f,
            "D" to 200f,
            "E" to 300f,
            "F" to 400f,
            "G" to 500f
        )

    }
}