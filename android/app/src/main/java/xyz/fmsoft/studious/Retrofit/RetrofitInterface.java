package xyz.fmsoft.studious.Retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by fredericmurry on 12/28/16.
 */

public interface RetrofitInterface {
    @FormUrlEncoded
    @POST("login")
    Call<Login> login(@Field("email")String email, @Field("password")String password);

    @FormUrlEncoded
    @POST("register")
    Call<Signup> signup(@Field("email")String email, @Field("password")String password, @Field("name")String name);

    @GET("profile")
    Call<Profile> getProfile(@Header("Authorization") String token);

    @FormUrlEncoded
    @POST("addTerm")
    Call<Response> addTerm(@Header("Authorization")String token, @Field("name")String name, @Field("school")String school,
                           @Field("startDate")String startDate, @Field("endDate")String endDate, @Field("type")String type);

}
