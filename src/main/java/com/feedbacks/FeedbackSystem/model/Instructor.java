package com.feedbacks.FeedbackSystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@SQLDelete(sql = "UPDATE instructor SET is_deleted = true WHERE instructor_id = ?")
@FilterDef(name = "deletedInstructorFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedInstructorFilter", condition = "is_deleted = false")
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int instructorId;

    private String instructorName;

    private double avgRating;
    private long feedbackCount;

    private LocalDateTime deletedAt;
    private String deletedBy;

    private String restoredBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
