package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/29/16.
 */

public class User {

    private String _id;
    private String name;
    private String email;
    private Course[] courses;
    private boolean verified;


    public User(String _id, String name, String email, Course[] courses, boolean verified) {
        this._id = _id;
        this.name = name;
        this.email = email;
        this.courses = courses;
        this.verified = verified;
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Course[] getCourses() {
        return courses;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public String toString() {
        return "_id: "+_id+", name: "+name+", email: "+email+", verified: "+verified;
    }
}
