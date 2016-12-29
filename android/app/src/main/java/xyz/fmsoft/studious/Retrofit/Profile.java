package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/29/16.
 */

public class Profile {

    private String success;
    private int code;
    private String message;
    private User user;

    public Profile(String success, int code, String message, User user){
        this.success = success;
        this.code = code;
        this.message = message;
        this.user = user;
    }

    public String getSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "success: "+success+", code: "+code+", message: "+message+", User: "+user.toString();
    }
}
