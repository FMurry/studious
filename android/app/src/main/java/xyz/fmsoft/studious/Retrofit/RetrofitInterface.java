package xyz.fmsoft.studious.Retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by fredericmurry on 12/28/16.
 */

public interface RetrofitInterface {
    @FormUrlEncoded
    @POST("login")
    Call<Login> login(@Field("email")String email, @Field("password")String password);
}
