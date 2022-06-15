package com.dreamlibrary.storyapp.rest;

import com.dreamlibrary.storyapp.item.AuthorMainSection;
import com.dreamlibrary.storyapp.item.HomeMainSection;
import com.dreamlibrary.storyapp.item.MainChildAuthorSection;
import com.dreamlibrary.storyapp.item.MainChildHomeSection;
import com.dreamlibrary.storyapp.response.AppRP;
import com.dreamlibrary.storyapp.response.AuthorDetailRP;
import com.dreamlibrary.storyapp.response.AuthorRP;
import com.dreamlibrary.storyapp.response.AuthorSpinnerRP;
import com.dreamlibrary.storyapp.response.BookDetailRP;
import com.dreamlibrary.storyapp.response.BookRP;
import com.dreamlibrary.storyapp.response.CatRP;
import com.dreamlibrary.storyapp.response.CatSpinnerRP;
import com.dreamlibrary.storyapp.response.CommentRP;
import com.dreamlibrary.storyapp.response.ContactRP;
import com.dreamlibrary.storyapp.response.DataRP;
import com.dreamlibrary.storyapp.response.DeleteCommentRP;
import com.dreamlibrary.storyapp.response.FaqRP;
import com.dreamlibrary.storyapp.response.FavouriteRP;
import com.dreamlibrary.storyapp.response.GetReportRP;
import com.dreamlibrary.storyapp.response.HomeRP;
import com.dreamlibrary.storyapp.response.LoginRP;
import com.dreamlibrary.storyapp.response.MyRatingRP;
import com.dreamlibrary.storyapp.response.PrivacyPolicyRP;
import com.dreamlibrary.storyapp.response.ProfileRP;
import com.dreamlibrary.storyapp.response.RatingRP;
import com.dreamlibrary.storyapp.response.RegisterRP;
import com.dreamlibrary.storyapp.response.RemoveContinueBook;
import com.dreamlibrary.storyapp.response.SubCatRP;
import com.dreamlibrary.storyapp.response.SubCatSpinnerRP;
import com.dreamlibrary.storyapp.response.TermsConditionsRP;
import com.dreamlibrary.storyapp.response.UserCommentRP;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    //get app data
    @POST("api.php")
    @FormUrlEncoded
    Call<AppRP> getAppData(@Field("data") String data);

    //login
    @POST("api.php")
    @FormUrlEncoded
    Call<LoginRP> getLogin(@Field("data") String data);

    //login check
    @POST("api.php")
    @FormUrlEncoded
    Call<LoginRP> getLoginDetail(@Field("data") String data);

    //register
    @POST("api.php")
    @FormUrlEncoded
    Call<RegisterRP> getRegisterDetail(@Field("data") String data);

    //forget password
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> getForgetPassword(@Field("data") String data);

    //profile
    @POST("api.php")
    @FormUrlEncoded
    Call<ProfileRP> getProfile(@Field("data") String data);

    //edit profile
    @POST("api.php")
    @Multipart
    Call<DataRP> getEditProfile(@Part("data") RequestBody data, @Part MultipartBody.Part part);

    //update password
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> updatePassword(@Field("data") String data);

    //home page
    @POST("api.php")
    @FormUrlEncoded
    Call<HomeRP> getHome(@Field("data") String data);

    //category
    @POST("api.php")
    @FormUrlEncoded
    Call<CatRP> getCategory(@Field("data") String data);

    //sub category
    @POST("api.php")
    @FormUrlEncoded
    Call<SubCatRP> getSubCategory(@Field("data") String data);

    //category spinner list
    @POST("api.php")
    @FormUrlEncoded
    Call<CatSpinnerRP> getCatSpinner(@Field("data") String data);

    //sub category spinner list
    @POST("api.php")
    @FormUrlEncoded
    Call<SubCatSpinnerRP> getSubCatSpinner(@Field("data") String data);

    //category by id book list
    @POST("api.php")
    @FormUrlEncoded
    Call<BookRP> getCatBook(@Field("data") String data);

    //author
    @POST("api.php")
    @FormUrlEncoded
    Call<AuthorRP> getAuthor(@Field("data") String data);

    //authorSection
    @POST("api.php")
    @FormUrlEncoded
    Call<AuthorMainSection> getAuthorSection(@Field("data") String data);

    //homeSection
    @POST("api.php")
    @FormUrlEncoded
    Call<HomeMainSection> getHomeSection(@Field("data") String data);

    //homeSection
    @POST("api.php")
    @FormUrlEncoded
    Call<HomeMainSection> getSearchSection(@Field("data") String data);

    //childAuthorSection
    @POST("api.php")
    @FormUrlEncoded
    Call<MainChildAuthorSection> getChildAuthorSection(@Field("data") String data);

    //childHomeSection
    @POST("api.php")
    @FormUrlEncoded
    Call<MainChildHomeSection> getChildHomeSection(@Field("data") String data);

    //childHomeSection
    @POST("api.php")
    @FormUrlEncoded
    Call<MainChildHomeSection> getChildSearchSection(@Field("data") String data);

    //author spinner list
    @POST("api.php")
    @FormUrlEncoded
    Call<AuthorSpinnerRP> getAuthorSpinner(@Field("data") String data);

    //author detail
    @POST("api.php")
    @FormUrlEncoded
    Call<AuthorDetailRP> getAuthorDetail(@Field("data") String data);

    //author by book
    @POST("api.php")
    @FormUrlEncoded
    Call<BookRP> getAuthorBook(@Field("data") String data);

    //continue reading book
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitContinueReading(@Field("data") String data);

    //latest and popular book
    @POST("api.php")
    @FormUrlEncoded
    Call<BookRP> getLatestBook(@Field("data") String data);

    //Favourite book
    @POST("api.php")
    @FormUrlEncoded
    Call<FavouriteRP> getFavouriteBook(@Field("data") String data);

    //related book
    @POST("api.php")
    @FormUrlEncoded
    Call<BookRP> getRelated(@Field("data") String data);

    //search book
    @POST("api.php")
    @FormUrlEncoded
    Call<BookRP> getSearchBook(@Field("data") String data);

    //book detail
    @POST("api.php")
    @FormUrlEncoded
    Call<BookDetailRP> getBookDetail(@Field("data") String data);

    //get all comment
    @POST("api.php")
    @FormUrlEncoded
    Call<CommentRP> getAllComment(@Field("data") String data);

    //comment
    @POST("api.php")
    @FormUrlEncoded
    Call<UserCommentRP> submitComment(@Field("data") String data);

    //delete comment
    @POST("api.php")
    @FormUrlEncoded
    Call<DeleteCommentRP> deleteComment(@Field("data") String data);

    //get my rating
    @POST("api.php")
    @FormUrlEncoded
    Call<MyRatingRP> getMyRating(@Field("data") String data);

    //rating book
    @POST("api.php")
    @FormUrlEncoded
    Call<RatingRP> submitRating(@Field("data") String data);

    //get report book
    @POST("api.php")
    @FormUrlEncoded
    Call<GetReportRP> getBookReport(@Field("data") String data);

    //report book
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitBookReport(@Field("data") String data);

    //get privacy policy
    @POST("api.php")
    @FormUrlEncoded
    Call<PrivacyPolicyRP> getPrivacyPolicy(@Field("data") String data);

    //get terms condition
    @POST("api.php")
    @FormUrlEncoded
    Call<TermsConditionsRP> getTermsCondition(@Field("data") String data);

    //get faq
    @POST("api.php")
    @FormUrlEncoded
    Call<FaqRP> getFaq(@Field("data") String data);

    //get contact us list
    @POST("api.php")
    @FormUrlEncoded
    Call<ContactRP> getContactSub(@Field("data") String data);

    //Submit contact
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitContact(@Field("data") String data);

    //remove continue reading
    @POST("api.php")
    @FormUrlEncoded
    Call<RemoveContinueBook> removeContinueReading(@Field("data") String data);
}
