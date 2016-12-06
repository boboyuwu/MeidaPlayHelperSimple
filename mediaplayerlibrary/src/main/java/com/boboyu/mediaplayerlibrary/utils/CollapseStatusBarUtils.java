package com.boboyu.mediaplayerlibrary.utils;

/**
 * Created by wubo on 2016/10/31.
 */
public class CollapseStatusBarUtils {
    public static void collapseStatusBar(android.content.Context context) {
        try {
             Object statusBarManager  = context.getSystemService("statusbar");
            java.lang.reflect.Method collapse;

            if (android.os.Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

}
