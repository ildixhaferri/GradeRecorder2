package com.aim.graderecorder2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Assignment implements Comparable<Assignment> {

    public static final String COURSE_KEY = "courseKey";

    @JsonIgnore
    private String key;

    private String courseKey;
    private String name;
    private double maxGrade;

    //Used by Firebase for deserializing JSON
    public Assignment(){
    }

    //Used when creating from scratch
    public Assignment(String courseKey,String name , double maxGrade){
        this.courseKey = courseKey;
        this.name = name;
        this.maxGrade = maxGrade;
    }

    public String getKey(){return  key;}
    public void setKey(String key) {this.key = key;}

    public String getCourseKey() {return courseKey;}
    public void setCourseKey(String courseKey) {this.courseKey = courseKey;}

    public void setName(String name) {this.name = name;}
    public String getName() {return name;}

    public double getMaxGrade() {return maxGrade;}

    public void setMaxGrade(double maxGrade) {this.maxGrade = maxGrade;}


    @Override
    public String toString() {return name;}

    @Override
    public int compareTo(Assignment another) {return name.compareTo(another.name); }
    }
