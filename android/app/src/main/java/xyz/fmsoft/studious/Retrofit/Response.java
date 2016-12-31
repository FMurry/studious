package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/30/16.
 */

//Handles Add Term, Add Assignment, and Add Course
public class Response {

    private boolean success;
    private int code;
    private String msg;

    public Response(boolean success, int code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
