package indi.hitszse2020g6.wakeapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.TextView
import com.db.williamchart.ExperimentalFeature
import com.db.williamchart.slidertooltip.SliderTooltip
import com.db.williamchart.pointtooltip.PointTooltip
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_focus.*
import kotlinx.android.synthetic.main.fragment_focus_statistic.*


//@ExperimentalFeature
class FocusStatisticFragment : Fragment() {

    private var focusFrequency = 13
    private var breakTimes = 14
    private var focusTime = 147

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
        horizontalBarChart0.animation.duration = animationDuration
        horizontalBarChart0.animate(horizontalBarSet0)
        horizontalBarChart1.animation.duration = animationDuration
        horizontalBarChart1.animate(horizontalBarSet1)
        horizontalBarChart2.animation.duration = animationDuration
        horizontalBarChart2.animate(horizontalBarSet2)
        donutChart.animation.duration = animationDuration
        donutChart.donutColors = intArrayOf(
            Color.parseColor("#8888CC"),
            Color.parseColor("#CCCCFF"),
            Color.parseColor("#AAAAFF")
        )
        donutChart.animate(donutSet)


    }

    private fun showStatistic()
    {
        requireView().findViewById<TextView>(R.id.focusFrequency1)!!.text = (focusFrequency/10).toString()
        requireView().findViewById<TextView>(R.id.focusFrequency0)!!.apply{
            text = (focusFrequency%10).toString()
        }
        requireView().findViewById<TextView>(R.id.breakTimes1)!!.apply{
            text = (breakTimes/10).toString()
        }
        requireView().findViewById<TextView>(R.id.breakTimes0)!!.apply{
            text = (breakTimes%10).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime2)!!.apply{
            text = (focusTime/100).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime1)!!.apply {
            text = (focusTime/10%10).toString()
        }
        requireView().findViewById<TextView>(R.id.focusTime0)!!.apply {
            text = (focusTime%10).toString()
        }
    }
    companion object {
        private const val animationDuration = 1000L
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
        private val horizontalBarSet0 = linkedMapOf(
            "    " to 0F,
            "PORRO" to 3F,
            "     " to 0F
        )
        private val horizontalBarSet1 = linkedMapOf(
            "    " to 0F,
            "FUSCE" to 3F,
            "     " to 0F
        )
        private val horizontalBarSet2 = linkedMapOf(
            "    " to 0F,
            "EGET" to 3F,
            "     " to 0F

        )
        private val donutSet = listOf(
            20f,
            80f,
            100f
        )
    }
}