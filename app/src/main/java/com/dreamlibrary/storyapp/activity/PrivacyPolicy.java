package com.dreamlibrary.storyapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.response.PrivacyPolicyRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.BannerAds;
import com.dreamlibrary.storyapp.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.util.Utils;

import org.jetbrains.annotations.NotNull;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrivacyPolicy extends AppCompatActivity {

    private Method method;
    public MaterialToolbar toolbar;
    private WebView webView;
    private ConstraintLayout conNoData;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        method = new Method(PrivacyPolicy.this);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_privacy_policy);
        toolbar.setTitle(getResources().getString(R.string.privacy_policy));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_right_align);

        conNoData = findViewById(R.id.con_noDataFound);
        webView = findViewById(R.id.webView_pp);

        conNoData.setVisibility(View.GONE);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_privacy_policy);
        BannerAds.showBannerAds(PrivacyPolicy.this,linearLayout);

        if (method.isNetworkAvailable()) {
            privacyPolicy();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    public void privacyPolicy() {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(PrivacyPolicy.this));
        jsObj.addProperty("method_name", "app_privacy_policy");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<PrivacyPolicyRP> call = apiService.getPrivacyPolicy(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<PrivacyPolicyRP>() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onResponse(@NotNull Call<PrivacyPolicyRP> call, @NotNull Response<PrivacyPolicyRP> response) {

                try {
                    PrivacyPolicyRP privacyPolicyRP = response.body();
                    assert privacyPolicyRP != null;

                    if (privacyPolicyRP.getStatus().equals("1")) {

                        webView.setBackgroundColor(Color.TRANSPARENT);
                        webView.setFocusableInTouchMode(false);
                        webView.setFocusable(false);
                        webView.getSettings().setDefaultTextEncodingName("UTF-8");
                        webView.getSettings().setJavaScriptEnabled(true);
                        String mimeType = "text/html";
                        String encoding = "utf-8";

                        String text = "<html dir=" + method.isWebViewTextRtl() + "><head>"
                                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/quicksand_regular.ttf\")}body{font-family: MyFont;color: " + method.webViewText() + "line-height:1.6}"
                                + "a {color:" + method.webViewLink() + "text-decoration:none}"
                                + "</style></head>"
                                + "<body>"
                                + privacyPolicyRP.getApp_privacy_policy()
                                + "</body></html>";

                        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(privacyPolicyRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<PrivacyPolicyRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                conNoData.setVisibility(View.VISIBLE);
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
