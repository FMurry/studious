package xyz.fmsoft.studious.Retrofit;

import java.util.List;

/**
 * Created by fredericmurry on 12/29/16.
 */

public class Term {

    private String _id;
    private String name;
    private String school;
    private String startDate;
    private String endDate;
    private String type;
    private List<Course> courses;

    public Term(String _id, String name, String school, String startDate, String endDate, String type, List<Course> courses) {
        this._id = _id;
        this.name = name;
        this.school = school;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.courses = courses;
    }

    public String get_id() { return _id;}
    public String getName() {
        return name;
    }

    public String getSchool() {
        return school;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getType() {
        return type;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
