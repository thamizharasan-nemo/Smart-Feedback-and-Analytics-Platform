package com.example.FeedbackSystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
// @SqlDelete Soft Delete the record instead of physically deleting it
@SQLDelete(sql = "UPDATE course SET is_deleted = true WHERE course_id = ?")
@SQLRestriction("is_deleted = false")   // @SQLRestriction replacement of @Where clause act as global filter for all queries. sol: use native query
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int courseId;

    private String courseName;

    private String courseDescription;

    private boolean isDeleted = false;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    private String deletedBy;

    private String restoredBy;

    @ManyToOne
    @JoinColumn(name = "instructor_id") // join column of instructor_id with course table
    //owning side of the courses list
    @JsonBackReference
    private Instructor instructor;

    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

}
