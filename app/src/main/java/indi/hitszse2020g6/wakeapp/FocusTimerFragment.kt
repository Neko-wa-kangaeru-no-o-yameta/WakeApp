package indi.hitszse2020g6.wakeapp


import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlinx.android.synthetic.main.fragment_focus_timer.*
import android.content.SharedPreferences
import android.widget.*
import androidx.core.content.ContextCompat
import com.binioter.guideview.Component
import com.binioter.guideview.GuideBuilder

const val REQUEST_CODE_OVERLAY = 101
const val REQUEST_SYS_ALERT = 102

class FocusTimerFragment : Fragment(), NumberPicker.OnValueChangeListener,
    NumberPicker.OnScrollListener, NumberPicker.Formatter {

    private val TAG: String = "FocusTimerFragment"

    var myCountDownTimer: CountDownTimer? = null

    var total_time: Long = 0
    var condition_flag: Int = 0
    var before_sys_time: Long = 0
    var set_focus_title: String = "用户自定义专注"

    private var btnFlag: Boolean = false

    private lateinit var mySharedPreferences: SharedPreferences
    private lateinit var myDao: RoomDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myDao = MainPageEventList.DAO
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_focus_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNumberPicker(hourpicker, minuteipcker)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")

        startBtn.setOnClickListener {
            if (condition_flag == 0) {
                //获得设置的时间
                total_time = (hourpicker.value * 3600 + minuteipcker.value * 60).toLong()
                if (total_time > 0) {
                    setButtonAni(true)
                    //设置动画时长
                    set_focus_title = "用户自定义"
                    setMyCountDownTimer(total_time)
                    myCircle.setCountdownTime(total_time * 1000)
                    myCircle.setAnimation(0f)
                    //开始的时候存数据，李宇航后面会判
                    val mt = MyFocusEntry(
                        uid = System.currentTimeMillis(),
                        totalFocusTime = total_time,
                        focusDate = System.currentTimeMillis(),
                        set_focus_title,
                        false
                    )
                    myDao.addFocusData(mt)
                } else {
                    Toast.makeText(context, "Unable to start a 0 minute focus.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (condition_flag == -1) {
//                Toast.makeText(context,set_focus_title,Toast.LENGTH_SHORT).show()
                condition_flag = 0
                myCircle.setAnimation(1f)
                myCircle.setCountdownTime(0)
                storeTime()
//                val mt = MyFocusEntry(
//                    uid = System.currentTimeMillis(),
//                    totalFocusTime = total_time,
//                    focusDate = System.currentTimeMillis(),
//                    set_focus_title,
//                    false
//                )
//                myDao.addFocusData(mt)
                startBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_play_circle_filled_24))
                toggleDisplay(false)
                hour.text = getString(R.string.two_zero)
                minute.text = getString(R.string.two_zero)
                second.text = getString(R.string.two_zero)
            }
        }

        pauseBtn.setOnClickListener {
            btnFlag = if (!btnFlag) {
                pauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_play_circle_filled_24))
                (activity as MainActivity).binder?.setIsBlocking(false)
                true
            } else {
                pauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_pause_circle_filled_24))
                (activity as MainActivity).binder?.setIsBlocking(true)
                false
            }
        }

        cancelBtn.setOnClickListener {
//            Toast.makeText(context, set_focus_title,Toast.LENGTH_SHORT).show()
//            val mt = MyFocusEntry(
//                uid = System.currentTimeMillis(),
//                totalFocusTime = total_time,
//                focusDate = System.currentTimeMillis(),
//                set_focus_title,
//                true
//            )
//            myDao.addFocusData(mt)
            if(myDao.getNearestFocusData().isNotEmpty()){
                val mt = myDao.getNearestFocusData()[0]
                val before_id = mt.uid
                myDao.updateFocusData(before_id,true)
                val tmpList = myDao.findFocusData(System.currentTimeMillis()-1000000000)
                for(item in tmpList){
                    Log.d("iiiiitem","${item.focusTitle}+${item.totalFocusTime}+${item.isCanceled}")
                }
            }

            condition_flag = 0

            storeTime()
            setButtonAni(false)
            myCountDownTimer?.cancel()
            (activity as MainActivity).binder?.setIsStored(false)
            (activity as MainActivity).binder?.stopCountDownTimer()
            (activity as MainActivity).binder?.setIsBlocking(false)
            myCircle.setAnimation(1f)
            myCircle.setCountdownTime(0)
            toggleDisplay(false)
            (activity as MainActivity).binder?.setIsBlocking(false)
        }

        getPreviousCondition()

        mySharedPreferences =
            requireContext().getSharedPreferences("new_user", Context.MODE_PRIVATE)
        if (mySharedPreferences.getBoolean("isNewTimerFragment", true)) {
            myCircle.post { showGuideView() }
            val editor = mySharedPreferences.edit()
            editor.putBoolean("isNewTimerFragment", false)
            editor.apply()
        }
    }

    private fun setMyCountDownTimer(setTime: Long) {
        //按了按钮开始计时
        if (condition_flag == 0) {
            setButtonAni(true)
            condition_flag = 1
            total_time = setTime
            (activity as MainActivity).binder?.setIsStored(true)
            (activity as MainActivity).binder?.startMyCountDownTimer(total_time, set_focus_title)
//            Toast.makeText(context, total_time.toString(), Toast.LENGTH_SHORT).show()

            storeTime()

//            myCircle.setCountdownTime(total_time * 1000)
//            myCircle.setAnimation(0f)
            toggleDisplay(true)
        }
        //如果之前开了一个计时先关掉
        if (myCountDownTimer != null) {
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
                    myCircle.setAnimation(1f)
                    myCircle.setCountdownTime(0)
                    startBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_check_circle_24))
                    setButtonAni(false)
                    Toast.makeText(context, "计时结束", Toast.LENGTH_SHORT).show()
                    condition_flag = -1
                    storeTime()
                    //service的也要停
                    if ((activity as MainActivity).mBound) {
                        (activity as MainActivity).binder?.stopCountDownTimer()
                        (activity as MainActivity).binder?.setIsStored(false)
                        (activity as MainActivity).binder?.setIsBlocking(false)
                    }
                    hour.text = getString(R.string.two_zero)
                    minute.text = getString(R.string.two_zero)
                    second.text = getString(R.string.two_zero)
                }
            }
        }.start()
    }

    private fun getPreviousCondition() {
        Log.d(TAG, "getPreviousConditon")
        val distance: Long
//        Log.d("Binder",((activity as MainActivity).binder == null).toString())
//        Log.d("Binder",(activity as MainActivity).binder?.getConditon().toString())
//        Log.d("Binder",(activity as MainActivity).binder?.getIsStored()!!.toString())
        if ((activity as MainActivity).binder != null && (activity as MainActivity).binder?.getConditon()!! > 0 && !(activity as MainActivity).binder?.getIsStored()!!) {
            //第一次进来，后台已经开始计时了，isStored表示第一次进来
            Log.d(TAG, "Background Service is Timing")
            condition_flag = 0
            total_time = (activity as MainActivity).binder?.getTotalTime()!!
            var pass_time = (activity as MainActivity).binder?.getConditon()!!
            set_focus_title = (activity as MainActivity).binder?.getFocusTitle()!!
            Log.d("FFFFFocusTitle",set_focus_title)
            distance = 0
            //跟着后台前台也开始计时
//            setMyCountDownTimer(total_time)

            setButtonAni(true)
            condition_flag = 1
            (activity as MainActivity).binder?.setIsStored(true)
            storeTime(System.currentTimeMillis()-(total_time-pass_time)*1000)

            myCircle.setCountdownTime(pass_time * 1000)
            myCircle.setAnimation((total_time-pass_time).toFloat()/total_time.toFloat())
            toggleDisplay(true)

            //如果之前开了一个计时先关掉
            if (myCountDownTimer != null) {
                myCountDownTimer!!.cancel()
            }
            myCountDownTimer = object : CountDownTimer((pass_time) * 1000, 1000) {
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
                        myCircle.setAnimation(1f)
                        myCircle.setCountdownTime(0)
                        startBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_check_circle_24))
                        setButtonAni(false)
                        Toast.makeText(context, "计时结束", Toast.LENGTH_SHORT).show()
                        condition_flag = -1
                        storeTime()
                        //service的也要停
                        if ((activity as MainActivity).mBound) {
                            (activity as MainActivity).binder?.stopCountDownTimer()
                            (activity as MainActivity).binder?.setIsStored(false)
                            (activity as MainActivity).binder?.setIsBlocking(false)
                        }
                        hour.text = getString(R.string.two_zero)
                        minute.text = getString(R.string.two_zero)
                        second.text = getString(R.string.two_zero)
                    }
                }
            }.start()

            //这里记得返回不然下面根据condition_flag会再开一次计时
            return
        } else {
            //后台没有开始计时，则读数据
            mySharedPreferences =
                requireContext().getSharedPreferences("user_time", Context.MODE_PRIVATE)
            if (mySharedPreferences.getInt("condition_flag", -2) != -2) {
                condition_flag = mySharedPreferences.getInt("condition_flag", -2)
                total_time = mySharedPreferences.getLong("total_time", 0)
                before_sys_time =
                    mySharedPreferences.getLong("before_system_time", System.currentTimeMillis())
                set_focus_title = mySharedPreferences.getString("focus_title","用户自定义").toString()
            }

            distance =
                (System.currentTimeMillis() - before_sys_time) / 1000      // before_sys_time is in ms, distance is in s???
            //如果之前是计时状态但是计时已经结束
            if (condition_flag == 1 && distance >= total_time) {
                condition_flag = -1
            }
        }
        //如果之前是在计时状态
        if (condition_flag == 1) {
            if ((activity as MainActivity).mBound && (!(activity as MainActivity).binder?.getBlock()!!)) {
                btnFlag = true
                pauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_play_circle_filled_24))
            }
            setButtonAni(true)
            toggleDisplay(true)
            //剩余计时时间
            setMyCountDownTimer(total_time - distance)
            //剩余计时时间
            myCircle.setCountdownTime((total_time - distance) * 1000)
            //剩余的弧形长度
            myCircle.setAnimation(distance.toFloat() / total_time.toFloat())
        } else if (condition_flag == -1) {
            //之前关掉的时候是计时结束状态
            toggleDisplay(true)
            hour.text = getString(R.string.two_zero)
            minute.text = getString(R.string.two_zero)
            second.text = getString(R.string.two_zero)
            myCircle.setAnimation(1f)
            myCircle.setCountdownTime(0)
            startBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_check_circle_24))
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

    private fun storeTime() {
        mySharedPreferences =
            requireContext().getSharedPreferences("user_time", Context.MODE_PRIVATE)
        val editor = mySharedPreferences.edit()
        editor.putLong("total_time", total_time)
        editor.putLong("before_system_time", System.currentTimeMillis())
        editor.putInt("condition_flag", condition_flag)
        editor.putString("focus_title",set_focus_title)
        editor.apply()
    }

    private fun storeTime(time:Long) {
        mySharedPreferences =
            requireContext().getSharedPreferences("user_time", Context.MODE_PRIVATE)
        val editor = mySharedPreferences.edit()
        editor.putLong("total_time", total_time)
        editor.putLong("before_system_time", time)
        editor.putInt("condition_flag", condition_flag)
        editor.putString("focus_title",set_focus_title)
        editor.apply()
    }

    private fun showGuideView() {
        val builder = GuideBuilder()
        builder.setTargetView(myCircle).setAlpha(150).setHighTargetPadding(10)
            .setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showGuideView2()
            }
        })
        builder.addComponent(MyCircleComponent())
        val guide = builder.createGuide()
        guide.show((activity as MainActivity))
    }

    private fun showGuideView2() {
        val builder = GuideBuilder()
        builder.setTargetView(startBtn).setAlpha(150).setHighTargetCorner(20)
            .setHighTargetPadding(20).setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {
                showGuideView3()
            }
        })
        builder.addComponent(SetClickComponent())
        val guide = builder.createGuide()
        guide.show((activity as MainActivity))
    }

    private fun showGuideView3(){
        val builder = GuideBuilder()
        builder.setTargetView(startBtn).setAlpha(150).setHighTargetCorner(20)
            .setHighTargetPadding(20).setHighTargetGraphStyle(Component.CIRCLE)
        builder.setOnVisibilityChangedListener(object : GuideBuilder.OnVisibilityChangedListener {
            override fun onShown() {}
            override fun onDismiss() {}
        })
        builder.addComponent(GuideToStasticFragmentComponent())
        val guide = builder.createGuide()
        guide.show((activity as MainActivity))
    }

    class GuideToStasticFragmentComponent:Component {
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_stastic_frag, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_BOTTOM
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 60
        }

        override fun getYOffset(): Int {
            return 0
        }
    }

    class SetClickComponent : Component {
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_start_timer_btn, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_BOTTOM
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 60
        }

        override fun getYOffset(): Int {
            return 0
        }
    }

    class MyCircleComponent : Component {
        override fun getView(inflater: LayoutInflater?): View {
            return inflater?.inflate(R.layout.layer_my_circle, null) as LinearLayout
        }

        override fun getAnchor(): Int {
            return Component.ANCHOR_OVER
        }

        override fun getFitPosition(): Int {
            return Component.FIT_END
        }

        override fun getXOffset(): Int {
            return 0
        }

        override fun getYOffset(): Int {
            return 100
        }

    }
}