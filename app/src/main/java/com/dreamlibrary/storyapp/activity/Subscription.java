package com.dreamlibrary.storyapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.adapter.SubscriptionAdapter;
import com.dreamlibrary.storyapp.item.SubscriptionModel;
import com.dreamlibrary.storyapp.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Subscription extends AppCompatActivity implements PaymentResultListener {

    private final String SECRET_KEY = "sk_test_51KiwJfSJPs3PY82HyaVyCt5x79laOCiD1jDscwMlCquT3S9nNYHt2khTCdNe3hiLqse88Z6QxDEEgd5In553XtlH00eONrfd1g";
    private String PUBLISH_KEY = "pk_test_51KiwJfSJPs3PY82HAjbKiChgJp0GvB0awtRoSsbdNr9gw7qlCQHwuEwQvpiPiKj9dkWNzdVbP49ixfRcYKiPnk3200SEAAKUGl";
    private String customerUrl = "https://api.stripe.com/v1/customers";
    private String ephemeralUrl = "https://api.stripe.com/v1/ephemeral_keys";
    private String paymentIntentUrl = "https://api.stripe.com/v1/payment_intents";

    private PaymentSheet paymentSheet;
    private RecyclerView planRecView;
    private ImageView backPress;
    private String customerID;
    private String ephemeralKey;
    private String clientSecret;

    private String TAG = "SubscriptionAct";
    String intentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        planRecView = findViewById(R.id.rec_premiumPlans);
        backPress = findViewById(R.id.iv_backPress);

        intentType = getIntent().getStringExtra("type");

        backPress.setOnClickListener(v -> super.onBackPressed());
        PaymentConfiguration.init(this, PUBLISH_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        List<SubscriptionModel> subscriptionModelList = new ArrayList<>();
        subscriptionModelList.add(new SubscriptionModel("Starter", "100", "1 Week", "Banner Ads", "Interstitial Ads", "Video Ads"));
        subscriptionModelList.add(new SubscriptionModel("Silver", "200", "1 Month", "Banner Ads", "Interstitial Ads", "Video Ads"));
        subscriptionModelList.add(new SubscriptionModel("Gold", "500", "3 Month", "Banner Ads", "Interstitial Ads", "Video Ads"));
        subscriptionModelList.add(new SubscriptionModel("Platinum", "900", "6 Month", "Banner Ads", "Interstitial Ads", "Video Ads"));
        subscriptionModelList.add(new SubscriptionModel("Diamond", "1500", "12 Month", "Banner Ads", "Interstitial Ads", "Video Ads"));

        SubscriptionAdapter subscriptionAdapter = new SubscriptionAdapter(this, subscriptionModelList, intentType);
        planRecView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        planRecView.setLayoutManager(layoutManager);
        planRecView.setItemAnimator(new DefaultItemAnimator());
        planRecView.setAdapter(subscriptionAdapter);
        getCustomerID();
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Utils.toast(this, "Success");
            Utils.dismiss();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Utils.toast(this, "Cancelled");
            Utils.dismiss();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Utils.dismiss();
            Utils.toast(this, "Failed with message: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError());
        }
    }

    private boolean getCustomerID() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                customerUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        customerID = jsonObject.optString("id");
                        getEphemeralKey();
                        //Utils.toast(this, customerID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Utils.dismiss();
            Log.d(TAG, "onErrorResponse:" + error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SECRET_KEY);
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        return true;
    }

    private void getEphemeralKey() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                ephemeralUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        ephemeralKey = jsonObject.optString("id");
                        //Utils.toast(this, ephemeralKey);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Utils.dismiss();
            Log.d(TAG, "onErrorResponse:" + error);
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
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void getClientSecret(int subsAmt) {
        Utils.show(this);
        if (getCustomerID()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    paymentIntentUrl,
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            clientSecret = jsonObject.optString("client_secret");
                            startNonIndianPayment();
                            Utils.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                Utils.dismiss();
                Log.d(TAG, "onErrorResponse:" + error);
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + SECRET_KEY);
                    return headers;
                }

                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("customer", customerID);
                    params.put("amount", subsAmt + "00");
                    params.put("currency", "usd");
                    params.put("automatic_payment_methods[enabled]", "true");
                    params.put("description", "for sales and purchase of pre");
                    //params.put("requires_capture", "true");
                    //params.put("payment_method_types", "card");
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }

    private void startNonIndianPayment() {
        paymentSheet.presentWithPaymentIntent(
                clientSecret, new PaymentSheet.Configuration(
                        getString(R.string.app_name),
                        new PaymentSheet.CustomerConfiguration(
                                customerID,
                                ephemeralKey)
                ));
    }

    //razor pay payment gateway
    public void startPayment(double amt) {

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_PChanjaebJfLBx");

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amt * 100); // amount in the smallest currency unit
            orderRequest.put("currency", "INR");
            orderRequest.put("contact", "9939853761");
            orderRequest.put("email", "shiukumar05@gmail.com");
            orderRequest.put("receipt", "order_rcptid_11");
            orderRequest.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            orderRequest.put("payment_capture", true);

            checkout.open(this, orderRequest);

        } catch (JSONException e) {
            // Handle Exception
            System.out.println(e.getMessage());
        }

       /* try {
            JSONObject options = new JSONObject();
            options.put("name", "Bringr");
            options.put("contact", "9056237684");
            options.put("email", "mdohid7430@gmail.com");
            //options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //options.put("description", order_reference_id);
            //options.put("order_id", "order_DgZ26rHjbzLLY2");
            options.put("currency", "INR");

            // Amount is always passed in currency subunits
            // Eg: "500" = INR 5.00

            // options.put("amount", payAmount * 100);

            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }*/
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(this, "" + s, Toast.LENGTH_SHORT).show();
        Utils.dismiss();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "" + s, Toast.LENGTH_SHORT).show();
        Utils.dismiss();
    }
}