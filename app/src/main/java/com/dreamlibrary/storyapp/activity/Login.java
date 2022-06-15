package com.dreamlibrary.storyapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.response.LoginRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;

import org.jetbrains.annotations.NotNull;

import cn.refactor.library.SmoothCheckBox;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    public static final String mypreference = "mypref";
    public static final String pref_email = "pref_email";
    public static final String pref_password = "pref_password";
    public static final String pref_check = "pref_check";
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private Method method;
    private SmoothCheckBox checkBox;
    private TextInputEditText editTextEmail, editTextPassword;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        method = new Method(Login.this);
        method.forceRTLIfSupported();

        pref = getSharedPreferences(mypreference, 0); // 0 - for private mode
        editor = pref.edit();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        editTextEmail = findViewById(R.id.editText_email_login);
        editTextPassword = findViewById(R.id.editText_password_login);

        MaterialButton buttonLogin = findViewById(R.id.button_login);
        ImageView ivSkip = findViewById(R.id.iv_skip_login);
        MaterialTextView textViewSignUp = findViewById(R.id.textView_signUp_login);
        MaterialTextView textViewFp = findViewById(R.id.textView_fp_login);
        ImageView welcomeBack = findViewById(R.id.iv_welcome_back);

        checkBox = findViewById(R.id.checkbox_login);
        checkBox.setChecked(false);

        Glide.with(this).load("https://androidphotos.fra1.digitaloceanspaces.com/ebooks/v.2/res/raw/welcome_back.gif").into(welcomeBack);

        if (pref.getBoolean(pref_check, false)) {
            editTextEmail.setText(pref.getString(pref_email, null));
            editTextPassword.setText(pref.getString(pref_password, null));
            checkBox.setChecked(true);
        } else {
            editTextEmail.setText("");
            editTextPassword.setText("");
            checkBox.setChecked(false);
        }

        checkBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked) {
                editor.putString(pref_email, editTextEmail.getText().toString());
                editor.putString(pref_password, editTextPassword.getText().toString());
                editor.putBoolean(pref_check, true);
            } else {
                editor.putBoolean(pref_check, false);
            }
            editor.commit();
        });

        buttonLogin.setOnClickListener(v -> login());

        textViewSignUp.setOnClickListener(v -> {
            Method.loginBack = false;
            startActivity(new Intent(Login.this, Register.class));
        });

        ivSkip.setOnClickListener(v -> {
            super.onBackPressed();
        });

        textViewFp.setOnClickListener(v -> {
            Method.loginBack = false;
            startActivity(new Intent(Login.this, ForgetPassword.class));
        });

    }


    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void login() {

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        editTextEmail.setError(null);
        editTextPassword.setError(null);

        if (!isValidMail(email) || email.isEmpty()) {
            editTextEmail.requestFocus();
            editTextEmail.setError(getResources().getString(R.string.please_enter_email));
        } else if (password.isEmpty()) {
            editTextPassword.requestFocus();
            editTextPassword.setError(getResources().getString(R.string.please_enter_password));
        } else {

            editTextEmail.clearFocus();
            editTextPassword.clearFocus();
            imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {
                login(email, password);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    public void login(final String sendEmail, final String sendPassword) {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Login.this));
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("method_name", "user_login");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<LoginRP> call = apiService.getLogin(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<LoginRP>() {
            @Override
            public void onResponse(@NotNull Call<LoginRP> call, @NotNull Response<LoginRP> response) {

                try {
                    LoginRP loginRP = response.body();
                    assert loginRP != null;

                    if (loginRP.getStatus().equals("1")) {

                        if (loginRP.getSuccess().equals("1")) {

                            method.editor.putBoolean(method.pref_login, true);
                            method.editor.putString(method.profileId, loginRP.getUser_id());
                            method.editor.putString(method.profileName, loginRP.getName());
                            method.editor.putString(method.userImage, loginRP.getUser_image());
                            method.editor.putString(method.loginType, "normal");
                            method.editor.apply();

                            Toast.makeText(Login.this, loginRP.getMsg(), Toast.LENGTH_SHORT).show();

                            if (checkBox.isChecked()) {
                                editor.putString(pref_email, editTextEmail.getText().toString());
                                editor.putString(pref_password, editTextPassword.getText().toString());
                                editor.putBoolean(pref_check, true);
                                editor.commit();
                            }

                            editTextEmail.setText("");
                            editTextPassword.setText("");

                            /*if (Method.loginBack) {
                                Method.loginBack = false;
                                Events.Login loginNotify = new Events.Login("");
                                GlobalBus.getBus().post(loginNotify);
                                Toast.makeText(Login.this, "1", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            } else {*/
                                startActivity(new Intent(Login.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finishAffinity();
                            //}

                        } else {
                            method.alertBox(loginRP.getMsg());
                        }

                    } else {
                        method.alertBox(loginRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<LoginRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

