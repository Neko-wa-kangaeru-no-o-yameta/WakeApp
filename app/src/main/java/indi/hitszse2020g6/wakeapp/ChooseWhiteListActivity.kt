package indi.hitszse2020g6.wakeapp

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_choose_white_list.*
import kotlinx.android.synthetic.main.fragment_focus_statistic.view.*
import kotlinx.android.synthetic.main.fragment_focus_statistic.view.textView
import kotlinx.android.synthetic.main.my_app_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseWhiteListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_white_list)
        longTimeMethod()
    }

    private fun longTimeMethod(){
        //Dispatchers 指定协程运行在Android的哪个线程里
        GlobalScope.launch(Dispatchers.IO){
            var appList = ArrayList<AppInfo>()
            var packages = packageManager.getInstalledPackages(0)
            for(i in 0..(packages.size-1)){
                var packageInfo = packages.get(i)
                var tmpInfo = AppInfo()
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                //非系统应用
                if((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM)==0){
                    appList.add(tmpInfo)
                }
            }
            //切换线程更新UI
            withContext(Dispatchers.Main){
                val insertPoint = findViewById(R.id.linearContainer) as ViewGroup
                for(item in appList){
                    val vi = applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v: View = vi.inflate(R.layout.my_app_item_layout, null)
                    if(v == null){
                        Log.d("ShowAppActivity", "ooops")
                    }else{
                        v.textView.text = item.appName
                        v.imageView.setImageDrawable(item.appIcon)
                        //动态生成ID
                        v.id = View.generateViewId()
                        insertPoint.addView(v)
                    }
                }
                show_message.visibility = View.GONE
                Log.d("GGG","FINISHED")
            }
        }
    }
}

private class AppInfo{
    var appName = ""
    var appIcon: Drawable? = null
}