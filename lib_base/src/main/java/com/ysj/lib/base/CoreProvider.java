package com.ysj.lib.base;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.core.content.FileProvider;

/**
 * 用于初始化一些基本内容并提供全局 Application
 * <p>
 *
 * @author Ysj
 */
public class CoreProvider extends FileProvider {

    public static String authorities = "";

    private static Application mApplication;

    @Override
    public boolean onCreate() {
        mApplication = (Application) getContext();
        authorities = mApplication.getPackageName() + ".core.provider";
        return true;
    }

    public static Application getApplication() {
        return mApplication;
    }

    /**
     * 通过 Application 的 context 打开一个 Activity </br>
     * 会自动添加 {@linkplain Intent#FLAG_ACTIVITY_NEW_TASK}
     *
     * @param intent Intent
     */
    public static void startActivity(Intent intent) {
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }

    public static Resources getRes() {
        return mApplication.getResources();
    }

    /**
     * 获取 SharedPreferences <br>
     *
     * @see Application#getSharedPreferences(String, int)
     */
    public static SharedPreferences getSP(String name, int mode) {
        return mApplication.getSharedPreferences(name, mode);
    }
}
