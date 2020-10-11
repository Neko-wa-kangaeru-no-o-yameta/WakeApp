package indi.hitszse2020g6.wakeapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_choose_custom_theme.*

class ChooseCustomTheme : AppCompatActivity() {

    private var beforeDawable: Drawable? = null
    private var beforeSelected: View? = null
    private var choosedTheme: String? = "theme_grey"
    private lateinit var mySharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_custom_theme)

        theme_confirm.setOnClickListener {
            mySharedPreferences = getSharedPreferences("user_theme", Context.MODE_PRIVATE)
            var editor = mySharedPreferences.edit()
            editor.putString("theme", choosedTheme)
            editor.commit()
            finish()
        }
        theme_cancel.setOnClickListener {
            finish()
        }

        mySharedPreferences = getSharedPreferences("user_theme", Context.MODE_PRIVATE)
        var tmp = mySharedPreferences.getString("theme", "")
        when (tmp) {
            "theme_grey" -> clickFunc(theme_grey, getDrawable(R.drawable.shape_grey_selected))
            "theme_white" -> clickFunc(theme_white, getDrawable(R.drawable.shape_white_selected))
            "theme_green" -> clickFunc(theme_green, getDrawable(R.drawable.shape_green_selected))
            "theme_yellow" -> clickFunc(theme_yellow, getDrawable(R.drawable.shape_yellow_selected))
            "theme_pink" -> clickFunc(theme_pink, getDrawable(R.drawable.shape_pink_selected))
            "theme_purple" -> clickFunc(theme_purple, getDrawable(R.drawable.shape_purple_selected))
            "theme_blue" -> clickFunc(theme_blue, getDrawable(R.drawable.shape_blue_selected))
            "theme_red" -> clickFunc(theme_red, getDrawable(R.drawable.shape_red_selected))
            "theme_black" -> clickFunc(theme_black, getDrawable(R.drawable.shape_black_selected))
            else -> clickFunc(theme_grey, getDrawable(R.drawable.shape_grey_selected))
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