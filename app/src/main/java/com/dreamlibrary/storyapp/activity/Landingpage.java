package com.dreamlibrary.storyapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.response.RegisterRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Events;
import com.dreamlibrary.storyapp.util.GlobalBus;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Landingpage extends AppCompatActivity {

    private CardView emailLogin, googleLogin, facebookLogin;
    private ImageView welcomeBack;
    private MaterialButton buttonSkip;
    private MaterialTextView textViewTerms;
    private MaterialCheckBox checkBoxTerms;
    private Method method;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 007;
    private static final String EMAIL = "email";
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landingpage);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //facebook button
        callbackManager = CallbackManager.Factory.create();

        intiViews();
        initListeners();
    }

    private void initListeners() {
        Glide.with(this).load("https://androidphotos.fra1.digitaloceanspaces.com/ebooks/v.2/res/raw/welcome_back.gif").into(welcomeBack);
        emailLogin.setOnClickListener(v -> {
            if (checkBoxTerms.isChecked()) {
                startActivity(new Intent(this, Login.class));
            } else {
                method.alertBox(getResources().getString(R.string.please_select_terms));
            }
        });

        buttonSkip.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        textViewTerms.setOnClickListener(v -> startActivity(new Intent(Landingpage.this, TermsConditions.class)));

        googleLogin.setOnClickListener(v -> {
            if (checkBoxTerms.isChecked()) {
                if (method.isNetworkAvailable()) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            } else {
                method.alertBox(getResources().getString(R.string.please_select_terms));
            }
        });

        facebookLogin.setOnClickListener(v -> {
            if (checkBoxTerms.isChecked()) {
                LoginManager.getInstance().logInWithReadPermissions(Landingpage.this, Arrays.asList(EMAIL));
            } else {
                method.alertBox(getResources().getString(R.string.please_select_terms));
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbUser(loginResult);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Landingpage.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //facebook login get email and name
    private void fbUser(LoginResult loginResult) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String id = object.getString("id");
                    String name = object.getString("name");
                    String email = object.getString("email");
                    registerSocialNetwork(id, name, email, "facebook");
                } catch (JSONException e) {
                    try {
                        String id = object.getString("id");
                        String name = object.getString("name");
                        registerSocialNetwork(id, name, "", "facebook");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email"); // Parameters that we ask for facebook
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    private void intiViews() {
        method = new Method(this);
        callbackManager = CallbackManager.Factory.create();

        emailLogin = findViewById(R.id.cv_email_login);
        googleLogin = findViewById(R.id.cv_google_login);
        facebookLogin = findViewById(R.id.cv_facebook_login);
        welcomeBack = findViewById(R.id.iv_welcome_back);
        buttonSkip = findViewById(R.id.button_skip_login);
        checkBoxTerms = findViewById(R.id.checkbox_terms_login);
        textViewTerms = findViewById(R.id.textView_terms_login);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //Google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

            assert account != null;
            String id = account.getId();
            String name = account.getDisplayName();
            String email = account.getEmail();

            registerSocialNetwork(id, name, email, "google");

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    public void registerSocialNetwork(String id, String sendName, String sendEmail, String type) {

        Utils.show(this);
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Landingpage.this));
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("auth_id", id);
        jsObj.addProperty("type", type);
        jsObj.addProperty("device_id", method.getDeviceId());
        jsObj.addProperty("method_name", "user_register");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RegisterRP> call = apiService.getRegisterDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<RegisterRP>() {
            @Override
            public void onResponse(@NotNull Call<RegisterRP> call, @NotNull Response<RegisterRP> response) {

                try {
                    RegisterRP registerRP = response.body();
                    assert registerRP != null;

                    if (registerRP.getStatus().equals("1")) {

                        if (registerRP.getSuccess().equals("1")) {

                            method.editor.putBoolean(method.pref_login, true);
                            method.editor.putString(method.profileId, registerRP.getUser_id());
                            method.editor.putString(method.loginType, type);
                            method.editor.commit();

                            if (Method.loginBack) {
                                Events.Login loginNotify = new Events.Login("");
                                GlobalBus.getBus().post(loginNotify);
                                Method.loginBack = false;
                                onBackPressed();
                            } else {
                                startActivity(new Intent(Landingpage.this, MainActivity.class));
                                finishAffinity();
                            }

                        } else {
                            if (type.equals("google")) {
                                mGoogleSignInClient.signOut()
                                        .addOnCompleteListener(Landingpage.this, task -> {
                                            method.editor.putBoolean(method.pref_login, false);
                                            method.editor.commit();
                                        });
                            } else {
                                LoginManager.getInstance().logOut();
                            }
                            method.alertBox(registerRP.getMsg());
                        }

                    } else {
                        method.alertBox(registerRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<RegisterRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }
}