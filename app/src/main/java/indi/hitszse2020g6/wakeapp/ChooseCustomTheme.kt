package indi.hitszse2020g6.wakeapp

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.db.williamchart.extensions.getDrawable
import kotlinx.android.synthetic.main.activity_choose_custom_theme.*

class ChooseCustomTheme : AppCompatActivity() {

    private var beforeDawable: Drawable? = null
    private var beforeSelected:View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_custom_theme)

        theme_confirm.setOnClickListener {
            finish()
        }
        theme_cancel.setOnClickListener {
            finish()
        }

        theme_grey.setOnClickListener {
            clickFunc(theme_grey,getDrawable(R.drawable.shape_grey_selected))
        }

        theme_white.setOnClickListener {
            clickFunc(theme_white,getDrawable(R.drawable.shape_white_selected))
        }

        theme_green.setOnClickListener {
            clickFunc(theme_green,getDrawable(R.drawable.shape_green_selected))
        }

        theme_yellow.setOnClickListener {
            clickFunc(theme_yellow,getDrawable(R.drawable.shape_yellow_selected))
        }

        theme_pink.setOnClickListener {
            clickFunc(theme_pink,getDrawable(R.drawable.shape_pink_selected))
        }

        theme_purple.setOnClickListener {
            clickFunc(theme_purple,getDrawable(R.drawable.shape_purple_selected))
        }

        theme_blue.setOnClickListener {
            clickFunc(theme_blue,getDrawable(R.drawable.shape_blue_selected))
        }

        theme_red.setOnClickListener {
            clickFunc(theme_red,getDrawable(R.drawable.shape_red_selected))
        }

        theme_black.setOnClickListener {
            clickFunc(theme_black,getDrawable(R.drawable.shape_black_selected))
        }
    }

    private fun clickFunc(btn:View,selectDrawable: Drawable?){
        //把之前点的那个恢复一下
        if(beforeSelected!=null){
            beforeSelected!!.background = beforeDawable
        }
        //设置现在这个
        beforeDawable = btn.background
        beforeSelected = btn
        btn.background = selectDrawable
    }
}