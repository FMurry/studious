package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/29/16.
 */

public class Signup {
    final String success;
    final int code;
    final String msg;

    Signup(String success, int code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public String getSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
