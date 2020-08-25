package com.ks.loader;

import android.app.Application;
import android.content.Context;

import com.ks.loader_host_library.BuildConfig;

public class RePluginInternal {

    public static final boolean FOR_DEV = BuildConfig.DEBUG;

    // FIXME 不建议缓存Application对象，容易导致InstantRun失效（警告中写着，具体原因待分析）
    static Context sAppContext;

    static void init(Application app) {
        sAppContext = app;
    }

    /**
     * 获取宿主注册时的Context对象
     */
    public static Context getAppContext() {
        return sAppContext;
    }

    /**
     * 获取宿主注册时的ClassLoader
     */
    public static ClassLoader getAppClassLoader() {
        return getAppContext().getClassLoader();
    }
}
