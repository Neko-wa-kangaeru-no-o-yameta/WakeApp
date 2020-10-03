package indi.hitszse2020g6.wakeapp


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
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
    private var btnFlag:Boolean = false

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

        startBtn.setOnClickListener (object : View.OnClickListener {
            override fun onClick(v: View?) {
                setButtonAni(true)
            }
        })

        pauseBtn.setOnClickListener{
            if(!btnFlag){
                pauseBtn.setImageDrawable(resources.getDrawable(R.drawable.startbutton_fill_24))
                btnFlag = true
            }else{
                pauseBtn.setImageDrawable(resources.getDrawable(R.drawable.pausebutton_fill_24))
                btnFlag = false
            }
        }

        cancelBtn.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                setButtonAni(false)
            }
        })
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

    private fun init(hourPicker: NumberPicker, minutePicker: NumberPicker){
        hourPicker.setFormatter(this);
        hourPicker.setOnValueChangedListener(this);
        hourPicker.setOnScrollListener(this);
        hourPicker.setMaxValue(3);
        hourPicker.setMinValue(0);
        hourPicker.setValue(0);
//        hourPicker.textSize = 50f


        minutePicker.setFormatter(this);
        minutePicker.setOnValueChangedListener(this);
        minutePicker.setOnScrollListener(this);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setValue(0);
//        minutePicker.textSize = 50f

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

    private fun setButtonAni(divide:Boolean){
        val startSpring:SpringForce
        val pauseSpring:SpringForce
        val cancelSpring:SpringForce
        val startAnima:SpringAnimation
        val pauseAnima:SpringAnimation
        val cancelAnima:SpringAnimation
        val sAnimation = AlphaAnimation(1.0f,0.0f)
        val LRAnimation = AlphaAnimation(0.0f,1.0f)

        //分裂动画
        if(divide){
            //一开始开始按钮就要消失
            startBtn.visibility = View.INVISIBLE
            startBtn.isClickable = false
            //一开始左右两个按钮均为可见
            pauseBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
            sAnimation.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {
                    Log.d(TAG,"Animation starts")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.d(TAG,"Animation ends")
                    //动画结束时左右两个按钮可点击
                    pauseBtn.isClickable = true
                    cancelBtn.isClickable = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    Log.d(TAG,"Animation repeats")
                }

            })
            sAnimation.duration = 200
            startBtn.startAnimation(sAnimation)

            LRAnimation.duration = 200
            pauseBtn.startAnimation(LRAnimation)
            cancelBtn.startAnimation(LRAnimation)

            startSpring = SpringForce(0f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            pauseSpring = SpringForce(-150f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            cancelSpring = SpringForce(150f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            startAnima = SpringAnimation(startBtn,SpringAnimation.TRANSLATION_Z).setSpring(startSpring)
            pauseAnima = SpringAnimation(pauseBtn,SpringAnimation.TRANSLATION_X).setSpring(pauseSpring)
            cancelAnima = SpringAnimation(cancelBtn,SpringAnimation.TRANSLATION_X).setSpring(cancelSpring)

            startAnima.cancel()
            startAnima.setStartValue(0f)
            startAnima.start()

            pauseAnima.cancel()
            pauseAnima.setStartValue(0f)
            pauseAnima.start()

            cancelAnima.cancel()
            cancelAnima.setStartValue(0f)
            cancelAnima.start()

        }else{
            LRAnimation.duration = 200
            LRAnimation.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {
                    Log.d(TAG,"Animation starts")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.d(TAG,"Animation ends")
                    //动画结束时左右两个按钮不可见，仅有开始按钮可见
                    startBtn.visibility = View.VISIBLE
                    pauseBtn.visibility = View.INVISIBLE
                    cancelBtn.visibility = View.INVISIBLE
                    startBtn.isClickable = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    Log.d(TAG,"Animation repeats")
                }

            })
            pauseBtn.startAnimation(LRAnimation)
            cancelBtn.startAnimation(LRAnimation)

            pauseSpring = SpringForce(0f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            cancelSpring = SpringForce(0f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            pauseAnima = SpringAnimation(pauseBtn,SpringAnimation.TRANSLATION_X).setSpring(pauseSpring)
            cancelAnima = SpringAnimation(cancelBtn,SpringAnimation.TRANSLATION_X).setSpring(cancelSpring)

            pauseAnima.cancel()
            pauseAnima.setStartValue(-150f)
            pauseAnima.start()

            cancelAnima.cancel()
            cancelAnima.setStartValue(150f)
            cancelAnima.start()
        }
    }
}