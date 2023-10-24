package com.example.sch;

public class Student {
    private String userId;
    private String name;
    private String subjects;

    public Student() {
        // Default constructor required for Firebase
    }

    public Student(String userId, String name, String subjects) {
        this.userId = userId;
        this.name = name;
        this.subjects = subjects;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    // Define getters and setters for the fields (optional)
}
