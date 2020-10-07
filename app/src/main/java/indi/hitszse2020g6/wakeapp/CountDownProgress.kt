package indi.hitszse2020g6.wakeapp

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast

class CountDownProgress: View {
    private var defaultCircleSolideColor: Int = Color.BLUE
    private var defaultCircleStrokeColor: Int = Color.WHITE
    private var defaultCircleStrokeWidth = 16
    private var defaultCircleRadius = 390
    private var progressColor: Int = Color.BLUE
    private var progressWidth = 8
    private var textColor: Int = Color.BLACK
    private var textSize = 30f
    private var defaultCriclePaint: Paint? = null
    private var progressPaint: Paint? = null
    private var currentAngle = 0f
    private var countdownTime: Long = 0
    private var mStartSweepValue = -90

    lateinit var animator:ValueAnimator

    constructor(context: Context):super(context)

    constructor(context: Context, attrs: AttributeSet):super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):super(
        context,
        attrs,
        defStyleAttr
    ){
        //使用typedArray获得自定义属性
        //使用Context的obtainStyleAttributes创建typedArray
        var typedArray: TypedArray = getContext().obtainStyledAttributes(
            attrs,
            R.styleable.CountDownProgress
        )
        //attr.xml中表明了format
        //根据format使用不同的的getColor/getDimension方法
        var indexCount:Int = typedArray.indexCount
        var i:Int = 0
        for(i in 0..indexCount){
            var attr:Int = typedArray.getIndex(i)
            when(attr){
                R.styleable.CountDownProgress_default_circle_solide_color -> {
                    defaultCircleSolideColor = typedArray.getColor(attr, defaultCircleSolideColor)
                    break
                }
                R.styleable.CountDownProgress_default_circle_stroke_color -> {
                    defaultCircleStrokeColor = typedArray.getColor(attr, defaultCircleStrokeColor);
                    break;
                }
                R.styleable.CountDownProgress_default_circle_stroke_width -> {
                    defaultCircleStrokeWidth =
                        typedArray.getDimension(attr, defaultCircleStrokeWidth.toFloat()).toInt()
                    break
                }
                R.styleable.CountDownProgress_default_circle_radius -> {
                    defaultCircleRadius =
                        typedArray.getDimension(attr, defaultCircleRadius.toFloat()).toInt()
                    break
                }
                R.styleable.CountDownProgress_progress_color -> {
                    progressColor = typedArray.getColor(attr, progressColor);
                    break;
                }
                R.styleable.CountDownProgress_progress_width -> {
                    progressWidth = typedArray.getDimension(attr, progressWidth.toFloat()).toInt()
                    break
                }
                R.styleable.CountDownProgress_text_color -> {
                    textColor = typedArray.getColor(attr, textColor);
                    break;
                }
                R.styleable.CountDownProgress_text_size -> {
                    textSize = typedArray.getDimension(attr, textSize)
                    break
                }
            }
        }
        //回收画笔
        typedArray.recycle()
    }

    //设置画笔的方法
    private fun setPaint() {
        //默认圆
        defaultCriclePaint = Paint()
        defaultCriclePaint!!.isAntiAlias = true //抗锯齿
        defaultCriclePaint!!.isDither = true //防抖动
        defaultCriclePaint!!.style = Paint.Style.STROKE
        defaultCriclePaint!!.strokeWidth = defaultCircleStrokeWidth.toFloat()
        defaultCriclePaint!!.color = defaultCircleStrokeColor //这里先画边框的颜色，后续再添加画笔画实心的颜色
        //默认圆上面的进度弧度
        progressPaint = Paint()
        progressPaint!!.isAntiAlias = true
        progressPaint!!.isDither = true
        progressPaint!!.style = Paint.Style.STROKE
        progressPaint!!.strokeWidth = progressWidth.toFloat()
        progressPaint!!.color = progressColor
        progressPaint!!.strokeCap = Paint.Cap.ROUND //设置画笔笔刷样式

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //设置画笔
        setPaint()
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        //画默认圆
        canvas.drawCircle(
            defaultCircleRadius.toFloat(),
            defaultCircleRadius.toFloat(),
            defaultCircleRadius.toFloat(),
            defaultCriclePaint!!
        )

        //画进度圆弧
        canvas.drawArc(
            RectF(
                0F, 0F, (defaultCircleRadius * 2).toFloat(),
                (defaultCircleRadius * 2).toFloat()
            ), mStartSweepValue.toFloat(), 360 * currentAngle, false, progressPaint!!
        )
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize: Int
        val heightSize: Int
        val strokeWidth = Math.max(defaultCircleStrokeWidth, progressWidth)
        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = paddingLeft + defaultCircleRadius * 2 + strokeWidth + paddingRight
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = paddingTop + defaultCircleRadius * 2 + strokeWidth + paddingBottom
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setAnimation(offset: Float){
        animator = ValueAnimator.ofFloat(offset, 1f)
        //动画时长，让进度条在CountDown时间内正好从0-360走完，这里由于用的是CountDownTimer定时器，倒计时要想减到0则总时长需要多加1000毫秒，所以这里时间也跟着+1000ms
        animator.duration = countdownTime
        animator.interpolator = LinearInterpolator() //匀速
        animator.repeatCount = 0 //表示不循环，-1表示无限循环
        //值从0-1.0F 的动画，动画时长为countdownTime，ValueAnimator没有跟任何的控件相关联，那也正好说明ValueAnimator只是对值做动画运算，而不是针对控件的，我们需要监听ValueAnimator的动画过程来自己对控件做操作
        //添加监听器,监听动画过程中值的实时变化(animation.getAnimatedValue()得到的值就是0-1.0)
        animator.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
            invalidate() //实时刷新view，这样我们的进度条弧度就动起来了
        }
        //开启动画
        animator.start()
        //还需要另一个监听，监听动画状态的监听器
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                //倒计时结束的时候，需要通过自定义接口通知UI去处理其他业务逻辑
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun stopAnima(){
        Toast.makeText(context,"Anima End",Toast.LENGTH_SHORT).show()
        animator.end()
        invalidate()
    }

//    fun startCountDownTime(myCountDownTimer: CountDownTimer){
//        //调用倒计时操作
//        myCountDownTimer.start()
//    }

    fun setCountdownTime(countdownTime: Long) {
        this.countdownTime = countdownTime
    }
}