package indi.hitszse2020g6.wakeapp

import android.Manifest
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
import kotlinx.android.synthetic.main.activity_main.*


const val INTENT_AFFAIR_DETAIL = 1
const val INTENT_SCHEDULE_DETAIL = 2
const val REQUEST_SETTING_EVENT = 3

class MainActivity : AppCompatActivity() {

    var mBound = false
    lateinit var blockAppService: BlockAppService
    lateinit var binder: BlockAppService.MyBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as BlockAppService.MyBinder
            blockAppService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        Intent(this, BlockAppService::class.java).also { intent ->
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
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
}