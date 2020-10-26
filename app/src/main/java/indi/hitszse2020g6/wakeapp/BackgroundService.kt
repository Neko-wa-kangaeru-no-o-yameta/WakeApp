package indi.hitszse2020g6.wakeapp

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import org.json.JSONArray
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
    "com.android.permissioncontroller",
    "indi.hitszse2020g6.wakeapp",
    "",
    "com.google.android.apps.nexuslauncher",
    "com.miui.home",
    "com.huawei.android.launcher"
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
    var defaultWhiteList:MutableList<String> = ArrayList()

    var isStored:Boolean = false
    var isBlocking: Boolean = false
    var startBlocking: Long = -1
    var stopBlocking: Long = -1
    var focusTitle: String = ""

    var isMuting: Boolean = false
    var totalTime:Long = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        pendingReturn = false
        Log.d(TAG, "rebind.")
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
//                    for(item in finalWhiteList){
//                        Log.d("DDDDDebug",item)
//                    }
                    for(wlPackageName in finalWhiteList) {
                        if(topPackageName == wlPackageName) needToBlock = false
                    }
//                    Log.d("BCKGRND", "$topPackageName, needToBlock = $needToBlock, isBlocking = $isBlocking")
                    if (needToBlock && isBlocking) {
                        if (!pendingReturn) {
                            Log.d("BCKGRND", "Trying to return...")
                            val i = Intent(this@BackgroundService, MainActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            i.putExtra("RequestCode", REQUEST_OPEN_TIMER_FRG)
                            Handler(Looper.getMainLooper()).post { drawOverlay() }
                            this@BackgroundService.startActivity(i)
                            pendingReturn = true
                        }
                    }
                }
            },
            1000, 1000
        )

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
            isBlocking = block
            Log.d("BlockAppService","isBlocking changed: $isBlocking")
        }

        fun getBlock():Boolean {
            val blocking = isBlocking
            return blocking
        }

        fun stopCountDownTimer(){
            myCountTime = 0
            useCustomWhiteList = false
            if(myCountDownTimer!=null){
                myCountDownTimer!!.cancel()
            }
            isBlocking = false
            (getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_NORMAL
            Log.d("BlockAppService","stopCountDownTimer")
        }

        fun setCustomWhiteList(cusWL: List<String>) {
            customWhiteList = cusWL
        }

        fun setUseCustomWhiteList(useCusWL : Boolean) {
            useCustomWhiteList = useCusWL
        }

        fun setDefaultWhiteList(defWL: List<String>) {
            defaultWhiteList = defWL as MutableList<String>
        }

        fun getFocusTitle() = focusTitle

        fun startTimer(entry:EventTableEntry){
            totalTime = entry.stopTime - entry.startTime
            for (item in entry.customWhiteList){
                Log.d("CustomWhiteList",item)
            }
            useCustomWhiteList = true
            customWhiteList = entry.customWhiteList
            if(entry.mute) {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_SILENT
            }
            startMyCountDownTimer(totalTime,entry.title)
            //发送BroadCast通知切换页面
            val myIntent = Intent()
            myIntent.action = "switchToFocusFragment"
            sendBroadcast(myIntent)
        }

        fun startMyCountDownTimer(totalTime:Long,title:String){
            //读一下用户默认白名单
            //在开启专注的时候更改现有的会使用原来的
            val mySharedPreferences = getSharedPreferences("user_default_white_list", Context.MODE_PRIVATE)
            val jsonArray = JSONArray(mySharedPreferences.getString("default_white_list","[]"))
            defaultWhiteList = ArrayList()
            Log.d(TAG,"Hhhhhhhere")
            if(jsonArray.length()>0){
                for(item in 0 until jsonArray.length()){
                    defaultWhiteList.add(jsonArray.get(item) as String)
                }
            }

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
                    (getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }.start()
            Log.d("BlockAppService","startCountDownTimer")
        }

        fun getStartTime(): Long = startBlocking

        fun getStopTime(): Long = stopBlocking

        fun getConditon():Long = myCountTime

        fun getTotalTime():Long = totalTime

        fun setIsStored(b:Boolean){
            isStored = b
        }

        fun getIsStored():Boolean = isStored
    }

}