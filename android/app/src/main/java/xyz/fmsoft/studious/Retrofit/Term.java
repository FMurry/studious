package xyz.fmsoft.studious.Retrofit;

/**
 * Created by fredericmurry on 12/29/16.
 */

public class Term {

    private String name;
    private String school;
    private String startDate;
    private String endDate;
    private String type;
    private Course[] courses;

    public Term(String name, String school, String startDate, String endDate, String type, Course[] courses) {
        this.name = name;
        this.school = school;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.courses = courses;
    }

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

    public Course[] getCourses() {
        return courses;
    }
}
