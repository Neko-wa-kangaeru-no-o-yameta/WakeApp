package indi.hitszse2020g6.wakeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help_guide.*

class HelpGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeColors(this)
        setContentView(R.layout.activity_help_guide)
        help_back.setOnClickListener {
            finish()
        }
    }
}