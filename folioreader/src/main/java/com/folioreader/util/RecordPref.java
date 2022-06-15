package com.folioreader.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class RecordPref {

    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public RecordPref(Activity activity) {
        this.activity = activity;
        this.sharedPreferences = activity.getSharedPreferences("RECORD", Context.MODE_PRIVATE);
        this.editor = this.sharedPreferences.edit();
    }

    public void countDownload(boolean x) {

        int historyCount = sharedPreferences.getInt("downloadCount", 0);


        if (x) {
            historyCount++;
        } else {
            if (historyCount != 0) {
                historyCount--;
            }
        }

        editor.putInt("downloadCount", historyCount);
        editor.apply();

    }

    public void countFavourite(boolean x) {

        int historyCount = sharedPreferences.getInt("favouriteCount", 0);

        if (x) {
            historyCount++;
        } else {
            if (historyCount != 0) {
                historyCount--;
            }
        }

        editor.putInt("favouriteCount", historyCount);
        editor.apply();

    }

    public int getDownloadCount() {
        return sharedPreferences.getInt("downloadCount", 0);
    }

    public int getFavouriteCount() {
        return sharedPreferences.getInt("favouriteCount", 0);
    }

}
