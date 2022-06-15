package com.dreamlibrary.storyapp.util;

import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;

public class Utils {

    public static Boolean isShowing = false;
    private static CustomDialog customDialog;
    private static String TAG = Utils.class.getSimpleName();


    public static void show(Context context) {
        if (!isShowing) {
            isShowing = true;
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            customDialog.setCanceledOnTouchOutside(false);
            customDialog.setContentView(R.layout.custom_dialog_progress);
            ImageView iv = customDialog.findViewById(R.id.iv_bookLoading);
            Glide.with(context).load("https://androidphotos.fra1.digitaloceanspaces.com/ebooks/v.2/res/raw/book_load.gif").into(iv);
            Window window = customDialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);
            customDialog.show();
        }
    }

    public static void dismiss() {
        if (isShowing) {
            isShowing = false;
            try {
                customDialog.dismiss();
            } catch (Exception e) {
                log("Exception",e.getMessage());
            }
        }
    }

    public static void toast(Context context, String toastmsg) {
        Toast.makeText(context, toastmsg, Toast.LENGTH_SHORT).show();
    }

    public static void log(String TAG, String content) {
        Log.d(TAG, "" + content);
    }


}
