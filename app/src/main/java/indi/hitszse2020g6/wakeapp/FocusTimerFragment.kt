package indi.hitszse2020g6.wakeapp


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.res.ResourcesCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.room.Room
import kotlinx.android.synthetic.main.fragment_focus_timer.*


const val REQUEST_CODE_OVERLAY = 101
const val REQUEST_SYS_ALERT = 102

class FocusTimerFragment : Fragment(), NumberPicker.OnValueChangeListener,
    NumberPicker.OnScrollListener, NumberPicker.Formatter {

    private val TAG: String = "FocusTimerFragment"

    var myCountDownTimer: CountDownTimer? = null

    var total_time: Long = 0
    var condition_flag: Int = 0
    var before_sys_time: Long = 0

    private var btnFlag: Boolean = false

    private lateinit var myDatabase: AppRoomDB
    private lateinit var myDao: RoomDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        registerReceiver()

        getPreviousCondition()

        startBtn.setOnClickListener {
            if (condition_flag == 0) {
                setButtonAni(true)
                //获得设置的时间
                total_time = (hourpicker.value * 3600 + minuteipcker.value * 60).toLong()
                //设置动画时长
                myCircle.setCountdownTime(total_time * 1000)
                myCircle.setAnimation(0f)

                if ((activity as MainActivity).mBound) {
                    (activity as MainActivity).binder.startCountDownTimer(total_time)
                }
            } else if (condition_flag == -1) {
                //往数据库里记一下这次的专注时间和专注次数
                //记得写

                condition_flag = 0
                val myTime = MyTimeEntry(
                    1,
                    total_time,
                    condition_flag,
                    System.currentTimeMillis()
                )
                if (myDao.findFromTimeTable().isEmpty()) {
                    myDao.insertMyTime(myTime)
                } else {
                    myDao.updateMyTime(myTime)
                }
                startBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.startbutton_fill_24,
                        null
                    )
                )
                toggleDisplay(false)
            }
        }

        pauseBtn.setOnClickListener {
            btnFlag = if (!btnFlag) {
                pauseBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.startbutton_fill_24,
                        null
                    )
                )
                true
            } else {
                pauseBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.pausebutton_fill_24,
                        null
                    )
                )
                false
            }
            (activity as MainActivity).binder.changeIsBlocking()
        }

        cancelBtn.setOnClickListener {
            condition_flag = 0
            val myTime = MyTimeEntry(
                1,
                total_time,
                condition_flag,
                System.currentTimeMillis()
            )
            if (myDao.findFromTimeTable().isEmpty()) {
                myDao.insertMyTime(myTime)
            } else {
                myDao.updateMyTime(myTime)
            }
            setButtonAni(false)
            myCountDownTimer?.cancel()
            if ((activity as MainActivity).mBound) {
                (activity as MainActivity).binder.stopCountDownTimer()
            }
            myCircle.stopAnima()
            toggleDisplay(false)
        }
    }


    private fun initNumberPicker(hourPicker: NumberPicker, minutePicker: NumberPicker) {
        hourPicker.setFormatter(this)
        hourPicker.setOnValueChangedListener(this)
        hourPicker.setOnScrollListener(this)
        hourPicker.maxValue = 3
        hourPicker.minValue = 0
        hourPicker.value = 0


        minutePicker.setFormatter(this)
        minutePicker.setOnValueChangedListener(this)
        minutePicker.setOnScrollListener(this)
        minutePicker.maxValue = 59
        minutePicker.minValue = 0
        minutePicker.value = 0

        //设置为对当前值不可编辑
        hourPicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        minutePicker.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS

        //这里设置为不循环显示，默认值为true
        hourPicker.wrapSelectorWheel = true
        minutePicker.wrapSelectorWheel = true
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {

    }

    override fun onScrollStateChange(view: NumberPicker?, scrollState: Int) {

    }

    override fun format(value: Int): String {
        var tmpStr = java.lang.String.valueOf(value)
        if (value < 10) {
            tmpStr = "0$tmpStr"
        }
        return tmpStr
    }

    fun setButtonAni(divide: Boolean) {
        val startSpring: SpringForce
        val pauseSpring: SpringForce
        val cancelSpring: SpringForce
        val startAnima: SpringAnimation
        val pauseAnima: SpringAnimation
        val cancelAnima: SpringAnimation
        val sAnimation: AlphaAnimation
        val LrAnimation: AlphaAnimation
        //分裂动画
        if (divide) {
            sAnimation = AlphaAnimation(1.0f, 0.0f)
            LrAnimation = AlphaAnimation(0.0f, 1.0f)
            //一开始开始按钮就要消失
            startBtn.visibility = View.INVISIBLE
            startBtn.isClickable = false
            //一开始左右两个按钮均为可见
            pauseBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
            sAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
//                    Log.d(TAG, "Animation starts")
                }

                override fun onAnimationEnd(animation: Animation?) {
//                    Log.d(TAG, "Animation ends")
                    //动画结束时左右两个按钮可点击
                    pauseBtn.isClickable = true
                    cancelBtn.isClickable = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
//                    Log.d(TAG, "Animation repeats")
                }

            })
            sAnimation.duration = 200
            startBtn.startAnimation(sAnimation)

            LrAnimation.duration = 200
            pauseBtn.startAnimation(LrAnimation)
            cancelBtn.startAnimation(LrAnimation)

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
            LrAnimation = AlphaAnimation(1.0f, 0.0f)
            LrAnimation.duration = 200
            LrAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
