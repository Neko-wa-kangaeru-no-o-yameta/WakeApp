package indi.hitszse2020g6.wakeapp

import android.Manifest
import android.app.*
import android.content.*
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.from
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import indi.hitszse2020g6.wakeapp.mainPage.PARAM_START_FOCUS_FROM_BACKGROUND
import indi.hitszse2020g6.wakeapp.mainPage.WeatherData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.net.UnknownHostException
import java.util.*


const val INTENT_AFFAIR_DETAIL = 1
const val INTENT_SCHEDULE_DETAIL = 2
const val REQUEST_SETTING_EVENT = 3
const val REQUEST_ALARM = 4

const val PARAM_START_FOCUS_TIME = "indi.hitszse2020g6.wakeapp.paramStartFocus"
const val ACTION_START_FOCUS_TIME = "indi.hitszse2020g6.wakeapp.actionStartFocus"
const val ACTION_START_ALARM = "indi.hitszse2020g6.wakeapp.actionStartAlarm"

class MainActivity() : AppCompatActivity() {

    var mBound = false
    lateinit var blockAppService: BackgroundService
    var binder: BackgroundService.MyBinder? = null
    var jumped = false
    private lateinit var sharedPreferences: SharedPreferences

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as BackgroundService.MyBinder
            blockAppService = binder!!.getService()
            mBound = true
            Log.d("MainActivity", "Connected")

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("MainActivity", "disconnected")
            mBound = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel(this)
        Log.d("MainActivity", "Oncreate")
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_main)

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
//            setTurnScreenOn(true)
//        } else {
//            window.addFlags(
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//            )
//        }

        Log.d("Main activity", "Waking up...")

        MainPageEventList.DAO = AppRoomDB.getDataBase(this).getDAO()
        GlobalScope.launch(Dispatchers.IO) {
            MainPageEventList.getEventListFromDB()
        }
        MainPageEventList.context = this
        MainPageEventList.alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        MainPageEventList.termStart = getSharedPreferences("schedule_time", Context.MODE_PRIVATE).getLong("startTime", -1)
        Log.d("MainActivity", "start at ${MainPageEventList.termStart}")
        if(MainPageEventList.termStart != -1L) {
            val c = Calendar.getInstance().apply { timeInMillis = MainPageEventList.termStart.toLong() }
            c.firstDayOfWeek = Calendar.MONDAY
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            MainPageEventList.currentWeek = ((System.currentTimeMillis() - c.timeInMillis) / (7 * 24 * 60 * 60 * 1000) + 1).toInt()
            MainPageEventList.currentDayOfWeek =
                mapOf(
                    Calendar.MONDAY to 1,
                    Calendar.TUESDAY to 2,
                    Calendar.WEDNESDAY to 3,
                    Calendar.THURSDAY to 4,
                    Calendar.FRIDAY to 5,
                    Calendar.SATURDAY to 6,
                    Calendar.SUNDAY to 7,
                )[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)] ?: error("MAP ERROR")
        }

        //加载课程表数据
        CourseList.DAO = AppRoomDB.getDataBase(this).getDAO()
        CourseList.getDatefromDB()
        CourseList.context = this

        receiveBroadCast()

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            Log.d("Main Activity", "onNavigationItemReselectedListener")
            when (item.itemId) {
                R.id.bottomNavHomeBtn -> {
                    findNavController(R.id.mainNavFragment).navigate(R.id.action_global_mainPageFragment)
                    true
                }
                R.id.bottomNavScheduleBtn -> {
                    findNavController(R.id.mainNavFragment).navigate(R.id.action_global_scheduleFragment)
                    true
                }
                R.id.bottomNavFocusBtn -> {
                    findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
                    true
                }
                R.id.bottomNavSettingBtn -> {
                    findNavController(R.id.mainNavFragment).navigate(R.id.action_global_settingsFragment)
                    true
                }
                else -> false
            }
        }

        val startFocusUid = intent.getLongExtra(PARAM_START_FOCUS_FROM_BACKGROUND, -1)
        if(startFocusUid != -1L) {
            GlobalScope.launch(Dispatchers.IO) {
                val entry = MainPageEventList.DAO.getEvent(startFocusUid)
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d("MainActivity", "Starting Timer...")
                    binder?.setUseCustomWhiteList(entry.hasCustomWhiteList)
                    binder?.setCustomWhiteList(entry.customWhiteList)
                    binder?.setIsBlocking(true)
                    binder?.startTimer(entry)
                }, 500)
            }
        }

        //之前课程表没有设置，默认写一下时间
        sharedPreferences = getSharedPreferences(
            "schedule_time",
            Context.MODE_PRIVATE
        )
        //之前啥都没有的话
        if(sharedPreferences.getLong("startTime", -1).toInt() ==-1){
            val editor = sharedPreferences.edit()
            val startTime = getTimeInMills(2020, 0, 1)
            val endTime = getTimeInMills(2020, 11, 31)
            editor.putLong("startTime", startTime)
            editor.putLong("endTime", endTime)
            editor.apply()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "Onstart")

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
//            setTurnScreenOn(true)
//        } else {
//            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
//        }


        //获取应用列表权限
        hasPermissionToReadNetworkStats()
        //获取悬浮窗权限
        if (!Settings.canDrawOverlays(this@MainActivity)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE_OVERLAY)
        }
        //获取弹框权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW), REQUEST_SYS_ALERT)
        }
