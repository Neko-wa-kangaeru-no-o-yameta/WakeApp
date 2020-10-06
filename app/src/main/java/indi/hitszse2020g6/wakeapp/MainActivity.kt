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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
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

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as BackgroundService.MyBinder
            blockAppService = binder.getService()
            mBound = true

            when(intent.action) {
                ACTION_START_FOCUS_TIME-> {
                    binder.startCountDownTimer(intent.getLongExtra(PARAM_START_FOCUS_TIME, 0))
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
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

        when(intent.action) {
            ACTION_START_FOCUS_TIME-> {
                Log.d("MainActivity", "Load alarm request @ ${System.currentTimeMillis()}")
            }
        }
    }

    override fun onStart() {
        super.onStart()

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
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
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
        super.onResume()
        //获得启动该activity的intent对象
        val myIntent: Intent = intent
        if (myIntent.getIntExtra("RequestCode", -1) == REQUEST_OPEN_TIMER_FRG) {
            findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
            bottomNavigationView.selectedItemId = R.id.bottomNavFocusBtn
        }
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
        Log.d("MainActivity", "Received intent from background service")
        val intentFilter = IntentFilter()
        intentFilter.addAction("change_page")
        this.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                findNavController(R.id.mainNavFragment).navigate(R.id.action_global_focusFragment)
                bottomNavigationView.selectedItemId = R.id.bottomNavFocusBtn
                val bundle = intent.extras
                //后台通知前台开始计时

                val t = bundle?.getLong("change_page_data")
                //等一会儿跳转过去再计时
                Log.d("BEFORE_T",t.toString())
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(object:Runnable{
                    override fun run() {
                        if (t != null) {
                            binder.startCountDownTimer(t)
                        }
                    }
                },500)
            }
        }, intentFilter)
    }
}