package com.ks.loader;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.ks.loader.utils.FileUtil;
import com.ks.loader.utils.ReflectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


/**
 * 对宿主的HostClassLoader做修改。这是RePlugin中唯一需要修改宿主私有属性的位置了
 *
 * @author RePlugin Team
 */
public class PatchClassLoaderUtils {

    private static final String TAG = "PatchClassLoaderUtils";
    private static final String DEX_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "loaderdex/modelDex.dex";

    public static boolean patch(Application application) {
        try {
            RePluginInternal.init(application);

            Context oBase = application.getBaseContext();
            if (oBase == null) {
                return false;
            }

            Object oPackageInfo = ReflectUtils.readField(oBase, "mPackageInfo");
            if (oPackageInfo == null) {
                return false;
            }

            // 获取mPackageInfo.mClassLoader
            ClassLoader oClassLoader = (ClassLoader) ReflectUtils.readField(oPackageInfo, "mClassLoader");
            if (oClassLoader == null) {
                return false;
            }
            ArrayList<File> dexFiles = new ArrayList<File>();
            dexFiles.add(new File(DEX_PATH));
            SystemClassLoaderAdder.installDexes(application, oClassLoader, application.getFilesDir(), dexFiles);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



}
