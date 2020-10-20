package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList.context
import kotlinx.android.synthetic.main.activity_choose_custom_theme.*
import java.util.*

class ChooseCustomTheme : AppCompatActivity() {

    private val TAG:String = "ChooseCustomTheme"
    private var beforeDawable: Drawable? = null
    private var beforeSelected: View? = null
    private var choosedTheme: String? = "theme_black"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"OnCreate")
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_choose_custom_theme)

        theme_confirm.setOnClickListener {
            var red:Int = 0
            var green:Int = 0
            var blue:Int = 0
            when(choosedTheme){
                "theme_green"->{
                    red = 8
                    green = 212
                    blue = 196
                }
                "theme_yellow"->{
                    red = 253
                    green = 229
                    blue = 95
                }
                "theme_pink"->{
                    red = 255
                    green = 132
                    blue = 164
                }
                "theme_purple"->{
                    red = 112
                    green = 62
                    blue = 223
                }
                "theme_blue"->{
                    red = 25
                    green = 77
                    blue = 132
                }
                "theme_red"->{
                    red = 255
                    green = 103
                    blue = 118
                }
                "theme_black"->{
                    red = 43
                    green = 44
                    blue = 48
                }
            }
            ThemeColors.setNewThemeColor(this, red, green, blue)
            var editor = getSharedPreferences("redGreenBlue", Context.MODE_PRIVATE).edit()
            editor.putInt("red", red)
            editor.putInt("green",green)
            editor.putInt("blue",blue)
            editor.apply()
            editor = getSharedPreferences("changeTheme", Context.MODE_PRIVATE).edit()
            editor.putBoolean("changed",true)
            editor.commit()
        }
        theme_cancel.setOnClickListener {
            finish()
        }

        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("ThemeColors", Context.MODE_PRIVATE)
        val stringColor = sharedPreferences.getString("color", "2d2d2d")
        Log.d(TAG,stringColor!!)
        when (stringColor) {
            "0fd2c3" -> {
                choosedTheme = "theme_green"
                clickFunc(theme_green, getDrawable(R.drawable.shape_green_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_green))
            }
            "ffe15a" -> {
                choosedTheme = "theme_yellow"
                clickFunc(theme_yellow, getDrawable(R.drawable.shape_yellow_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_yellow))
            }
            "ff87a5" -> {
                choosedTheme = "theme_pink"
                clickFunc(theme_pink, getDrawable(R.drawable.shape_pink_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_pink))
            }
            "693ce1" -> {
                choosedTheme = "theme_purple"
                clickFunc(theme_purple, getDrawable(R.drawable.shape_purple_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_purple))
            }
            "1e4b87" -> {
                choosedTheme = "theme_blue"
                clickFunc(theme_blue, getDrawable(R.drawable.shape_blue_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_blue))
            }
            "ff6978" -> {
                choosedTheme = "theme_red"
                clickFunc(theme_red, getDrawable(R.drawable.shape_red_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_red))
            }
            "2d2d2d" -> {
                choosedTheme = "theme_black"
                clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_black))
            }
            else -> {
                choosedTheme = "theme_black"
                clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
                show_page.setImageDrawable(getDrawable(R.drawable.page_black))
            }
        }

        theme_green.setOnClickListener {
            choosedTheme = "theme_green"
            clickFunc(theme_green, getDrawable(R.drawable.shape_green_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_green))
        }

        theme_yellow.setOnClickListener {
            choosedTheme = "theme_yellow"
            clickFunc(theme_yellow, getDrawable(R.drawable.shape_yellow_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_yellow))
        }

        theme_pink.setOnClickListener {
            choosedTheme = "theme_pink"
            clickFunc(theme_pink, getDrawable(R.drawable.shape_pink_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_pink))
        }

        theme_purple.setOnClickListener {
            choosedTheme = "theme_purple"
            clickFunc(theme_purple, getDrawable(R.drawable.shape_purple_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_purple))
        }

        theme_blue.setOnClickListener {
            choosedTheme = "theme_blue"
            clickFunc(theme_blue, getDrawable(R.drawable.shape_blue_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_blue))
        }

        theme_red.setOnClickListener {
            choosedTheme = "theme_red"
            clickFunc(theme_red, getDrawable(R.drawable.shape_red_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_red))
        }

        theme_black.setOnClickListener {
            choosedTheme = "theme_black"
            clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
            show_page.setImageDrawable(getDrawable(R.drawable.page_black))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"OnResume")
    }

    private fun clickFunc(btn: View, selectDrawable: Drawable?) {
        //把之前点的那个恢复一下
        if (beforeSelected != null) {
            beforeSelected!!.background = beforeDawable
        }
        //设置现在这个
        beforeDawable = btn.background
        beforeSelected = btn
        btn.background = selectDrawable
    }
}