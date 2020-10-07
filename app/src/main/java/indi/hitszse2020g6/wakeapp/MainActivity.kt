package indi.hitszse2020g6.wakeapp

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import indi.hitszse2020g6.wakeapp.mainPage.PARAM_START_FOCUS_FROM_BACKGROUND
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


const val INTENT_AFFAIR_DETAIL = 1
const val INTENT_SCHEDULE_DETAIL = 2
const val REQUEST_SETTING_EVENT = 3
const val REQUEST_ALARM = 4

const val PARAM_START_FOCUS_TIME = "indi.hitszse2020g6.wakeapp.paramStartFocus"
const val ACTION_START_FOCUS_TIME = "indi.hitszse2020g6.wakeapp.actionStartFocus"
const val ACTION_START_ALARM = "indi.hitszse2020g6.wakeapp.actionStartAlarm"

class MainActivity : AppCompatActivity() {

    var mBound = false
    lateinit var blockAppService: BackgroundService
    lateinit var binder: BackgroundService.MyBinder
    var jumped = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as BackgroundService.MyBinder
            blockAppService = binder.getService()
            mBound = true
            Log.d("MainActivity","Connected")

            when(intent.action) {
                ACTION_START_FOCUS_TIME-> {
                    binder.startCountDownTimer(intent.getLongExtra(PARAM_START_FOCUS_TIME, 0),"自定义专注")
//                    binder.startCountDownTimer(intent.getLongExtra(PARAM_START_FOCUS_TIME, 0))
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("MainActivity","disconnected")
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainPageEventList.DAO = AppRoomDB.getDataBase(this).getDAO()
        MainPageEventList.getEventListFromDB()
        MainPageEventList.context = this
        MainPageEventList.alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
                    binder.setUseCustomWhiteList(entry.hasCustomWhiteList)
                    binder.setCustomWhiteList(entry.customWhiteList)
                    binder.setIsBlocking(true)
                    binder.changePage(entry.startTime, entry.stopTime, entry.title)
//                    binder.startCoutnDownTimer()
                }, 500)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity","Onstart")

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
        //startService
        //在MainActivity onStart的时候开启一个service,这个service安排指定的任务在指定的演示后开始进行重复的固定速率的执行
        Intent(this, BackgroundService::class.java).also { intent ->
            startService(intent)
//            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d("B Main Activity", "trying to bind")
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Log.d("BCKGRND", "can't draw overlay")
        }
    }

    override fun onResume() {
        Log.d("MainActivity","OnResume")
        super.onResume()

        //获得启动该activity的intent对象
        val myIntent: Intent = intent
        if (myIntent.getIntExtra("RequestCode", -1) == REQUEST_OPEN_TIMER_FRG && !jumped) {
            findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
            bottomNavigationView.selectedItemId = R.id.bottomNavFocusBtn
            jumped = true
        }

        Intent(this, BackgroundService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d("B Main Activity", "trying to bind")

            //连上的时候说一声
            var myIntent = Intent()
            myIntent.setAction("Connnecting")
            myIntent.putExtra("connect",true)
            sendBroadcast(myIntent)
        }
    }

    override fun onPause() {
        super.onPause()
        unbindService(connection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    fun receiveBroadCast(){
        val intentFilter = IntentFilter()
        intentFilter.addAction("change_page")
        this.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("MainActivity", "Received intent from background service")
                findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
                bottomNavigationView.selectedItemId = R.id.bottomNavFocusBtn
                val bundle = intent.extras
                //后台通知前台开始计时

                var t = bundle?.getLong("change_page_data")
                var title: String? = bundle?.getString("change_page_title")
                //等一会儿跳转过去再计时
                Log.d("BEFORE_T",t.toString())
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(object:Runnable{
                    override fun run() {
                        if (t != null&& title!=null) {
                            binder.startCountDownTimer(t,title)
                        }
                    }
                },500)
            }
        }, intentFilter)
    }
}

class FocusReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Receiver", "received")
        Toast.makeText(context, "receiver received!", Toast.LENGTH_SHORT).show()
        Log.d("Receiver", "uid: ${intent?.getLongExtra(PARAM_START_FOCUS_FROM_BACKGROUND, -1)}")
        context!!.startActivity(Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("RequestCode", REQUEST_OPEN_TIMER_FRG)
            putExtra(PARAM_START_FOCUS_FROM_BACKGROUND, intent?.getLongExtra(PARAM_START_FOCUS_FROM_BACKGROUND, -1))
        })
    }
}

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Receiver", "received")
        Toast.makeText(context, "receiver received!", Toast.LENGTH_SHORT).show()
        context!!.startActivity(Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("RequestCode", REQUEST_OPEN_TIMER_FRG)
        })
    }
}