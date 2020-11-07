package com.ysj.lib.base.utils;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.ysj.lib.base.CoreProvider;


/**
 * 方便显示不同时长的Toast
 * <p>
 *
 * @author Ysj
 */
public class ToastUtil {

    /**
     * 显示 Toast.LENGTH_SHORT
     *
     * @param text 要显示的text
     */
    public static void showShortToast(String text) {
        Run.runOnUiThread(() -> Toast.makeText(CoreProvider.getApplication(), text, Toast.LENGTH_SHORT).show());
    }

    /**
     * 显示Toast.LENGTH_SHORT
     *
     * @param strRes 要显示的text的资源id
     */
    public static void showShortToast(@StringRes int strRes) {
        Run.runOnUiThread(() ->
        {
            Application context = CoreProvider.getApplication();
            Toast.makeText(context, context.getResources().getString(strRes), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 显示Toast.LENGTH_LONG
     *
     * @param strRes 要显示的text的资源id
     */
    public static void showLongToast(@StringRes int strRes) {
        Run.runOnUiThread(() ->
        {
            Application context = CoreProvider.getApplication();
            Toast.makeText(context, context.getResources().getString(strRes), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * 显示Toast.LENGTH_LONG
     *
     * @param text 要显示的text
     */
    public static void showLongToast(String text) {
        Run.runOnUiThread(() -> Toast.makeText(CoreProvider.getApplication(), text, Toast.LENGTH_LONG).show());
    }

}
