package com.aim.graderecorder2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ildi on 4/19/2017.
 */

public class Course implements Comparable<Course> {

    public static final String NAME = "name";
    public static final String OWNERS = "owners";


    @JsonIgnore
    private String key;

    private String name;

    private Map<String, Boolean> owners;

    //Required for json serialization on Firebase
    public Course(){
    }

    public Course (String name, String uid){
        this.name = name;
        owners = new HashMap<>();
        owners.put(uid, true);
    }


    public String getKey() { return key; }

    public String getName() { return name; }

    public void setKey(String key) { this.key = key; }

    public void setName(String name) { this.name = name; }


    public Map<String, Boolean> getOwners() { return owners; }

    public void setOwners(Map<String, Boolean> owners) { this.owners = owners; }

    @Override
    public String toString() {return name;}

    @Override
    public int compareTo(Course another) {return name.compareTo(another.name);}
}
