package com.capsane.simplecamera;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by capsane on 18-7-14.
 *
 */

public class Globals {

    private static final String TAG = "Globals";

    public static String GlobalSaveDirName;
    public static String GlobalSaveDirPath;

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public Globals(String GlobalSaveDirPath) {

    }

    public static void initSaveDir(File externalFiles) {
        Date date = new Date(System.currentTimeMillis());
        GlobalSaveDirName = FORMAT.format(date);
        GlobalSaveDirPath = externalFiles + File.separator + GlobalSaveDirName;
        Log.e(TAG, "初始化文件夹为时间： " + GlobalSaveDirName);
        Log.e(TAG, "文件夹： " + GlobalSaveDirPath);
    }

}
