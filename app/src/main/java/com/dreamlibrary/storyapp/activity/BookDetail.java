package com.dreamlibrary.storyapp.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.multidex.BuildConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.bumptech.glide.Glide;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.adapter.BookHomeAdapter;
import com.dreamlibrary.storyapp.adapter.BookTagAdapter;
import com.dreamlibrary.storyapp.adapter.CommentAdapter;
import com.dreamlibrary.storyapp.interfaces.FavouriteIF;
import com.dreamlibrary.storyapp.interfaces.OnClick;
import com.dreamlibrary.storyapp.item.CommentList;
import com.dreamlibrary.storyapp.response.BookDetailRP;
import com.dreamlibrary.storyapp.response.DataRP;
import com.dreamlibrary.storyapp.response.GetReportRP;
import com.dreamlibrary.storyapp.response.MyRatingRP;
import com.dreamlibrary.storyapp.response.RatingRP;
import com.dreamlibrary.storyapp.response.UserCommentRP;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.BannerAds;
import com.dreamlibrary.storyapp.util.Constant;
import com.dreamlibrary.storyapp.util.DownloadEpub;
import com.dreamlibrary.storyapp.util.Events;
import com.dreamlibrary.storyapp.util.GlobalBus;
import com.dreamlibrary.storyapp.util.Method;
import com.dreamlibrary.storyapp.util.Utils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.folioreader.util.RecordPref;
import com.github.ornolfr.ratingview.RatingView;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetail extends AppCompatActivity {

    public MaterialToolbar toolbar;
    private OnClick onClick;
    private Method method;
    private Animation myAnim;
    private String bookId, type;
    private int rate, position = 0;
    private InputMethodManager imm;
    private RecordPref recordPref;
    private ProgressBar progressBar;
    private BookDetailRP bookDetailRP;
    private List<CommentList> commentLists;
    private CommentAdapter commentAdapter;
    private WebView webView;
    private TextInputEditText editTextComment;
    private CircleImageView imageViewUser;
    private RecyclerView recyclerViewComment, recyclerViewRelated, recyclerViewTag;
    private ConstraintLayout conNoData, conRelated, conComment;
    private LinearLayout conMain;
    private ImageView imageView, imageViewBookCover, imageViewFav, imageViewSend;
    private LinearLayout ratingLayout, llAuthorClick, llTagContainer;

    private TextView imageViewRead;
    private TextView favText;
    private MaterialTextView textViewBookName, textViewAuthor, textViewRating, textViewView, textViewRelViewAll, textViewComment, seeMore, readingStatus;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_details, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        recordPref = new RecordPref(BookDetail.this);
        GlobalBus.getBus().register(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        type = intent.getStringExtra("type");
        position = intent.getIntExtra("position", 0);

        myAnim = AnimationUtils.loadAnimation(BookDetail.this, R.anim.bounce);

        commentLists = new ArrayList<>();

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            startActivity(new Intent(BookDetail.this, BookDetail.class)
                    .putExtra("bookId", id)
                    .putExtra("position", position)
                    .putExtra("type", type));
            finish();
        };
        method = new Method(BookDetail.this, onClick);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_bookDetail);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_right_align);

        conMain = findViewById(R.id.con_main_bookDetail);
        conNoData = findViewById(R.id.con_noDataFound);
        imageView = findViewById(R.id.imageView_bookDetail);
        favText = findViewById(R.id.favText);
        imageViewBookCover = findViewById(R.id.imageView_book_bookDetail);
        imageViewRead = findViewById(R.id.imageView_read_bookDetail);
        textViewRelViewAll = findViewById(R.id.textView_relatedViewAll_bookDetail);
        textViewComment = findViewById(R.id.textView_comment_bookDetail);
        webView = findViewById(R.id.webView_bookDetail);
        textViewBookName = findViewById(R.id.textView_bookName_bookDetail);
        textViewAuthor = findViewById(R.id.textView_authorName_bookDetail);
        textViewAuthor.setSelected(true);
        progressBar = findViewById(R.id.progressBar_bookDetail);
        recyclerViewRelated = findViewById(R.id.recyclerView_related_bookDetail);
        recyclerViewComment = findViewById(R.id.recyclerView_comment_bookDetail);
        recyclerViewTag = findViewById(R.id.recyclerView_tag_bookDetail);
        conRelated = findViewById(R.id.con_related_bookDetail);
        textViewRating = findViewById(R.id.textView_ratingCount_bookDetail);
        textViewView = findViewById(R.id.textView_view_bookDetail);
        imageViewUser = findViewById(R.id.imageView_commentPro_bookDetail);
        imageViewSend = findViewById(R.id.imageView_comment_bookDetail);
        editTextComment = findViewById(R.id.editText_comment_bookDetail);
        imageViewFav = findViewById(R.id.image_favorite_bookDetail);
        conComment = findViewById(R.id.con_commentList_bookDetail);
        ratingLayout = findViewById(R.id.ratingLayout);
        llAuthorClick = findViewById(R.id.ll_author_bookDetail);
        llTagContainer = findViewById(R.id.container_tags);
        seeMore = findViewById(R.id.textView_seeMore);
        readingStatus = findViewById(R.id.text_readingStatus);

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_bookDetail);
        BannerAds.showBannerAds(BookDetail.this, linearLayout);

        if (method.isLogin()) {
            Glide.with(BookDetail.this).load(method.getUserImage())
                    .placeholder(R.drawable.profile)
                    .into(imageViewUser);
        }


        textViewBookName.setSelected(true);

        recyclerViewRelated.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRelatedBook = new LinearLayoutManager(BookDetail.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewRelated.setLayoutManager(layoutManagerRelatedBook);
        recyclerViewRelated.setFocusable(false);
        recyclerViewRelated.setNestedScrollingEnabled(false);

        recyclerViewTag.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerTag = new LinearLayoutManager(BookDetail.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTag.setLayoutManager(layoutManagerTag);
        recyclerViewTag.setFocusable(false);
        recyclerViewTag.setNestedScrollingEnabled(false);

        recyclerViewComment.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BookDetail.this);
        recyclerViewComment.setLayoutManager(layoutManager);
        recyclerViewComment.setFocusable(false);
        recyclerViewComment.setNestedScrollingEnabled(false);

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                scd(method.userId());
            } else {
                scd("0");
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Utils.dismiss();
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        llAuthorClick.setOnClickListener(v -> {
            llAuthorClick.startAnimation(myAnim);
            startActivity(new Intent(BookDetail.this, Author.class)
                    .putExtra("id", bookDetailRP.getAid()));
        });

        createNativeAd();
    }

    //For all the native Ads
    private void createNativeAd() {

        FrameLayout nativeAdContainer = findViewById(R.id.native_ad_layout);
        switch (Constant.appRP.getNative_ad_type()) {
            case "admob":

                @SuppressLint("InflateParams") View view = this.getLayoutInflater().inflate(R.layout.admob_ad, null, true);

                TemplateView templateView = view.findViewById(R.id.my_template);
                if (templateView.getParent() != null) {
                    ((ViewGroup) templateView.getParent()).removeView(templateView); // <- fix
                }
                nativeAdContainer.addView(templateView);
                AdLoader adLoader = new AdLoader.Builder(BookDetail.this, Constant.appRP.getNative_ad_id())
                        .forNativeAd(nativeAd -> {
                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder()
                                    .build();

                            templateView.setStyles(styles);
                            templateView.setNativeAd(nativeAd);

                        })
                        .build();

                AdRequest adRequest;
                if (Method.personalizationAd) {
                    adRequest = new AdRequest.Builder()
                            .build();
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("npa", "1");
                    adRequest = new AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                            .build();
                }
                adLoader.loadAd(adRequest);
                break;
            case "facebook": {
                LayoutInflater inflater = LayoutInflater.from(BookDetail.this);
                LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, nativeAdContainer, false);

                NativeAd nativeAd = new NativeAd(this, Constant.appRP.getNative_ad_id());

                // Add the AdOptionsView
                LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);

                // Create native UI using the ad metadata.
                MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
                Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

                nativeAdContainer.addView(adView);

                NativeAdListener nativeAdListener = new NativeAdListener() {
                    @Override
                    public void onMediaDownloaded(Ad ad) {
                        // Native ad finished downloading all assets
                        Log.e("status_data", "Native ad finished downloading all assets.");
                    }

                    @Override
                    public void onError(Ad ad, AdError adError) {
                        // Native ad failed to load
                        Log.e("status_data", "Native ad failed to load: " + adError.getErrorMessage());
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        // Native ad is loaded and ready to be displayed
                        Log.d("status_data", "Native ad is loaded and ready to be displayed!");
                        // Race condition, load() called again before last ad was displayed
                        if (nativeAd != ad) {
                            return;
                        }
                        // Inflate Native Ad into Container
                        Log.d("status_data", "on load" + " " + ad.toString());

                        NativeAdLayout nativeAdLayout = new NativeAdLayout(BookDetail.this);
                        AdOptionsView adOptionsView = new AdOptionsView(BookDetail.this, nativeAd, nativeAdLayout);
                        adChoicesContainer.removeAllViews();
                        adChoicesContainer.addView(adOptionsView, 0);

                        // Set the Text.
                        nativeAdTitle.setText(nativeAd.getAdvertiserName());
                        nativeAdBody.setText(nativeAd.getAdBodyText());
                        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                        // Create a list of clickable views
                        List<View> clickableViews = new ArrayList<>();
                        clickableViews.add(nativeAdTitle);
                        clickableViews.add(nativeAdCallToAction);

                        // Register the Title and CTA button to listen for clicks.
                        nativeAd.registerViewForInteraction(
                                nativeAdContainer,
                                nativeAdMedia,
                                nativeAdIcon,
                                clickableViews);
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                        // Native ad clicked
                        Log.d("status_data", "Native ad clicked!");
                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {
                        // Native ad impression
                        Log.d("status_data", "Native ad impression logged!");
                    }
                };

                // Request an ad
                nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
                break;
            }
            case "startapp": {

                LayoutInflater inflater = LayoutInflater.from(this);
                CardView adView = (CardView) inflater.inflate(R.layout.native_start_item, nativeAdContainer, false);

                nativeAdContainer.addView(adView);

                ImageView icon = adView.findViewById(R.id.icon);
                TextView title = adView.findViewById(R.id.title);
                TextView description = adView.findViewById(R.id.description);
                Button button = adView.findViewById(R.id.button);

                final StartAppNativeAd nativeAd = new StartAppNativeAd(this);

                nativeAd.loadAd(new NativeAdPreferences()
                        .setAdsNumber(1)
                        .setAutoBitmapDownload(true)
                        .setPrimaryImageSize(1), new AdEventListener() {
                    @Override
                    public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                        ArrayList<NativeAdDetails> ads = nativeAd.getNativeAds();    // get NativeAds list
                        NativeAdDetails nativeAdDetails = ads.get(0);
                        if (nativeAdDetails != null) {
                            icon.setImageBitmap(nativeAdDetails.getImageBitmap());
                            title.setText(nativeAdDetails.getTitle());
                            description.setText(nativeAdDetails.getDescription());
                            button.setText(nativeAdDetails.isApp() ? "Install" : "Open");

                            nativeAdDetails.registerViewForInteraction(adView);

                        }
                    }

                    @Override
                    public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                        if (BuildConfig.DEBUG) {
                            Log.e("onFailedToReceiveAd: ", "" + ad.getErrorMessage());
                        }
                    }
                });
                break;
            }
            case "applovins": {
                LayoutInflater inflater = LayoutInflater.from(this);
                FrameLayout nativeAdLayout = (FrameLayout) inflater.inflate(R.layout.activity_native_max_template, nativeAdContainer, false);
                MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Constant.appRP.getNative_ad_id(), this);
                nativeAdLoader.loadAd();
                nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                    @Override
                    public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                        super.onNativeAdLoaded(maxNativeAdView, maxAd);
                        // Add ad view to view.
                        nativeAdLayout.removeAllViews();
                        nativeAdLayout.addView(maxNativeAdView);
                        nativeAdContainer.addView(nativeAdLayout);
                    }

                    @Override
                    public void onNativeAdLoadFailed(String s, MaxError maxError) {
                        super.onNativeAdLoadFailed(s, maxError);
                    }

                    @Override
                    public void onNativeAdClicked(MaxAd maxAd) {
                        super.onNativeAdClicked(maxAd);
                    }
                });
                break;
            }
        }

    }

    @Subscribe
    public void getNotify(Events.AddComment comment) {
        if (bookDetailRP != null) {
            if (bookDetailRP.getId().equals(comment.getBook_id())) {
                commentLists.add(0, new CommentList(comment.getComment_id(),
                        comment.getBook_id(), comment.getUser_id(), comment.getUser_name(), comment.getUser_image(),
                        comment.getComment_text(), comment.getComment_date()));
                if (commentAdapter != null) {
                    commentAdapter.notifyDataSetChanged();
                    String textView_total = getResources().getString(R.string.view_all) + " " + "(" + comment.getTotal_comment() + ")";
                    textViewComment.setText(textView_total);
                }
            }
            if (commentLists.size() == 0) {
                conComment.setVisibility(View.GONE);
            } else {
                conComment.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe
    public void getNotify(Events.DeleteComment deleteComment) {
        if (bookDetailRP != null) {
            if (bookDetailRP.getId().equals(deleteComment.getBookId())) {
                if (textViewComment != null) {
                    String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + deleteComment.getTotalComment() + ")";
                    textViewComment.setText(buttonTotal);
                }
            }
            if (deleteComment.getType().equals("all_comment")) {
                if (bookDetailRP.getId().equals(deleteComment.getBookId())) {
                    commentLists.clear();
                    commentLists.addAll(deleteComment.getCommentLists());
                    if (commentAdapter != null) {
                        commentAdapter.notifyDataSetChanged();
                    }
                }
            }
            if (commentLists.size() == 0) {
                conComment.setVisibility(View.GONE);
            } else {
                conComment.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_download:
                if (method.isNetworkAvailable()) {
                    if (bookDetailRP.getBook_file_url().contains(".epub")) {
                        method.download(bookDetailRP.getId(),
                                bookDetailRP.getBook_title(),
                                bookDetailRP.getBook_cover_img(),
                                getAllAuthorName(bookDetailRP),
                                bookDetailRP.getBook_file_url(), "epub");
                    } else {
                        method.download(bookDetailRP.getId(),
                                bookDetailRP.getBook_title(),
                                bookDetailRP.getBook_cover_img(),
                                getAllAuthorName(bookDetailRP),
                                bookDetailRP.getBook_file_url(), "pdf");
                    }
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                break;
            case R.id.action_report:
                if (method.isLogin()) {
                    getReport(method.userId(), bookDetailRP.getId());
                } else {
                    Method.loginBack = true;
                    startActivity(new Intent(BookDetail.this, Landingpage.class));
                }
                break;
            case R.id.action_share:

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "I am reading \"" + bookDetailRP.getBook_title() + "\" on " + getResources().getString(R.string.app_name) + " come & join me at https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_one)));

                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    // single book detail
    public void scd(String userId) {
        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "get_single_book");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookDetailRP> call = apiService.getBookDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<BookDetailRP>() {
            @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled", "UseCompatLoadingForDrawables"})
            @Override
            public void onResponse(@NotNull Call<BookDetailRP> call, @NotNull Response<BookDetailRP> response) {

                try {
                    bookDetailRP = response.body();
                    assert bookDetailRP != null;

                    //for printing large logs i.e for printing more that 4000 words
                    //largeLogcat("BOOK_DETAILS", new Gson().toJson(response.body()));

                    Log.e("TAG", bookDetailRP.getCopyright_link());

                    if (bookDetailRP.getStatus().equals("1")) {

                        toolbar.setTitle(bookDetailRP.getBook_title());

                        if (bookDetailRP.getIs_fav().equals("true")) {
                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.favourited));
                            favText.setText("Favorite");
                            favText.setTextColor(getResources().getColor(R.color.gnt_blue));
                        } else {
                            favText.setText("Add To Library");
                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_book));
                        }

                        int value = Integer.parseInt(bookDetailRP.getBook_views());
                        value++;
                        bookDetailRP.setBook_views(String.valueOf(value));

                        Glide.with(BookDetail.this).load(bookDetailRP.getBook_cover_img())
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageViewBookCover);

                        Glide.with(BookDetail.this).load(bookDetailRP.getBook_bg_img())
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageView);

                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setPluginState(WebSettings.PluginState.ON);

                        webView.setBackgroundColor(Color.TRANSPARENT);
                        webView.setFocusableInTouchMode(false);
                        webView.setFocusable(false);
                        webView.getSettings().setDefaultTextEncodingName("UTF-8");
                        String mimeType = "text/html";
                        String encoding = "utf-8";

                        String text = "<html dir=" + method.isWebViewTextRtl() + "><head>"
                                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/quicksand_regular.ttf\")}body{font-family: MyFont;color: " + method.webViewText() + "line-height:1.6}"
                                + "a {color:" + method.webViewLink() + "text-decoration:none}"
                                + "</style></head>"
                                + "<body>"
                                + bookDetailRP.getBook_description()
                                + "</body></html>";

                        webView.loadDataWithBaseURL(null, text.length() > 500 ? text.substring(0, 500) + " ..." : text, mimeType, encoding, null);

                        /*webView.setOnTouchListener((v, event) -> {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                webView.loadDataWithBaseURL(null, text.substring(0, text.length() - 1), mimeType, encoding, null);
                                return true;
                            }
                            return false;
                        });*/

                        seeMore.setOnClickListener(v -> {
                            if (seeMore.getText().equals("More")) {
                                seeMore.setText("Less");
                                webView.loadDataWithBaseURL(null, text.substring(0, text.length() - 1), mimeType, encoding, null);
                            } else {
                                seeMore.setText("More");
                                webView.loadDataWithBaseURL(null, text.length() > 500 ? text.substring(0, 500) + " ..." : text, mimeType, encoding, null);
                            }
                        });
                        textViewBookName.setText(bookDetailRP.getBook_title());

                        String authorName = "";

                        if (bookDetailRP.getAuthorList().size() == 1) {
                            authorName = authorName + bookDetailRP.getAuthorList().get(0).getAuthorName();
                        } else {
                            for (int i = 0; i < bookDetailRP.getAuthorList().size(); i++) {
                                authorName = authorName + bookDetailRP.getAuthorList().get(i).getAuthorName() + ", ";
                            }
                        }

                        readingStatus.setText(bookDetailRP.getBook_complt());
                        textViewAuthor.setText(method.getAuthorFormatted(authorName));
                        textViewRating.setText(bookDetailRP.getRate_avg());
                        textViewView.setText(Method.Format(Integer.parseInt(bookDetailRP.getBook_views())) /*+ "\nReads"*/);

                        List<String> bookTagList = Arrays.asList(bookDetailRP.getBook_tag().split(","));
                        if (bookTagList.size() == 0) {
                            llTagContainer.setVisibility(View.GONE);
                        } else {
                            llTagContainer.setVisibility(View.VISIBLE);
                            llTagContainer.setVisibility(View.VISIBLE);
                            BookTagAdapter bookTagAdapter = new BookTagAdapter(BookDetail.this, bookTagList);
                            recyclerViewTag.setAdapter(bookTagAdapter);
                        }

                        //related book
                        if (bookDetailRP.getBookLists().size() == 0) {
                            conRelated.setVisibility(View.GONE);
                        } else {
                            conRelated.setVisibility(View.VISIBLE);
                            BookHomeAdapter bookHomeAdapter = new BookHomeAdapter(BookDetail.this, bookDetailRP.getBookLists(), "home_continue", onClick);
                            recyclerViewRelated.setAdapter(bookHomeAdapter);
                        }

                        commentLists.addAll(bookDetailRP.getCommentLists());
                        textViewComment.setText(getResources().getString(R.string.view_all) + " " + "(" + bookDetailRP.getTotal_comment() + ")");

                        //book comment
                        if (commentLists.size() == 0) {
                            conComment.setVisibility(View.GONE);
                        } else {
                            conComment.setVisibility(View.VISIBLE);
                            commentAdapter = new CommentAdapter(BookDetail.this, commentLists);
                            recyclerViewComment.setAdapter(commentAdapter);
                        }

                        conMain.setVisibility(View.VISIBLE);

                        imageView.setOnClickListener(v -> continueBook(bookDetailRP.getId()));

                        ratingLayout.setOnClickListener(v -> {
                            ratingLayout.startAnimation(myAnim);
                            if (method.isLogin()) {
                                myRating(bookDetailRP.getId(), method.userId());
                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Landingpage.class));
                            }
                        });

                        imageViewFav.setOnClickListener(v -> {
                            imageViewFav.startAnimation(myAnim);
                            if (method.isLogin()) {
                                FavouriteIF favouriteIF = (isFavourite, message) -> {
                                    if (!isFavourite.equals("")) {
                                        if (isFavourite.equals("true")) {
                                            favText.setText("Favorite");
                                            recordPref.countFavourite(true);
                                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.favourited));
                                            favText.setTextColor(getResources().getColor(R.color.gnt_blue));
                                        } else {
                                            favText.setText("Add To Library");
                                            recordPref.countFavourite(false);
                                            if (method.isDarkMode()) {
                                                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.favourite));
                                                favText.setTextColor(getResources().getColor(R.color.white));
                                            } else {
                                                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.favourite));
                                                favText.setTextColor(getResources().getColor(R.color.appColorDark));
                                            }
                                        }
                                    }
                                };
                                method.addToFav(bookDetailRP.getId(), method.userId(), favouriteIF);
                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Landingpage.class));
                            }
                        });

                        imageViewRead.setOnClickListener(v -> {
                            imageViewRead.startAnimation(myAnim);
                            if (method.isNetworkAvailable()) {
                                continueBook(bookDetailRP.getId());
                            } else {
                                method.alertBox(getResources().getString(R.string.internet_connection));
                            }
                        });

                        textViewComment.setOnClickListener(v -> startActivity(new Intent(BookDetail.this, AllComment.class)
                                .putExtra("bookId", bookDetailRP.getId())));

                        textViewRelViewAll.setOnClickListener(v ->
                                startActivity(new Intent(BookDetail.this, RelatedBook.class)
                                        .putExtra("cat_id", bookDetailRP.getCat_id())
                                        .putExtra("sub_cat_id", bookDetailRP.getCat_id())
                                        .putExtra("aid", bookDetailRP.getAid())
                                        .putExtra("book_id", bookDetailRP.getId())));

                        imageViewSend.setOnClickListener(v -> {

                            if (method.isLogin()) {
                                editTextComment.setError(null);
                                String comment = editTextComment.getText().toString();
                                if (comment.isEmpty()) {
                                    editTextComment.requestFocus();
                                    editTextComment.setError(getResources().getString(R.string.please_enter_comment));
                                } else {

                                    editTextComment.clearFocus();
                                    imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);

                                    if (method.isNetworkAvailable()) {
                                        Comment(method.userId(), bookDetailRP.getId(), comment);
                                    } else {
                                        method.alertBox(getResources().getString(R.string.internet_connection));
                                    }
                                }
                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Landingpage.class));
                            }
                        });

                    } else if (bookDetailRP.getStatus().equals("2")) {
                        method.suspend(bookDetailRP.getMessage());
                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(bookDetailRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
                progressBar.setVisibility(View.GONE);
                Utils.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<BookDetailRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                conNoData.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public String getAllAuthorName(BookDetailRP detailRP) {
        String authorName = "";
        if (detailRP.getAuthorList().size() == 1) {
            authorName = authorName + detailRP.getAuthorList().get(0).getAuthorName();
        } else {
            for (int i = 0; i < detailRP.getAuthorList().size(); i++) {
                authorName = authorName + detailRP.getAuthorList().get(i).getAuthorName() + ", ";
            }
        }
        return authorName;
    }


    public void continueBook(String bookId) {

        if (method.isLogin()) {

            Utils.show(this);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
            jsObj.addProperty("user_id", method.userId());
            jsObj.addProperty("book_id", bookId);
            jsObj.addProperty("method_name", "user_continue_book");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataRP> call = apiService.submitContinueReading(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<DataRP>() {
                @Override
                public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                    try {
                        DataRP dataRP = response.body();
                        assert dataRP != null;

                        if (dataRP.getStatus().equals("1")) {

                            if (dataRP.getSuccess().equals("1")) {
                                openBook();
                            } else {
                                method.alertBox(dataRP.getMsg());
                            }

                        } else if (dataRP.getStatus().equals("2")) {
                            method.suspend(dataRP.getMessage());
                        } else {
                            method.alertBox(dataRP.getMessage());
                        }

                    } catch (Exception e) {
                        Log.d("exception_error", e.toString());
                        Utils.dismiss();
                        method.alertBox(getResources().getString(R.string.failed_try_again));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    Utils.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        } else {
            openBook();
        }

    }

    private void openBook() {
        if (bookDetailRP.getBook_file_url().contains(".epub")) {
            DownloadEpub downloadEpub = new DownloadEpub(BookDetail.this);
            downloadEpub.pathEpub(bookDetailRP.getBook_file_url(), bookDetailRP.getId(), bookDetailRP.getBook_cover_img(), bookDetailRP.getBook_title());
        } else {
            startActivity(new Intent(BookDetail.this, PDFShow.class)
                    .putExtra("id", bookDetailRP.getId())
                    .putExtra("link", bookDetailRP.getBook_file_url())
                    .putExtra("toolbarTitle", bookDetailRP.getBook_title())
                    .putExtra("type", "link"));
            Utils.dismiss();
        }
    }

    //get user rating
    private void myRating(String bookId, String userId) {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "get_rating");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MyRatingRP> call = apiService.getMyRating(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<MyRatingRP>() {
            @Override
            public void onResponse(@NotNull Call<MyRatingRP> call, @NotNull Response<MyRatingRP> response) {

                try {
                    MyRatingRP myRatingRP = response.body();
                    assert myRatingRP != null;

                    if (myRatingRP.getStatus().equals("1")) {

                        if (myRatingRP.getSuccess().equals("1")) {

                            final Dialog dialog = new Dialog(BookDetail.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.rating_dialog);
                            if (method.isRtl()) {
                                dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                            }
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                            MaterialButton button = dialog.findViewById(R.id.button_ratingDialog);
                            ImageView imageViewClose = dialog.findViewById(R.id.image_close_ratingDialog);
                            RatingView ratingBar = dialog.findViewById(R.id.ratingBar_ratingDialog);

                            ratingBar.setRating(0);
                            ratingBar.setOnRatingChangedListener((oldRating, newRating) -> rate = (int) newRating);

                            imageViewClose.setOnClickListener(v -> dialog.dismiss());

                            button.setOnClickListener(v -> {
                                if (method.isNetworkAvailable()) {
                                    if (rate > 0) {
                                        ratingSend(rate, userId, bookId);
                                        dialog.dismiss();
                                    } else {
                                        method.alertBox(getResources().getString(R.string.rating_status));
                                    }
                                } else {
                                    method.alertBox(getResources().getString(R.string.internet_connection));
                                }
                            });

                            ratingBar.setRating(Float.parseFloat(myRatingRP.getUser_rate()));

                            dialog.show();

                        }

                    } else if (myRatingRP.getStatus().equals("2")) {
                        method.suspend(myRatingRP.getMessage());
                    } else {
                        method.alertBox(myRatingRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<MyRatingRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    //send user rating
    public void ratingSend(final int rate, String userId, final String bookId) {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("rate", rate);
        jsObj.addProperty("method_name", "book_rating");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RatingRP> call = apiService.submitRating(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<RatingRP>() {
            @Override
            public void onResponse(@NotNull Call<RatingRP> call, @NotNull Response<RatingRP> response) {

                try {
                    RatingRP ratingRP = response.body();
                    assert ratingRP != null;

                    //method.alertBox(ratingRP.getMessage());
                    if (ratingRP.getStatus().equals("1")) {

                        if (ratingRP.getSuccess().equals("1")) {
                            bookDetailRP.setRate_avg(ratingRP.getRate_avg());
                            bookDetailRP.setTotal_rate(ratingRP.getTotal_rate());
                            textViewRating.setText(ratingRP.getTotal_rate());
                        }

                        method.alertBox(ratingRP.getMsg());

                    } else if (ratingRP.getStatus().equals("2")) {
                        method.suspend(ratingRP.getMessage());
                    } else {
                        method.alertBox(ratingRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.getMessage());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                Utils.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<RatingRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    //get book report
    public void getReport(String userId, String bookId) {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("method_name", "get_report");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetReportRP> call = apiService.getBookReport(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<GetReportRP>() {
            @Override
            public void onResponse(@NotNull Call<GetReportRP> call, @NotNull Response<GetReportRP> response) {

                try {
                    GetReportRP getReportRP = response.body();
                    assert getReportRP != null;
                    if (getReportRP.getStatus().equals("1")) {

                        final Dialog dialog = new Dialog(BookDetail.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.report_dialog);
                        if (method.isRtl()) {
                            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        }
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

                        TextInputEditText editText = dialog.findViewById(R.id.editText_report_dialog);
                        MaterialButton buttonSubmit = dialog.findViewById(R.id.button_submit_review_dialog);
                        MaterialButton buttonClose = dialog.findViewById(R.id.button_close_review_dialog);

                        editText.setText(getReportRP.getReport());

                        buttonSubmit.setOnClickListener(v -> {

                            editText.setError(null);
                            String stringReport = editText.getText().toString();

                            if (stringReport.equals("") || stringReport.isEmpty()) {
                                editText.requestFocus();
                                editText.setError(getResources().getString(R.string.please_enter_comment));
                            } else {
                                editText.clearFocus();
                                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                submitReport(userId, bookId, stringReport);
                                dialog.dismiss();
                            }

                        });

                        buttonClose.setOnClickListener(v -> {
                            dialog.dismiss();
                        });

                        dialog.show();

                    } else if (getReportRP.getStatus().equals("2")) {
                        method.suspend(getReportRP.getMessage());
                    } else {
                        method.alertBox(getReportRP.getMessage());
                    }

                } catch (Exception e) {
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                    Log.d("exception_error", e.toString());
                }
                Utils.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<GetReportRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    //send book report
    public void submitReport(String userId, String bookId, String report) {

        Utils.show(this);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("report", report);
        jsObj.addProperty("method_name", "book_report");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.submitBookReport(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {
                        method.alertBox(dataRP.getMsg());
                    } else if (dataRP.getStatus().equals("2")) {
                        method.suspend(dataRP.getMessage());
                    } else {
                        method.alertBox(dataRP.getMessage());
                    }

                } catch (Exception e) {
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                    Log.d("exception_error", e.toString());
                }
                Utils.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                Utils.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    //comment
    private void Comment(final String userId, String bookId, final String comment) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("comment_text", comment);
        jsObj.addProperty("method_name", "user_comment");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UserCommentRP> call = apiService.submitComment(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<UserCommentRP>() {
            @Override
            public void onResponse(@NotNull Call<UserCommentRP> call, @NotNull Response<UserCommentRP> response) {

                try {
                    UserCommentRP userCommentRP = response.body();
                    assert userCommentRP != null;

                    if (userCommentRP.getStatus().equals("1")) {

                        if (userCommentRP.getSuccess().equals("1")) {

                            editTextComment.setText("");

                            commentLists.add(0, new CommentList(userCommentRP.getComment_id(),
                                    userCommentRP.getBook_id(), userCommentRP.getUser_id(), userCommentRP.getUser_name(),
                                    userCommentRP.getUser_profile(), userCommentRP.getComment_text(), userCommentRP.getComment_date()));

                            if (commentLists.size() == 0) {
                                conComment.setVisibility(View.GONE);
                            } else {
                                conComment.setVisibility(View.VISIBLE);
                                if (commentAdapter == null) {
                                    commentAdapter = new CommentAdapter(BookDetail.this, commentLists);
                                    recyclerViewComment.setAdapter(commentAdapter);
                                } else {
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }

                            String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + userCommentRP.getTotal_comment() + ")";
                            textViewComment.setText(buttonTotal);

                        }

                        Toast.makeText(BookDetail.this, userCommentRP.getMsg(), Toast.LENGTH_SHORT).show();

                    } else if (userCommentRP.getStatus().equals("2")) {
                        method.suspend(userCommentRP.getMessage());
                    } else {
                        method.alertBox(userCommentRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(@NotNull Call<UserCommentRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        if (type.equals("book") || type.equals("external")) {
            startActivity(new Intent(BookDetail.this, MainActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }
}