//        //startService
//        //在MainActivity onStart的时候开启一个service,这个service安排指定的任务在指定的演示后开始进行重复的固定速率的执行
//        if(!mBound) {
        Intent(this, BackgroundService::class.java).also { intent ->
            startService(intent)
            Log.d("Main Activity: onStart", "trying to bind")
        }
//        }

        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Log.d("BCKGRND", "can't draw overlay")
        }

        if (!(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
            val intent = Intent(
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        Log.d("MainActivity", "OnResume")
        super.onResume()

        sharedPreferences = getSharedPreferences("changeTheme", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("changed", false)){
               val tmp = getSharedPreferences("redGreenBlue", Context.MODE_PRIVATE)
            val red = tmp.getInt("red", 43)
            val green = tmp.getInt("green", 44)
            val blue = tmp.getInt("blue", 48)
            val editor = sharedPreferences.edit()
            editor.putBoolean("changed", false)
            editor.apply()
            ThemeColors.setNewThemeColor(this, red, green, blue)
        }

        //获得启动该activity的intent对象
        val myIntent: Intent = intent
        if (myIntent.getIntExtra("RequestCode", -1) == REQUEST_OPEN_TIMER_FRG && !jumped) {
            findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
            bottomNavigationView.selectedItemId = R.id.bottomNavFocusBtn
            jumped = true
        }

        if(!mBound) {
            Intent(this, BackgroundService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
                Log.d("Main Activity: onResume", "trying to bind")
            }
        }
        //连上的时候说一声 // WHY?
        val i = Intent()
        i.setAction("Connnecting")
        i.putExtra("connect", true)
        sendBroadcast(i)

        GlobalScope.launch(Dispatchers.IO) {
            try{
                WeatherData.updateWeather()
            } catch (e: UnknownHostException) {
                // Ignore
            }

            Handler(Looper.getMainLooper()).post {
                if(WeatherData.weatherID != -1) {
                    findViewById<TextView>(R.id.mainPage_tempStr)?.text = "${String.format("%.0f", WeatherData.temperature)}°C"
                    findViewById<TextView>(R.id.mainPage_weatherStr)?.text = WeatherData.weatherDesc
                    findViewById<ImageView>(R.id.mainPage_weatherIcon)?.setImageDrawable(
                        mapOf(
                            "01d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_wb_sunny_24, null),
                            "01n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_wb_sunny_24, null),
                            "02d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_partly_cloudy, null),
                            "02n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_partly_cloudy, null),
                            "03d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_wb_cloudy_24, null),
                            "03n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_wb_cloudy_24, null),
                            "04d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_wb_cloudy_24, null),
                            "04n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_wb_cloudy_24, null),
                            "09d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_pouring, null),
                            "09n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_pouring, null),
                            "10d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_pouring, null),
                            "10n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_pouring, null),
                            "11d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_lightning, null),
                            "11n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_lightning, null),
                            "13d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_snowy_heavy, null),
                            "13n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_snowy_heavy, null),
                            "50d" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_fog, null),
                            "50n" to ResourcesCompat.getDrawable(resources, R.drawable.ic_weather_fog, null),
                        )[WeatherData.weatherIcon]
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "OnPause")
        try {
            unbindService(connection)
        } catch (ex: java.lang.IllegalArgumentException) {
            ;
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createNotificationChannel(c: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "事务提醒"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(PARAM_ALARM_UID, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(c, NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun hasPermissionToReadNetworkStats(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true
        }
        requestReadNetworkStats()
        return false
    }

    // 打开“有权查看使用情况的应用”页面
    private fun requestReadNetworkStats() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun receiveBroadCast(){
        val intentFilter = IntentFilter()
        intentFilter.addAction("switchToFocusFragment")
        this.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("MainActivity", "Received intent from background service")
//                findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
//                bottomNavigationView.selectedItemId = R.id.bottomNavFocusBtn
            }
        }, intentFilter)
    }

    private fun getTimeInMills(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        return calendar.timeInMillis
    }
}

class FocusReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Receiver", "received")
        Log.d("Receiver", "uid: ${intent?.getLongExtra(PARAM_START_FOCUS_FROM_BACKGROUND, -1)}")
        val binder = peekService(context, Intent(context, BackgroundService::class.java)) as BackgroundService.MyBinder?
        if(binder == null) {
            Intent(context, BackgroundService::class.java).also { intent ->
                context?.startService(intent)
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            val entry = MainPageEventList.DAO.getEvent(
                intent!!.getLongExtra(
                    PARAM_START_FOCUS_FROM_BACKGROUND,
                    -1
                )
            )
            //抓紧时间写一下表
            val mt = MyFocusEntry(
                uid = System.currentTimeMillis(),
                totalFocusTime = entry.stopTime - entry.startTime,
                focusDate = System.currentTimeMillis(),
                entry.title,
                false
            )
            MainPageEventList.DAO.addFocusData(mt)
            Log.d("YYYYYYes", "write")
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("FocusReceiver", "Starting Timer...")
                binder?.setUseCustomWhiteList(entry.hasCustomWhiteList)
                binder?.setCustomWhiteList(entry.customWhiteList)
                binder?.setIsBlocking(true)
                binder?.startTimer(entry)
            }, 500)
        }
    }
}

const val PARAM_ALARM_UID = "indi.hitszse2020g6.wakeapp.PARAM_ALARM_UID"
class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Receiver", "received, uid = ${intent!!.getLongExtra(PARAM_ALARM_UID, -1)}")
        context!!.startActivity(Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(PARAM_ALARM_UID, intent.getLongExtra(PARAM_ALARM_UID, -1))
        })

        val entry = MainPageEventList.DAO.getEvent(
            intent!!.getLongExtra(
                PARAM_ALARM_UID,
                -1
            )
        )
        var reminderContext = findReminder(entry)
        if(reminderContext == ""){
            reminderContext = " "
        }
        val notificationBuilder = NotificationCompat.Builder(context, PARAM_ALARM_UID)
            .setSmallIcon(R.mipmap.launcher_icon)
            .setContentTitle(entry.title)
            .setContentText(reminderContext)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify(intent!!.getLongExtra(PARAM_ALARM_UID, -1).toInt(), notificationBuilder.build())
        }
    }
    private fun findReminder(entry: EventTableEntry):String{
        val nowTime = Calendar.getInstance().timeInMillis
        var closestReminder = entry.reminder[0]
        for(i in entry.reminder.indices){
            if(!entry.isAffair && 1000*(entry.startTime - entry.reminder[i].delta) <= nowTime){
                if(kotlin.math.abs(nowTime - 1000*(entry.startTime - entry.reminder[i].delta)) < kotlin.math.abs(
                        nowTime - 1000*(entry.startTime - closestReminder.delta))){
                    closestReminder = entry.reminder[i]
                }
            }
            else if(entry.isAffair && 1000*(entry.stopTime - entry.reminder[i].delta) <= nowTime){
                if(kotlin.math.abs(nowTime - 1000*(entry.stopTime - entry.reminder[i].delta)) < kotlin.math.abs(
                        nowTime - 1000*(entry.stopTime - closestReminder.delta))){
                    closestReminder = entry.reminder[i]
                }
            }
        }
        return closestReminder.description
    }
}