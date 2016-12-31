package xyz.fmsoft.studious.Retrofit;

import java.util.List;

/**
 * Created by fredericmurry on 12/29/16.
 */

public class User {

    private final  String _id;
    private final String name;
    private final String email;
    private final List<Term> terms;
    private final boolean verified;
    private final boolean pro;


    public User(String _id, String name, String email, List<Term> terms, boolean verified, boolean pro) {
        this._id = _id;
        this.name = name;
        this.email = email;
        this.terms = terms;
        this.verified = verified;
        this.pro = pro;
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

    public List<Term> getTerms() {
        return terms;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isPro() {return pro;}

    @Override
    public String toString() {
        return "_id: "+_id+", name: "+name+", email: "+email+", verified: "+verified+", pro: "+pro;
    }
}