//                    Log.d(TAG, "Animation starts")
                }

                override fun onAnimationEnd(animation: Animation?) {
//                    Log.d(TAG, "Animation ends")
                    //动画结束时左右两个按钮不可见，仅有开始按钮可见
                    startBtn.visibility = View.VISIBLE
                    pauseBtn.visibility = View.INVISIBLE
                    cancelBtn.visibility = View.INVISIBLE
                    startBtn.isClickable = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
//                    Log.d(TAG, "Animation repeats")
                }

            })
            pauseBtn.startAnimation(LrAnimation)
            cancelBtn.startAnimation(LrAnimation)

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
        if(condition_flag == 0){
            setButtonAni(true)
            condition_flag = 1
            total_time = setTime
            Toast.makeText(context,total_time.toString(),Toast.LENGTH_SHORT).show()
            val myTime = MyTimeEntry(
                1,
                total_time,
                condition_flag,
                System.currentTimeMillis()
            )
            if (myDao.findFromTimeTable().isEmpty()) {
                myDao.insertMyTime(myTime)
            } else {
                myDao.updateMyTime(myTime)
            }
            myCircle.setCountdownTime(total_time * 1000)
            myCircle.setAnimation(0f)
            toggleDisplay(true)
        }
        if(myCountDownTimer!=null){
            myCountDownTimer!!.cancel()
        }
        myCountDownTimer = object : CountDownTimer((setTime) * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val h: Long = millisUntilFinished / (1000 * 60 * 60) //单位时
                val m: Long =
                    (millisUntilFinished - h * (1000 * 60 * 60)) / (1000 * 60) //单位分
                val s: Long =
                    (millisUntilFinished - h * (1000 * 60 * 60) - m * (1000 * 60)) / 1000 //单位秒L
                //防止下拉的时候出错
                if (hour != null && minute != null && second != null) {
                    hour.text = "0$h"
                    if (m < 10) {
                        minute.text = "0$m"
                    } else {
                        minute.text = m.toString()
                    }
                    if (s < 10) {
                        second.text = "0$s"
                    } else {
                        second.text = s.toString()
                    }
                }
            }

            //计时结束的操作
            override fun onFinish() {
                if (startBtn != null) {
                    startBtn.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.coutdown_finished_fill_24,null))
                    setButtonAni(false)
                    Toast.makeText(context, "计时结束", Toast.LENGTH_SHORT).show()
                    condition_flag = -1
                }
            }
        }.start()
    }

    private fun getPreviousCondition() {
        val tmp = myDao.findFromTimeTable()
        if (tmp.isNotEmpty()) {
            total_time = tmp[0].totalTime
            condition_flag = tmp[0].conditionFlag
            before_sys_time = tmp[0].beforeSysTime
        }
        val distance: Long = (System.currentTimeMillis() - before_sys_time) / 1000
        //如果之前是计时状态但是计时已经结束
        if (condition_flag == 1 && distance >= total_time) {
            condition_flag = -1
        }
        //如果之前是在计时状态
        if (condition_flag == 1) {
            if ((activity as MainActivity).mBound && (!(activity as MainActivity).binder.getBlock())) {
                btnFlag = true
                pauseBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.startbutton_fill_24,
                        null
                    )
                )
            }
            setButtonAni(true)
            toggleDisplay(true)
            myCircle.setCountdownTime((total_time - distance) * 1000)
            myCircle.setAnimation(distance.toFloat() / total_time.toFloat())

            if ((activity as MainActivity).mBound) {
                (activity as MainActivity).binder.startCountDownTimer(total_time - distance)
            }else{
                Log.d(TAG,"oooops")
            }

        } else if (condition_flag == -1) {
            //之前关掉的时候是计时结束状态
            toggleDisplay(true)
            hour.text = "00"
            minute.text = "00"
            second.text = "00"

            startBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.coutdown_finished_fill_24,
                    null
                )
            )
            startBtn.isClickable = true
        }
    }

    private fun toggleDisplay(bool: Boolean) {
        if (bool) {
            hourText.visibility = View.INVISIBLE
            minText.visibility = View.INVISIBLE
            hourpicker.visibility = View.INVISIBLE
            minuteipcker.visibility = View.INVISIBLE
            hour.visibility = View.VISIBLE
            minute.visibility = View.VISIBLE
            second.visibility = View.VISIBLE
            divide1.visibility = View.VISIBLE
            divide2.visibility = View.VISIBLE
        } else {
            hourText.visibility = View.VISIBLE
            minText.visibility = View.VISIBLE
            hourpicker.visibility = View.VISIBLE
            minuteipcker.visibility = View.VISIBLE
            hour.visibility = View.GONE
            minute.visibility = View.GONE
            second.visibility = View.GONE
            divide1.visibility = View.GONE
            divide2.visibility = View.GONE
        }
    }

    fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("startTicking")
        requireContext().registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                //后台通知前台开始计时
                var t = bundle?.getLong("startTicking_data")
                if (t != null) {
                    setMyCountDownTimer(t)
                }
            }
        }, intentFilter)
    }
}