package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import indi.hitszse2020g6.wakeapp.eventDetail.PARAM_SCHEDULE_DETAIL_TO_WHITELIST_JSON
import indi.hitszse2020g6.wakeapp.eventDetail.PARAM_WHITELIST_TO_SCHEDULE_DETAIL_JSON
import indi.hitszse2020g6.wakeapp.eventDetail.ScheduleDetailActivity
import kotlinx.android.synthetic.main.activity_choose_white_list.*
import kotlinx.android.synthetic.main.my_app_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray


class ChooseWhiteListActivity : AppCompatActivity(),CompoundButton.OnCheckedChangeListener {

    private val TAG:String = "ChooseWhiteListActivity"
    private var myWhiteList:MutableList<String> = mutableListOf()
    private lateinit var mySharedPreferences: SharedPreferences
    private var tmpList:MutableList<String> = mutableListOf()
    private var openFlag:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_choose_white_list)

        var myIntent = intent
        var b:Bundle? = myIntent.extras
        //庚宝请求的
        if(b!=null && b.getString(PARAM_SCHEDULE_DETAIL_TO_WHITELIST_JSON)!=null){
            Log.d(TAG,"GENGBAO!!!")
            openFlag = true
            //获得庚宝发送的字符串
            var jsonStr = b.getString(PARAM_SCHEDULE_DETAIL_TO_WHITELIST_JSON)
            //解析成list
            myWhiteList = Json.decodeFromString(jsonStr!!)
        }else{
            // 用户自行打开设置的，读之前的默认设置
            Log.d(TAG,"USER!!!")
            mySharedPreferences = getSharedPreferences("user_default_white_list", Context.MODE_PRIVATE)
            var jsonArray = JSONArray(mySharedPreferences.getString("default_white_list","[]"))
            Log.d(TAG,"${jsonArray.length()}")
            if(jsonArray.length()>0){
                for(item in 0 until jsonArray.length()){
                    myWhiteList.add(jsonArray.get(item) as String)
                    Log.d(TAG,myWhiteList[item])
                }
            }
        }
        //读应用列表
        longTimeMethod()

        white_list_confirm.setOnClickListener{
            for(item in myWhiteList){
                Log.d(TAG,item)
            }
            //庚宝申请的，返回给庚宝
            if(openFlag){
                Log.d(TAG,"GENGBAO!!!")
                val str = Json.encodeToString(myWhiteList)
                Log.d(TAG, "$str.")
                myIntent.putExtra(PARAM_WHITELIST_TO_SCHEDULE_DETAIL_JSON,str)
                setResult(RESULT_OK,myIntent)
                this.finish()
            }else{
                //用户自己打开的
                Log.d(TAG,"USER!!!")
                val jsonArray = convertIntoJsonArray()
                mySharedPreferences = getSharedPreferences("user_default_white_list", Context.MODE_PRIVATE)
                val editor = mySharedPreferences.edit()
                editor.putString("default_white_list",jsonArray.toString())
                editor.commit()
            }
            finish()
        }

        white_list_cancel.setOnClickListener {
            if(openFlag){
                //返回给庚宝一个空的
                var tmpList:List<String> = emptyList()
                val str = Json.encodeToString(tmpList)
                Log.d(TAG, "$str.")
                myIntent.putExtra(PARAM_WHITELIST_TO_SCHEDULE_DETAIL_JSON,str)
                setResult(RESULT_OK,myIntent)
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("changeTheme", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("changed",false)){
            val tmp = getSharedPreferences("redGreenBlue",Context.MODE_PRIVATE)
            val red = tmp.getInt("red",43)
            val green = tmp.getInt("green",44)
            val blue = tmp.getInt("blue",48)
            val editor = sharedPreferences.edit()
            editor.putBoolean("changed",false)
            editor.apply()
            ThemeColors.setNewThemeColor(this,red,green,blue)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onStart")
    }

    private fun longTimeMethod(){
        //Dispatchers 指定协程运行在Android的哪个线程里
        GlobalScope.launch(Dispatchers.IO){
            val appList = ArrayList<AppInfo>()
            val packages = packageManager.getInstalledPackages(0)
            for(i in 0 until packages.size){
                val packageInfo = packages.get(i)
                val tmpInfo = AppInfo()
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                tmpInfo.appPackageName = packageInfo.applicationInfo.packageName
                //非系统应用
                if((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM)==0){
                    appList.add(tmpInfo)
                }
            }
            //切换线程更新UI
            withContext(Dispatchers.Main){
                Log.d(TAG,"LONG_TIME_METHOD")
                val insertPoint = findViewById<ViewGroup>(R.id.linearContainer)
                for(item in appList){
                    val vi = applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v: View = vi.inflate(R.layout.my_app_item_layout, null)
                    v.app_name.text = item.appName
                    Log.d(TAG,tmpList.size.toString())
                    if(item.appPackageName in myWhiteList){
                        v.switch_white_list.isChecked = true
                    }
                    v.imageView.setImageDrawable(item.appIcon)
                    v.hideTextView.text = item.appPackageName
                    v.switch_white_list.setOnCheckedChangeListener(this@ChooseWhiteListActivity)
                    //动态生成ID
                    v.id = View.generateViewId()
                    insertPoint.addView(v)
                }
                show_message.visibility = View.GONE
                Log.d("GGG","FINISHED")
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val myParent = (buttonView?.parent) as ViewGroup
        if(buttonView.isChecked){
            myWhiteList.add(myParent.getChildAt(7).hideTextView.text.toString())
        }else{
            //如果之前在白名单里，移除
            if(myParent.getChildAt(7).hideTextView.text.toString() in myWhiteList){
                myWhiteList.remove(myParent.getChildAt(7).hideTextView.text.toString())
            }
        }
    }

    fun convertIntoJsonArray(): JSONArray {
        val jsonArray:JSONArray = JSONArray()
        for(item in myWhiteList){
            Log.d(TAG,item)
            jsonArray.put(item)
        }
        return jsonArray
    }
}

private class AppInfo{
    var appName = ""
    var appIcon: Drawable? = null
    var appPackageName = ""
}