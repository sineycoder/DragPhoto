package com.siney.dragphoto;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author: siney
 * Date: 2019/6/28
 * description:
 * 用来处理权限相关设置以及打开系统相关请求页面
 */
public class PermissionUtils {

    public static boolean checkAllPermission(Activity activity){
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        List<String> list = new ArrayList<>();
        for(String permission:permissions){
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                list.add(permission);
            }
        }
        if (list.size() > 0) {
            ActivityCompat.requestPermissions(activity, list.toArray(new String[list.size()]), 1);
            return false;
        }else{
            return true;
        }
    }

}
