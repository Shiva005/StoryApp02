package com.dreamlibrary.storyapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.ContactUs;
import com.dreamlibrary.storyapp.activity.Faq;
import com.dreamlibrary.storyapp.activity.Landingpage;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.activity.Subscription;
import com.dreamlibrary.storyapp.response.ProfileRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Method;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment {
    private Method method;
    private ImageView imageView;
    private MaterialTextView textViewName;
    private LinearLayout lastReading, downloads, favourites;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.profile_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.profile));
        }

        lastReading = view.findViewById(R.id.ll_me_lastReading);
        downloads = view.findViewById(R.id.ll_me_downloads);
        favourites = view.findViewById(R.id.ll_me_favourites);

        lastReading.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new ContinueFragment(),
                    getResources().getString(R.string.continues)).addToBackStack(getResources().getString(R.string.continue_book)).commitAllowingStateLoss();
            MainActivity.mainTitle.setText("Your last readings");
        });
        downloads.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new DownloadFragment(),
                    getResources().getString(R.string.download)).addToBackStack(getResources().getString(R.string.download)).commitAllowingStateLoss();
            MainActivity.mainTitle.setText("Download list");
        });
        favourites.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new FavouriteFragment(),
                    getResources().getString(R.string.favorite)).addToBackStack(getResources().getString(R.string.favorite)).commitAllowingStateLoss();
            MainActivity.mainTitle.setText("Favourite books list");
        });

        method = new Method(getActivity());
        method.forceRTLIfSupported();
        callProfileData();

        settingScreen(view);
        return view;

    }

    private void callProfileData() {
        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                profile(method.userId());
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    public void profile(String userId) {

        if (getActivity() != null) {
            MainActivity.mainTitle.setText("My Profile");
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("method_name", "user_profile");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ProfileRP> call = apiService.getProfile(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<ProfileRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<ProfileRP> call, @NotNull Response<ProfileRP> response) {

                    if (getActivity() != null) {

                        try {
                            ProfileRP profileRP = response.body();
                            assert profileRP != null;

                            if (profileRP.getStatus().equals("1")) {

                                method.editor.putString(method.userImage, profileRP.getUser_profile());
                                method.editor.commit();

                                Glide.with(getActivity().getApplicationContext()).load(profileRP.getUser_profile())
                                        .placeholder(R.drawable.profile).into(imageView);

                                method.editor.putString(method.profileName, profileRP.getName());
                                method.editor.apply();
                                textViewName.setText(profileRP.getName());

                            } else if (profileRP.getStatus().equals("2")) {
                                method.suspend(profileRP.getMessage());
                            } else {
                                method.alertBox(profileRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }
                }

                @Override
                public void onFailure(@NotNull Call<ProfileRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    method.alertBox(getContext().getResources().getString(R.string.failed_try_again));
                }
            });
        }
    }


    private void settingScreen(View view) {

        LinearLayout writeMyStory = view.findViewById(R.id.con_write_my_story);
        LinearLayout premiumPlus = view.findViewById(R.id.con_premium_plus_setting);
        LinearLayout conContactUs = view.findViewById(R.id.con_contactUs_setting);
        LinearLayout conFaq = view.findViewById(R.id.con_faq_setting);
        LinearLayout conShareApp = view.findViewById(R.id.con_shareApp_setting);
        LinearLayout conRateApp = view.findViewById(R.id.con_rateApp_setting);
        LinearLayout conUserDetails = view.findViewById(R.id.con_user_detail);
        LinearLayout conAppSetting = view.findViewById(R.id.con_app_Setting);
        imageView = view.findViewById(R.id.imageView_pro);
        textViewName = view.findViewById(R.id.textView_name_pro);

        conAppSetting.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new SettingFragment(),
                    getResources().getString(R.string.settings)).addToBackStack("Profile").commitAllowingStateLoss();
        });
        writeMyStory.setOnClickListener(v -> {
            method.alertBox("This feature is coming soon...");
        });

        premiumPlus.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                //showCustomSelectionDialog();
                method.alertBox("This feature is coming soon...");
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        conContactUs.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                startActivity(new Intent(getActivity(), ContactUs.class));
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        conFaq.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                startActivity(new Intent(getActivity(), Faq.class));
            } else {
                method.alertBox(getString(R.string.internet_connection));
            }
        });
        conShareApp.setOnClickListener(v -> shareApp());
        conRateApp.setOnClickListener(v -> rateApp());
        conUserDetails.setOnClickListener(v -> login());

    }

    private void showCustomSelectionDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.item_choose_premium_selection);

        LinearLayout chooseIndian = dialog.findViewById(R.id.ll_premiumPlansIndian);
        LinearLayout chooseNonIndian = dialog.findViewById(R.id.ll_premiumPlansNonIndian);
        Button dialogButton = dialog.findViewById(R.id.btn_cancel);

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        dialogButton.setOnClickListener(v -> dialog.dismiss());
        chooseIndian.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Subscription.class).putExtra("type", "indian"));
            dialog.dismiss();
        });
        chooseNonIndian.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), Subscription.class).putExtra("type", "non_indian"));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void login() {
        if (method.isLogin()) {
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                    new EditProfileFragment(), getResources().getString(R.string.edit_profile))
                    .addToBackStack(getResources().getString(R.string.edit_profile)).commitAllowingStateLoss();
        } else {
            startActivity(new Intent(requireActivity(), Landingpage.class));
        }
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getApplication().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getApplication().getPackageName())));
        }
    }

    private void shareApp() {
        try {
            String string = getResources().getString(R.string.Let_me_recommend_you_this_application) + "\n\n" + "https://play.google.com/store/apps/details?id=" + getActivity().getApplication().getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, string);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_one)));

        } catch (Exception e) {
            Log.d("app_data", "");
        }
    }
}
