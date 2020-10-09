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
import androidx.navigation.findNavController
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_choose_white_list.*
import kotlinx.android.synthetic.main.fragment_focus_statistic.view.*
import kotlinx.android.synthetic.main.fragment_focus_statistic.view.textView
import kotlinx.android.synthetic.main.my_app_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

const val REQUEST_SELECT_WHITE_LIST = 123
const val RESULT_OK = 100

class ChooseWhiteListActivity : AppCompatActivity(),CompoundButton.OnCheckedChangeListener {

    private val TAG:String = "ChooseWhiteListActivity"
    private var myWhiteList:MutableList<String> = mutableListOf()
    private lateinit var mySharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_white_list)
        longTimeMethod()

        white_list_confirm.setOnClickListener{
            for(item in myWhiteList){
                Log.d(TAG,item)
            }


            //如果是lgz申请的
            var myIntent = intent
            var b: Bundle? = myIntent.extras
            if(b!!.get("REQUEST_CODE")== REQUEST_SELECT_WHITE_LIST){
                myIntent = intent
                var gson = Gson()
                var myString = gson.toJson(myWhiteList)
                b.putString("customized_white_list", myString)
                myIntent.putExtras(b)
                this.setResult(RESULT_OK,myIntent)
                this.finish()
            }else{
                //用户通过设置页面打开的
                var jsonArray = convertIntoJsonArray()
                mySharedPreferences = getSharedPreferences("user_default_white_list", Context.MODE_PRIVATE)
                var editor = mySharedPreferences.edit()
                editor.putString("default_white_list",jsonArray.toString())
                editor.commit()
                this.finish()
            }
        }

        white_list_cancel.setOnClickListener {
            finish()
        }
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
                val insertPoint = findViewById<ViewGroup>(R.id.linearContainer)
                for(item in appList){
                    val vi = applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v: View = vi.inflate(R.layout.my_app_item_layout, null)
                    v.app_name.text = item.appName
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
        var myParent = (buttonView?.parent) as ViewGroup
        myWhiteList.add(myParent.getChildAt(7).hideTextView.text.toString())
//        for(itemPos in 0 until myParent.childCount){
//            if(myParent.getChildAt(itemPos).hideTextView!=null){
//                Log.d(TAG,"$itemPos ${myParent.getChildAt(itemPos).hideTextView.text}")
//            }
//        }
    }

    fun convertIntoJsonArray(): JSONArray {
        var jsonArray:JSONArray = JSONArray()
        for(item in myWhiteList){
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