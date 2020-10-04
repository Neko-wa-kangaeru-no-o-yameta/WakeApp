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
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.schedule

const val REQUEST_OPEN_TIMER_FRG:Int = 109

class BlockAppService : Service() {
    private val TAG:String = "BlockApp Service"
    var isBlocking: Boolean = true
    private val binder: MyBinder = MyBinder()
    private var pendingReturn = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"BlockAppService onStartCommannd $isBlocking")
        Timer().scheduleAtFixedRate(
            object : TimerTask() {
                @RequiresApi(Build.VERSION_CODES.Q)
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
                    if ((topPackageName == "com.android.chrome" || topPackageName == "com.zhihu.android") && isBlocking) {
                        if (!pendingReturn) {
                            Handler(Looper.getMainLooper()).post { drawOverlay() }
                            Log.d("BCKGRND", "Trying to return...")
                            val i = Intent(this@BlockAppService, MainActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            i.putExtra("RequestCode", REQUEST_OPEN_TIMER_FRG)
                            this@BlockAppService.startActivity(i)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        fun getService() : BlockAppService = this@BlockAppService
        fun changeIsBlocking(){
            Log.d("BlockAppService","isBlocking changed")
            isBlocking = !isBlocking
        }
        fun getBlock():Boolean = isBlocking
        fun startCountDownTimer(){

        }
    }

}