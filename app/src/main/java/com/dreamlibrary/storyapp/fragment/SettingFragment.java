package com.dreamlibrary.storyapp.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.onesignal.OneSignal;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.Landingpage;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.activity.PrivacyPolicy;
import com.dreamlibrary.storyapp.activity.SplashScreen;
import com.dreamlibrary.storyapp.activity.TermsConditions;
import com.dreamlibrary.storyapp.util.Method;

import org.jetbrains.annotations.NotNull;

public class SettingFragment extends Fragment {

    private Method method;
    private String themMode;
    private MaterialTextView textView_cache_size;
    private MaterialButton btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.setting_fragment, container, false);
        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.settings));
        }

        method = new Method(getActivity());

        SwitchMaterial switchMaterial = view.findViewById(R.id.switch_setting);
        MaterialTextView textViewThemType = view.findViewById(R.id.textView_themType_setting);

        LinearLayout conThem = view.findViewById(R.id.con_them_setting);
        MaterialTextView appVersion = view.findViewById(R.id.textView_app_version_about_us);
        LinearLayout linearLayoutPP = view.findViewById(R.id.con_privacy_policy_setting);
        LinearLayout termsCondition = view.findViewById(R.id.con_terms_and_condition_setting);
        MaterialButton button_clear_cache = view.findViewById(R.id.button_clear_cache_setting);
        btnLogout = view.findViewById(R.id.btn_logout);
        textView_cache_size = view.findViewById(R.id.textView_cache_size);
        textView_cache_size.setText(Method.getCache(getContext()));
        if (method.isLogin()) {
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            btnLogout.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                logout();
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        linearLayoutPP.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                startActivity(new Intent(getContext(), PrivacyPolicy.class));
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        termsCondition.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                startActivity(new Intent(getContext(), TermsConditions.class));
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        button_clear_cache.setOnClickListener(v -> {
            boolean b = Method.deleteCache(getContext());
            if (b) {
                textView_cache_size.setText(Method.getCache(getContext()));
                Toast.makeText(getContext(), "Cache Cleared!!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
            }
            onResume();
        });


        switchMaterial.setChecked(method.pref.getBoolean(method.notification, true));

        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            OneSignal.unsubscribeWhenNotificationsAreDisabled(isChecked);
            method.editor.putBoolean(method.notification, isChecked);
            method.editor.commit();
        });

        try {
            PackageInfo pInfo = this.getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            appVersion.setText("Version "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        switch (method.themMode()) {
            case "system":
                textViewThemType.setText(getResources().getString(R.string.system_default));
                break;
            case "light":
                textViewThemType.setText(getResources().getString(R.string.light));
                break;
            case "dark":
                textViewThemType.setText(getResources().getString(R.string.dark));
                break;
        }

        conThem.setOnClickListener(v -> {

            Dialog dialog = new Dialog(requireActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialogbox_them);
            if (method.isRtl()) {
                dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
            dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup_them);
            MaterialTextView textViewOk = dialog.findViewById(R.id.textView_ok_them);
            MaterialTextView textViewCancel = dialog.findViewById(R.id.textView_cancel_them);

            switch (method.themMode()) {
                case "system":
                    radioGroup.check(radioGroup.getChildAt(0).getId());
                    break;
                case "light":
                    radioGroup.check(radioGroup.getChildAt(1).getId());
                    break;
                case "dark":
                    radioGroup.check(radioGroup.getChildAt(2).getId());
                    break;
            }

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                MaterialRadioButton rb = group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    switch (checkedId) {
                        case R.id.radioButton_system_them:
                            themMode = "system";
                            break;
                        case R.id.radioButton_light_them:
                            themMode = "light";
                            break;
                        case R.id.radioButton_dark_them:
                            themMode = "dark";
                            break;
                        default:
                            break;
                    }
                }
            });

            textViewOk.setOnClickListener(vOk -> {
                method.editor.putString(method.themSetting, themMode);
                method.editor.commit();
                dialog.dismiss();

                startActivity(new Intent(getActivity(), SplashScreen.class));
                getActivity().finishAffinity();

            });

            textViewCancel.setOnClickListener(vCancel -> dialog.dismiss());

            dialog.show();

        });

        setHasOptionsMenu(false);
        return view;
    }

    public void logout() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTitleTextStyle);
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.logout_message));
        builder.setPositiveButton(getResources().getString(R.string.logout),
                (arg0, arg1) -> {
                    if (method.getLoginType().equals("google")) {

                        // Configure sign-in to request the ic_user_login's ID, email address, and basic
                        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build();

                        // Build a GoogleSignInClient with the options specified by gso.
                        //Google login
                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

                        mGoogleSignInClient.signOut()
                                .addOnCompleteListener(getActivity(), task -> {
                                    method.editor.putBoolean(method.pref_login, false);
                                    method.editor.commit();
                                    startActivity(new Intent(getContext(), Landingpage.class));
                                    this.getActivity().finishAffinity();
                                });
                    } else if (method.getLoginType().equals("facebook")) {
                        LoginManager.getInstance().logOut();
                        method.editor.putBoolean(method.pref_login, false);
                        method.editor.commit();
                        startActivity(new Intent(getContext(), Landingpage.class));
                        this.getActivity().finishAffinity();
                    } else {
                        method.editor.putBoolean(method.pref_login, false);
                        method.editor.commit();
                        startActivity(new Intent(getContext(), Landingpage.class));
                        this.getActivity().finishAffinity();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                (dialogInterface, i) -> {

                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

}
