package com.ks.loaderdex

import android.app.Application
import android.content.Context
import com.ks.loader.PatchClassLoaderUtils

class App : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // 注入替换classLoader
        PatchClassLoaderUtils.patch(this)
    }
}
