package com.dreamlibrary.storyapp.util;

import android.util.Base64;

import com.dreamlibrary.storyapp.BuildConfig;
import com.dreamlibrary.storyapp.response.AppRP;

public class Constant {

    //Change WebView text color light and dark mode
    public static String webViewText = "#8b8b8b;";
    public static String webViewTextDark = "#a5a5a5;";

    //Change WebView link color light and dark mode
    public static String webViewLink = "#0782C1;";
    public static String webViewLinkDark = "#5387ED;";

    public static int AD_COUNT = 0;
    public static int AD_COUNT_SHOW = 0;

    public static AppRP appRP;

    public static String getUrl(){

        String url = "";
        try {
            byte[] data = Base64.decode(BuildConfig.My_api, Base64.DEFAULT);
            url = new String(data, "UTF-8");
        }catch (Exception e0){
            e0.printStackTrace();
        }

        return url;
    }

}
