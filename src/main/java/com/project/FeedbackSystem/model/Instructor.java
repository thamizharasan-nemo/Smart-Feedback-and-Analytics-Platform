package com.example.FeedbackSystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int instructorId;

    private String instructorName;

    @OneToMany(mappedBy = "instructor")
    @JsonManagedReference
    private List<Course> courses = new ArrayList<>();


    //method for assigning course and setting instructor
    public void addCourse(Course course){
        courses.add(course);
        course.setInstructor(this);
    }

    // Helper method for unassigning course
    public void removeCourse(Course course){
        courses.remove(course); // update inverse side
        course.setInstructor(null); // update owning side
    }
}
