package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/28/16.
 */

public class Login {

    final String success;
    final int code;
    final String token;

    Login(String success, int code, String token) {
        this.success = success;
        this.code = code;
        this.token = token;
    }

    public String getSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getToken() {
        return token;
    }
}
