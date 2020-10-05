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
import kotlinx.android.synthetic.main.fragment_focus_statistic.*


//@ExperimentalFeature
class FocusStatisticFragment : Fragment() {

    private var focusFrequency = 13
    private var breakTimes = 14
    private var focusTime = 147
    private var donutTotal = 0f

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
        showStatistic()
        lineChart.animation.duration = animationDuration
        lineChart.animate(lineSet)
        setDonutChart()
    }


    private fun showStatistic() {
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


    private fun setDonutTotal(): Float {
        donutTotal = 0f
        for (data in donutSet)
            donutTotal += data
        return donutTotal// - donutSet[donutSet.size-1]
    }


    @SuppressLint("CutPasteId")
    private fun setDonutChart(){
        val legendCreator = requireView().findViewById<LinearLayout>(R.id.legendLayout)
        legendCreator.removeAllViews()
        for (data in donutPairSet) {
            val tempLegendData = linkedMapOf(" " to 0f, data.toPair().first to data.toPair().second)
            val legendItem = LayoutInflater.from(legendCreator.context)
                .inflate((R.layout.focus_statistic_legend_item), legendCreator, false)
            legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart).animation.duration = legendAnimationDuration
            legendItem.findViewById<HorizontalBarChartView>(R.id.horizontalBarChart).animate(tempLegendData)
            legendCreator.addView(legendItem)
        }
        val donutChartCreator = requireView().findViewById<LinearLayout>(R.id.donutChartLayout)
        val chartItem = LayoutInflater.from(donutChartCreator.context)
            .inflate((R.layout.focus_statistic_donutchart_item), donutChartCreator, false)
        chartItem.findViewById<DonutChartView>(R.id.donutChart).animation.duration = animationDuration
        chartItem.findViewById<DonutChartView>(R.id.donutChart).donutColors = donutColorSet
        chartItem.findViewById<DonutChartView>(R.id.donutChart).donutTotal = setDonutTotal()
        chartItem.findViewById<DonutChartView>(R.id.donutChart).animate(donutSet)
        donutChartCreator.addView(chartItem)
    }

    companion object {
        private const val animationDuration = 1000L
        private const val legendAnimationDuration = 0L
        private val lineSet = linkedMapOf(
            "label1" to 5f,
            "label2" to 4.5f,
            "label3" to 4.7f,
            "label4" to 3.5f,
            "label5" to 3.6f,
            "label6" to 7.5f,
            "label7" to 7.5f,
            "label8" to 10f,
            "label9" to 5f,
            "label10" to 6.5f,
            "label11" to 3f,
            "label12" to 4f
        )
        private var donutSet = listOf(
            200f,
            120f,
            80f
        )
        var donutPairSet = linkedMapOf<String, Float>(
            "A" to 200f,
            "B" to 120f,
            "C" to 80f
        )
        val donutColorSet = intArrayOf(
            Color.parseColor("#8888CC"),
            Color.parseColor("#CCCCFF"),
            Color.parseColor("#AAAAFF")
        )
    }
}