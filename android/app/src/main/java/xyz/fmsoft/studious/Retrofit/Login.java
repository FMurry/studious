package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/28/16.
 */

public class Login {

    final String success;
    final int code;
    final String token;
    final String msg;

    Login(String success, int code, String token, String msg) {
        this.success = success;
        this.code = code;
        this.token = token;
        this.msg = msg;
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

    public String getMsg() {
        return msg;
    }
}
