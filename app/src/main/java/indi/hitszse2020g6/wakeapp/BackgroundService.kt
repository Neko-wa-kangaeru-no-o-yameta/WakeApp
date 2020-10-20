package indi.hitszse2020g6.wakeapp

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

const val REQUEST_OPEN_TIMER_FRG:Int = 109

val forceWhiteList: List<String> = arrayListOf(
    "android",
    "com.android.bluetooth",
    "com.android.contacts",
    "com.android.keychain",
    "com.android.keyguard",
    "com.android.launcher",
    "com.android.nfc",
    "com.android.phone",
    "com.android.providers.downloads",
    "com.android.settings",
    "com.android.systemui",
    "com.android.vending",
    "com.google.android.apps.enterprise.dmagent",
    "com.google.android.deskclock",
    "com.google.android.dialer",
    "com.google.android.gms",
    "com.google.android.googlequicksearchbox",
    "com.google.android.gsf",
    "com.google.android.gsf.login",
    "com.google.android.inputmethod.latin",
    "com.google.android.nfcprovision",
    "com.google.android.setupwizard",
    "com.samsung.android.contacts",
    "com.samsung.android.phone",
    "com.google.android.permissioncontroller",
    "indi.hitszse2020g6.wakeapp",
    "",
    "com.google.android.apps.nexuslauncher",
    "com.miui.home",
    // ADD YOUR LAUNCHER HERE!!!
)

class BackgroundService : Service() {
    private val TAG:String = "BlockApp Service"
    private val binder: MyBinder = MyBinder()
    private var pendingReturn = false
    private var myCountDownTimer:CountDownTimer? = null
    var myCountTime:Long = 0

    var useCustomWhiteList:Boolean = false
    var customWhiteList:List<String> = ArrayList()
    var defaultWhiteList:List<String> = ArrayList()

    var isStored:Boolean = false
    var isBlocking: Boolean = false
    var startBlocking: Long = -1
    var stopBlocking: Long = -1
    var focusTitle: String = ""

    var isMuting: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"BlockAppService onStartCommannd $isBlocking")
        Timer().scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    var topPackageName = ""
                    val mUsageStatsManager =
                        getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                    val time = System.currentTimeMillis()
                    // We get usage stats for the last 10 seconds
                    //public Map<String, UsageStats> queryAndAggregateUsageStats (long beginTime, long endTime)
                    val stats = mUsageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        time - 1000 * 10,
                        time
                    )
                    // Sort the stats by the last time used
                    //获得最前面的app
                    if (stats != null) {
                        val mySortedMap: SortedMap<Long, UsageStats> = TreeMap()
                        for (usageStats in stats) {
                            mySortedMap[usageStats.lastTimeUsed] = usageStats
                        }
                        if (!mySortedMap.isEmpty()) {
                            topPackageName = mySortedMap[mySortedMap.lastKey()]!!.packageName
                        }
                    }
//                    Log.d("BCKGRND", topPackageName)
                    var needToBlock = true
                    val finalWhiteList = listOf<String>(*forceWhiteList.toTypedArray(), *if(useCustomWhiteList) {customWhiteList.toTypedArray()} else {defaultWhiteList.toTypedArray()})
                    for(wlPackageName in finalWhiteList) {
                        if(topPackageName == wlPackageName) needToBlock = false
                    }
                    if (needToBlock && isBlocking) {
                        Log.d("BCKGRND", topPackageName)
                        if (!pendingReturn) {
                            Handler(Looper.getMainLooper()).post { drawOverlay() }
                            Log.d("BCKGRND", "Trying to return...")
                            val i = Intent(this@BackgroundService, MainActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            i.putExtra("RequestCode", REQUEST_OPEN_TIMER_FRG)
                            this@BackgroundService.startActivity(i)
                            pendingReturn = true
                        }
                    }
                }
            },
            1000, 1000
        )

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        pendingReturn = false
        Log.d(TAG, "rebind.")
        return binder
    }

    fun drawOverlay() {
        val windowManager = (applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Log.d("BCKGRND", "can't draw overlay")
        }

        val view = (applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.overlay, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(view, params)
        Log.d("BCKGRND", "Drawed overlay")
        Timer().schedule(6000) {
            Handler(Looper.getMainLooper()).post { windowManager.removeViewImmediate(view) }
            pendingReturn = false
        }
    }

    inner class MyBinder : Binder() {
        fun getService() : BackgroundService = this@BackgroundService

        fun setIsBlocking(block: Boolean){
            Log.d("BlockAppService","isBlocking changed")
            isBlocking = block
        }

        fun getBlock():Boolean = isBlocking

        fun stopCountDownTimer(){
            myCountTime = 0
            if(myCountDownTimer!=null){
                myCountDownTimer!!.cancel()
            }
            isBlocking = false
        }

        fun setCustomWhiteList(cusWL: List<String>) {
            customWhiteList = cusWL
        }

        fun setUseCustomWhiteList(useCusWL : Boolean) {
            useCustomWhiteList = useCusWL
        }

        fun setDefaultWhiteList(defWL: List<String>) {
            defaultWhiteList = defWL
        }

        fun getFocusTitle() = focusTitle

        fun startTimer(startTime:Long,endTime:Long, title: String){
            val totalTime = endTime - startTime
            startMyCountDownTimer(totalTime,title)
            //发送BroadCast通知切换页面
            val myIntent = Intent()
            myIntent.action = "switchToFocusFragment"
            sendBroadcast(myIntent)
        }

        fun startMyCountDownTimer(totalTime:Long,title:String){
            myCountTime = totalTime
            focusTitle = title
            //开始计时
            isBlocking = true
            if(myCountTime.toInt()!=0 && myCountDownTimer!=null){
                //如果之前在计时
                myCountDownTimer!!.cancel()
            }
            myCountDownTimer = object : CountDownTimer(totalTime*1000,1000){
                override fun onTick(millisUntilFinished: Long) {
                    //Do nothing
                    myCountTime--
                }
                override fun onFinish() {
                    isStored = false
                    isBlocking = false
                    Log.d(TAG,"BACKGROUND TIMER FINISHED")
                }
            }.start()
        }

        fun getStartTime(): Long = startBlocking

        fun getStopTime(): Long = stopBlocking

        fun getConditon():Long = myCountTime

        fun setIsStored(b:Boolean){
            isStored = b
        }

        fun getIsStored():Boolean = isStored
    }

}