/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ks.loader;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.ks.loader.utils.LogRelease;
import com.ks.loader.utils.ReflectUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 宿主的ClassLoader，插件框架的核心之一
 * <p>
 * 注意：为了兼容Android 7.0以上的LoadedApk.updateApplicationInfo中，对addDexPath方法的依赖，
 * 特将继承关系调整到PathClassLoader，以前是ClassLoader
 *
 * @author RePlugin Team
 */
public class RePluginClassLoader extends PathClassLoader {

    private static final String TAG = "RePluginClassLoader";

    private final String DEX_PATH = Environment.getExternalStorageState() + File.separator + "loaderdex/modelDex.dex";

    private final ClassLoader mOrig;

    /**
     * 用load系列代替
     */
    //private Method findClassMethod;

    private Method findResourceMethod;

    private Method findResourcesMethod;

    private Method findLibraryMethod;

    private Method getPackageMethod;
    private DexClassLoader dexClassLoader;

    public RePluginClassLoader(ClassLoader parent, ClassLoader orig) {

        // 由于PathClassLoader在初始化时会做一些Dir的处理，所以这里必须要传一些内容进来
        // 但我们最终不用它，而是拷贝所有的Fields
        super("", "", parent);
        mOrig = orig;

        // 将原来宿主里的关键字段，拷贝到这个对象上，这样骗系统以为用的还是以前的东西（尤其是DexPathList）
        // 注意，这里用的是“浅拷贝”
        // Added by Jiongxuan Zhang
        copyFromOriginal(orig);

        initMethods(orig);

        // 加载自己DexClassLoader
        installModelDexClassLoader(parent);
    }

    private void installModelDexClassLoader(ClassLoader parent) {
        Context appContext = RePluginInternal.getAppContext();
        String cacheDir = appContext.getCacheDir().getAbsolutePath();
        dexClassLoader = new DexClassLoader(DEX_PATH, cacheDir, cacheDir, parent);
        LogRelease.e(TAG, "加载dexClassLoader:" + dexClassLoader);
    }

    private void initMethods(ClassLoader cl) {
        Class<?> c = cl.getClass();
        findResourceMethod = ReflectUtils.getMethod(c, "findResource", String.class);
        findResourceMethod.setAccessible(true);
        findResourcesMethod = ReflectUtils.getMethod(c, "findResources", String.class);
        findResourcesMethod.setAccessible(true);
        findLibraryMethod = ReflectUtils.getMethod(c, "findLibrary", String.class);
        findLibraryMethod.setAccessible(true);
        getPackageMethod = ReflectUtils.getMethod(c, "getPackage", String.class);
        getPackageMethod.setAccessible(true);
    }

    private void copyFromOriginal(ClassLoader orig) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Android 2.2 - 2.3.7，有一堆字段，需要逐一复制
            // 以下方法在较慢的手机上用时：8ms左右
            copyFieldValue("libPath", orig);
            copyFieldValue("libraryPathElements", orig);
            copyFieldValue("mDexs", orig);
            copyFieldValue("mFiles", orig);
            copyFieldValue("mPaths", orig);
            copyFieldValue("mZips", orig);
        } else {
            // Android 4.0以上只需要复制pathList即可
            // 以下方法在较慢的手机上用时：1ms
            copyFieldValue("pathList", orig);
        }
    }

    private void copyFieldValue(String field, ClassLoader orig) {
        try {
            Field f = ReflectUtils.getField(orig.getClass(), field);
            if (f == null) {
                LogRelease.e(TAG, "rpcl.cfv: null! f=" + field);
                return;
            }

            // 删除final修饰符
            ReflectUtils.removeFieldFinalModifier(f);

            // 复制Field中的值到this里
            Object o = ReflectUtils.readField(f, orig);
            ReflectUtils.writeField(f, this, o);
        } catch (IllegalAccessException e) {
            LogRelease.e(TAG, "rpcl.cfv: fail! f=" + field);
        }
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        // 先从插件中找
        if (dexClassLoader != null) {
            try {
                c = dexClassLoader.loadClass(className);
                if (c != null) {
                    LogRelease.e(TAG, "dexClassLoader" + c);
                    return c;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        // 再从原始的去找
        try {
            c = mOrig.loadClass(className);
            LogRelease.e(TAG, "mOrig" + c);
            return c;
        } catch (Throwable e) {
        }
        // 再往上找
        return super.loadClass(className, resolve);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        Log.d(TAG, "findClass:" + className);
        return super.findClass(className);
    }

    @Override
    protected URL findResource(String resName) {
        try {
            return (URL) findResourceMethod.invoke(mOrig, resName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return super.findResource(resName);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Enumeration<URL> findResources(String resName) {
        try {
            return (Enumeration<URL>) findResourcesMethod.invoke(mOrig, resName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return super.findResources(resName);
    }

    @Override
    public String findLibrary(String libName) {
        try {
            return (String) findLibraryMethod.invoke(mOrig, libName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return super.findLibrary(libName);
    }

    @Override
    protected Package getPackage(String name) {
        // 金立手机的某些ROM(F103,F103L,F303,M3)代码ClassLoader.getPackage去掉了关键的保护和错误处理(2015.11~2015.12左右)
        // ，会返回null
        // 悬浮窗某些draw代码触发getPackage(...).getName()，getName出现空指针解引，导致悬浮窗进程出现了大量崩溃
        // 此处实现和AOSP一致确保不会返回null
        // SONGZHAOCHUN, 2016/02/29
        if (name != null && !name.isEmpty()) {
            Package pack = null;
            try {
                pack = (Package) getPackageMethod.invoke(mOrig, name);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (pack == null) {
                pack = super.getPackage(name);
            }
            if (pack == null) {
                return definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
            }
            return pack;
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[mBase=" + mOrig.toString() + "]";
    }
}
