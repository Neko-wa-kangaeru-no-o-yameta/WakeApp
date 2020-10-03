package indi.hitszse2020g6.wakeapp


import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_focus_timer.*
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val REQUEST_CODE_OVERLAY = 101
const val REQUEST_SYS_ALERT = 102

/**
 * A simple [Fragment] subclass.
 * Use the [FocusTimerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FocusTimerFragment : Fragment(), NumberPicker.OnValueChangeListener,
    NumberPicker.OnScrollListener, NumberPicker.Formatter {

    lateinit var myCountDownTimer: CountDownTimer

    private val TAG: String = "FocusTimerFragment"

    //    private var param1: String? = null
//    private var param2: String? = null
    private var btnFlag: Boolean = false

    private lateinit var myDatabase: AppRoomDB
    private lateinit var myDao: RoomDAO

    private var totalTime: Long = 0
    private var conditionFlag: Int = 0
    private var beforeSysTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
        myDatabase =
            Room.databaseBuilder(requireContext(), AppRoomDB::class.java, "app_room_database")
                .allowMainThreadQueries().build()

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
        Log.d(TAG, "onActivityCreated")
        initNumberPicker(hourpicker, minuteipcker)

        myDatabase = Room.databaseBuilder(requireContext(), AppRoomDB::class.java, "my_database")
            .allowMainThreadQueries().build()
        myDao = myDatabase.getDAO()

        getPreviousCondition()

        startBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (conditionFlag == 0) {
                    conditionFlag = 1
                    setButtonAni(true)
                    //获得设置的时间
                    totalTime = (hourpicker.value * 3600 + minuteipcker.value * 60).toLong()
                    Toast.makeText(context, totalTime.toString(), Toast.LENGTH_SHORT).show()

                    var myTime: MyTimeEntry = MyTimeEntry(
                        1,
                        totalTime,
                        conditionFlag,
                        System.currentTimeMillis()
                    )
                    if (myDao.findFromTimeTable().size == 0) {
                        myDao.insertMyTime(myTime)
                    } else {
                        myDao.updateMyTime(myTime)
                    }
                    //设置一下开始计时
                    pickTime.visibility = View.INVISIBLE
                    showTime.visibility = View.VISIBLE

                    setMyCountDownTimer(totalTime)
                    myCircle.setCountdownTime(totalTime * 1000)
                    myCircle.setAnimation(0f)
                    myCircle.startCountDownTime(myCountDownTimer)
                } else if (conditionFlag == -1) {
                    //往数据库里记一下这次的专注时间和专注次数
                    //记得写

                    conditionFlag = 0
                    var myTime: MyTimeEntry = MyTimeEntry(
                        1,
                        totalTime,
                        conditionFlag,
                        System.currentTimeMillis()
                    )
                    if (myDao.findFromTimeTable().size == 0) {
                        myDao.insertMyTime(myTime)
                    } else {
                        myDao.updateMyTime(myTime)
                    }
                    startBtn.setImageDrawable(resources.getDrawable(R.drawable.startbutton_fill_24))
                    pickTime.visibility = View.VISIBLE
                    showTime.visibility = View.INVISIBLE

                }
            }
        })

        pauseBtn.setOnClickListener {
            if (!btnFlag) {
                pauseBtn.setImageDrawable(resources.getDrawable(R.drawable.startbutton_fill_24))
                btnFlag = true
            } else {
                pauseBtn.setImageDrawable(resources.getDrawable(R.drawable.pausebutton_fill_24))
                btnFlag = false
            }

            //记得写业务逻辑
        }

        cancelBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                setButtonAni(false)
                myCountDownTimer.cancel()
                myCircle.stopAnima()
                showTime.visibility = View.INVISIBLE
                pickTime.visibility = View.VISIBLE
                conditionFlag = 0
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        myCountDownTimer.cancel()
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

    private fun initNumberPicker(hourPicker: NumberPicker, minutePicker: NumberPicker) {
        hourPicker.setFormatter(this);
        hourPicker.setOnValueChangedListener(this);
        hourPicker.setOnScrollListener(this);
        hourPicker.setMaxValue(3);
        hourPicker.setMinValue(0);
        hourPicker.setValue(0);


        minutePicker.setFormatter(this);
        minutePicker.setOnValueChangedListener(this);
        minutePicker.setOnScrollListener(this);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setValue(0);

        //设置为对当前值不可编辑
        hourPicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        //这里设置为不循环显示，默认值为true
        hourPicker.setWrapSelectorWheel(true);
        minutePicker.setWrapSelectorWheel(true);
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        Log.i(
            TAG, "onValueChange: 原来的值 " + oldVal + "--新值: "
                    + newVal
        );
    }

    override fun onScrollStateChange(view: NumberPicker?, scrollState: Int) {
        when (scrollState) {
            NumberPicker.OnScrollListener.SCROLL_STATE_FLING -> Log.i(
                TAG,
                "onScrollStateChange: 后续滑动"
            )
            NumberPicker.OnScrollListener.SCROLL_STATE_IDLE -> Log.i(
                TAG,
                "onScrollStateChange: 不滑动"
            )
            NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> Log.i(
                TAG,
                "onScrollStateChange: 滑动中"
            )
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

    private fun setButtonAni(divide: Boolean) {
        val startSpring: SpringForce
        val pauseSpring: SpringForce
        val cancelSpring: SpringForce
        val startAnima: SpringAnimation
        val pauseAnima: SpringAnimation
        val cancelAnima: SpringAnimation
        val sAnimation = AlphaAnimation(1.0f, 0.0f)
        val LRAnimation = AlphaAnimation(0.0f, 1.0f)

        //分裂动画
        if (divide) {
            //一开始开始按钮就要消失
            startBtn.visibility = View.INVISIBLE
            startBtn.isClickable = false
            //一开始左右两个按钮均为可见
            pauseBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
            sAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    Log.d(TAG, "Animation starts")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.d(TAG, "Animation ends")
                    //动画结束时左右两个按钮可点击
                    pauseBtn.isClickable = true
                    cancelBtn.isClickable = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    Log.d(TAG, "Animation repeats")
                }

            })
            sAnimation.duration = 200
            startBtn.startAnimation(sAnimation)

            LRAnimation.duration = 200
            pauseBtn.startAnimation(LRAnimation)
            cancelBtn.startAnimation(LRAnimation)

            startSpring =
                SpringForce(0f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            pauseSpring =
                SpringForce(-150f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            cancelSpring =
                SpringForce(150f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            startAnima = SpringAnimation(startBtn, SpringAnimation.TRANSLATION_Z).setSpring(
                startSpring
            )
            pauseAnima = SpringAnimation(pauseBtn, SpringAnimation.TRANSLATION_X).setSpring(
                pauseSpring
            )
            cancelAnima = SpringAnimation(cancelBtn, SpringAnimation.TRANSLATION_X).setSpring(
                cancelSpring
            )

            startAnima.cancel()
            startAnima.setStartValue(0f)
            startAnima.start()

            pauseAnima.cancel()
            pauseAnima.setStartValue(0f)
            pauseAnima.start()

            cancelAnima.cancel()
            cancelAnima.setStartValue(0f)
            cancelAnima.start()

        } else {
            LRAnimation.duration = 200
            LRAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    Log.d(TAG, "Animation starts")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.d(TAG, "Animation ends")
                    //动画结束时左右两个按钮不可见，仅有开始按钮可见
                    startBtn.visibility = View.VISIBLE
                    pauseBtn.visibility = View.INVISIBLE
                    cancelBtn.visibility = View.INVISIBLE
                    startBtn.isClickable = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    Log.d(TAG, "Animation repeats")
                }

            })
            pauseBtn.startAnimation(LRAnimation)
            cancelBtn.startAnimation(LRAnimation)

            pauseSpring =
                SpringForce(0f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            cancelSpring =
                SpringForce(0f).setDampingRatio(1f).setStiffness(SpringForce.STIFFNESS_LOW)
            pauseAnima = SpringAnimation(pauseBtn, SpringAnimation.TRANSLATION_X).setSpring(
                pauseSpring
            )
            cancelAnima = SpringAnimation(cancelBtn, SpringAnimation.TRANSLATION_X).setSpring(
                cancelSpring
            )

            pauseAnima.cancel()
            pauseAnima.setStartValue(-150f)
            pauseAnima.start()

            cancelAnima.cancel()
            cancelAnima.setStartValue(150f)
            cancelAnima.start()
        }
    }

    private fun setMyCountDownTimer(setTime: Long) {
        myCountDownTimer = object : CountDownTimer((setTime) * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val h: Long = millisUntilFinished / (1000 * 60 * 60) //单位时
                val m: Long =
                    (millisUntilFinished - h * (1000 * 60 * 60)) / (1000 * 60) //单位分
                val s: Long =
                    (millisUntilFinished - h * (1000 * 60 * 60) - m * (1000 * 60)) / 1000 //单位秒L
                //防止下拉的时候出错
                if (hour != null && minute != null && second != null) {
                    hour.text = h.toString()
                    if (m < 10) {
                        minute.text = "0" + m.toString()
                    } else {
                        minute.text = m.toString()
                    }
                    if (s < 10) {
                        second.text = "0" + s.toString()
                    } else {
                        second.text = s.toString()
                    }
                }
            }

            //计时结束的操作
            override fun onFinish() {
                //计时结束
                //可能要改

                if (startBtn != null) {
                    startBtn.setImageDrawable(resources.getDrawable(R.drawable.coutdown_finished_fill_24))
                    setButtonAni(false)
                    Toast.makeText(context, "计时结束", Toast.LENGTH_SHORT).show()
                    conditionFlag = -1
                }
            }
        }
    }

    private fun getPreviousCondition() {
        var tmp = myDao.findFromTimeTable()
        if (tmp.size != 0) {
            totalTime = tmp[0].totalTime
            conditionFlag = tmp[0].conditionFlag
            beforeSysTime = tmp[0].beforeSysTime
        }
        var distance: Long = (System.currentTimeMillis() - beforeSysTime) / 1000
        //如果之前是计时状态但是计时已经结束
        if (conditionFlag == 1 && distance >= totalTime) {
            conditionFlag = -1
        }
        //如果之前是在计时状态
        if (conditionFlag == 1) {
            setButtonAni(true)

            pickTime.visibility = View.INVISIBLE
            showTime.visibility = View.VISIBLE
            setMyCountDownTimer(totalTime - distance)
            myCircle.setCountdownTime((totalTime - distance) * 1000)
            myCircle.setAnimation(distance.toFloat() / totalTime.toFloat())
            myCircle.startCountDownTime(myCountDownTimer)
        } else if (conditionFlag == -1) {
            //之前关掉的时候是计时结束状态
            pickTime.visibility = View.INVISIBLE
            hour.text = "00"
            minute.text = "00"
            second.text = "00"
            showTime.visibility = View.VISIBLE
            startBtn.setImageDrawable(resources.getDrawable(R.drawable.coutdown_finished_fill_24))
            startBtn.isClickable = true
        }
    }
}