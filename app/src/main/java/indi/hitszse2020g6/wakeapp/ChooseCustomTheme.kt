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
                "theme_grey"->{
                    red = 244
                    green = 245
                    blue = 249
                }
                "theme_white"->{
                    red = 255
                    green = 255
                    blue = 255
                }
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
        }
        theme_cancel.setOnClickListener {
            finish()
        }

        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("ThemeColors", Context.MODE_PRIVATE)
        val stringColor = sharedPreferences.getString("color", "2d2d2d")
        when (stringColor) {
            "f0f0ff" -> {
                choosedTheme = "theme_grey"
                clickFunc(theme_grey, getDrawable(R.drawable.shape_grey_selected))
            }
            "ffffff" -> {
                choosedTheme = "theme_white"
                clickFunc(theme_white, getDrawable(R.drawable.shape_white_selected))
            }
            "0fd2c3" -> {
                choosedTheme = "theme_green"
                clickFunc(theme_green, getDrawable(R.drawable.shape_green_selected))
            }
            "ffe15a" -> {
                choosedTheme = "theme_yellow"
                clickFunc(theme_yellow, getDrawable(R.drawable.shape_yellow_selected))
            }
            "ff87a5" -> {
                choosedTheme = "theme_pink"
                clickFunc(theme_pink, getDrawable(R.drawable.shape_pink_selected))
            }
            "693ce1" -> {
                choosedTheme = "theme_purple"
                clickFunc(theme_purple, getDrawable(R.drawable.shape_purple_selected))
            }
            "1e4b81" -> {
                choosedTheme = "theme_blue"
                clickFunc(theme_blue, getDrawable(R.drawable.shape_blue_selected))
            }
            "ff6978" -> {
                choosedTheme = "theme_red"
                clickFunc(theme_red, getDrawable(R.drawable.shape_red_selected))
            }
            "2d2d2d" -> {
                choosedTheme = "theme_black"
                clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
            }
            else -> {
                choosedTheme = "theme_black"
                clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
            }
        }

        theme_grey.setOnClickListener {
            choosedTheme = "theme_grey"
            clickFunc(theme_grey, getDrawable(R.drawable.shape_grey_selected))
        }

        theme_white.setOnClickListener {
            choosedTheme = "theme_white"
            clickFunc(theme_white, getDrawable(R.drawable.shape_white_selected))
        }

        theme_green.setOnClickListener {
            choosedTheme = "theme_green"
            clickFunc(theme_green, getDrawable(R.drawable.shape_green_selected))
        }

        theme_yellow.setOnClickListener {
            choosedTheme = "theme_yellow"
            clickFunc(theme_yellow, getDrawable(R.drawable.shape_yellow_selected))
        }

        theme_pink.setOnClickListener {
            choosedTheme = "theme_pink"
            clickFunc(theme_pink, getDrawable(R.drawable.shape_pink_selected))
        }

        theme_purple.setOnClickListener {
            choosedTheme = "theme_purple"
            clickFunc(theme_purple, getDrawable(R.drawable.shape_purple_selected))
        }

        theme_blue.setOnClickListener {
            choosedTheme = "theme_blue"
            clickFunc(theme_blue, getDrawable(R.drawable.shape_blue_selected))
        }

        theme_red.setOnClickListener {
            choosedTheme = "theme_red"
            clickFunc(theme_red, getDrawable(R.drawable.shape_red_selected))
        }

        theme_black.setOnClickListener {
            choosedTheme = "theme_black"
            clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
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