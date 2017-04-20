package com.aim.graderecorder2.models;

/**
 * Created by Ildi on 4/19/2017.
 */
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Student implements Comparable<Student> {

    // Used for a query
    public static final String COURSE_KEY = "courseKey";

    @JsonIgnore
    private String key;

    private String courseKey;
    private String firstName;
    private String lastName;
    private String roseUsername;
    private String team;


    // Required default constructor for Firebase object mapping
    public Student() {}

    // Used when creating from scratch
    public Student(String courseKey, String firstName, String lastName, String roseUsername, String team) {
        this.courseKey = courseKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roseUsername = roseUsername;
        this.team = team;
    }

    public String getCourseKey() {
        return courseKey;
    }

    public void setCourseKey(String courseKey) {
        this.courseKey = courseKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRoseUsername() {
        return roseUsername;
    }

    public void setRoseUsername(String roseUsername) {
        this.roseUsername = roseUsername;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "Student{" +
                "key='" + key + '\'' +
                ", courseKey='" + courseKey + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roseUsername='" + roseUsername + '\'' +
                ", team='" + team + '\'' +
                '}';
    }

    @Override
    public int compareTo(Student another) {
        return roseUsername.compareTo(another.roseUsername);
    }
}
