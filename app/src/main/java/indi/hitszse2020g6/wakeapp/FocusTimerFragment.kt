package indi.hitszse2020g6.wakeapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.fragment_focus_timer.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FocusTimerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FocusTimerFragment : Fragment(),NumberPicker.OnValueChangeListener,NumberPicker.OnScrollListener,NumberPicker.Formatter{
    // TODO: Rename and change types of parameters
    private val TAG:String = "FocusTimerFragment"
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
        return inflater.inflate(R.layout.fragment_focus_timer, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init(hourpicker, minuteicker)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FocusTimerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FocusTimerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun init(hourPicker: NumberPicker, minutePicker: NumberPicker){
        hourPicker.setFormatter(this);
        hourPicker.setOnValueChangedListener(this);
        hourPicker.setOnScrollListener(this);
        hourPicker.setMaxValue(3);
        hourPicker.setMinValue(0);
        hourPicker.setValue(0);
        hourPicker.textSize = 50f


        minutePicker.setFormatter(this);
        minutePicker.setOnValueChangedListener(this);
        minutePicker.setOnScrollListener(this);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setValue(0);
        minutePicker.textSize = 50f

        //设置为对当前值不可编辑
        hourPicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        //这里设置为不循环显示，默认值为true
        hourPicker.setWrapSelectorWheel(true);
        minutePicker.setWrapSelectorWheel(true);
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        Log.i(TAG, "onValueChange: 原来的值 " + oldVal + "--新值: "
                + newVal);
    }

    override fun onScrollStateChange(view: NumberPicker?, scrollState: Int) {
        when (scrollState) {
            NumberPicker.OnScrollListener.SCROLL_STATE_FLING -> Log.i(TAG, "onScrollStateChange: 后续滑动")
            NumberPicker.OnScrollListener.SCROLL_STATE_IDLE -> Log.i(TAG, "onScrollStateChange: 不滑动")
            NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> Log.i(TAG, "onScrollStateChange: 滑动中")
        }
    }

    override fun format(value: Int): String {
        Log.i(TAG, "format: value")
        var tmpStr = java.lang.String.valueOf(value)
        if (value < 10) {
            tmpStr = "0$tmpStr"
        }
        return tmpStr
    }
}