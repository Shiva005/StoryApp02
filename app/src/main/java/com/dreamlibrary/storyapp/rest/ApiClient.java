package com.dreamlibrary.storyapp.rest;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.Utils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    public static final String BASE_URL = Constant.getUrl();
    private static Retrofit retrofit = null;
    private static final String TAG = "ApiClient";

    private static final int MY_SOCKET_TIMEOUT_MS = 5000;
    private static final String SECRET_KEY = "sk_test_51KiwJfSJPs3PY82HyaVyCt5x79laOCiD1jDscwMlCquT3S9nNYHt2khTCdNe3hiLqse88Z6QxDEEgd5In553XtlH00eONrfd1g";

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void postStringMethod(final Context context, final String url, Map<String, String> params, final VolleyStringCallback volleyCallback) {
        Utils.log(TAG, params.toString());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    Log.d(TAG, "onResponse: " + url + ",response:" + response);
                    Utils.dismiss();
                    volleyCallback.onSuccess(response);
                }, error -> {
            Log.d(TAG, "onResponse: " + url + ",onErrorResponse:" + error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SECRET_KEY);
                headers.put("Stripe-Version", "2020-08-27");
                return headers;
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
