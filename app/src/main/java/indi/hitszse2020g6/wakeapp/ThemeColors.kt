package indi.hitszse2020g6.wakeapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt


class ThemeColors(context: Context) {
    @ColorInt
    var color: Int

    // Checking if title text color will be black
    private val isLightActionBar: Boolean
        private get() { // Checking if title text color will be black
            val rgb: Int = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
            return rgb > 210
        }

    companion object {
        private const val NAME = "ThemeColors"
        private const val KEY = "color"
        fun setNewThemeColor(activity: Activity, red: Int, green: Int, blue: Int) {
            var red = red
            var green = green
            var blue = blue
            val colorStep = 15
            red = Math.round(red / colorStep.toFloat()) * colorStep
            green = Math.round(green / colorStep.toFloat()) * colorStep
            blue = Math.round(blue / colorStep.toFloat()) * colorStep
            val stringColor = Integer.toHexString(Color.rgb(red, green, blue)).substring(2)
            val editor = activity.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            editor.putString(KEY, stringColor)
            editor.apply()
            activity.recreate()
        }
    }

    init {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val stringColor = sharedPreferences.getString(KEY, "004bff")
        color = Color.parseColor("#$stringColor")
//        if (isLightActionBar) context.setTheme(R.style.AppTheme)
        Log.d("Theme Colors",stringColor!!)
        context.setTheme(
            context.getResources()
                .getIdentifier("T_$stringColor", "style", context.getPackageName())
        )
    }
}