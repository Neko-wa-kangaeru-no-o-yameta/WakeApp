package indi.hitszse2020g6.wakeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_choose_custom_theme.*

class ChooseCustomTheme : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_custom_theme)

        theme_confirm.setOnClickListener {
            finish()
        }
        theme_cancel.setOnClickListener {
            finish()
        }
    }
}