package indi.hitszse2020g6.wakeapp

import android.app.Application
import skin.support.SkinCompatManager
import skin.support.app.SkinAppCompatViewInflater


class MyApp:Application() {
    override fun onCreate() {
        super.onCreate()
        SkinCompatManager.withoutActivity(this)
            .addInflater(SkinAppCompatViewInflater()) // 基础控件换肤初始化
            .setSkinStatusBarColorEnable(false) // 关闭状态栏换肤，默认打开[可选]
            .setSkinWindowBackgroundEnable(false) // 关闭windowBackground换肤，默认打开[可选]
            .loadSkin()
    }
}