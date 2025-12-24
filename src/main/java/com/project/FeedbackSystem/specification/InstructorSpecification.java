package com.example.FeedbackSystem.specification;

import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Instructor;
import org.springframework.data.jpa.domain.Specification;

public class InstructorSpecification {

    public static Specification<Instructor> hasInstructorId(Integer instructorId){
        return ((root, query, criteriaBuilder) ->
                instructorId == null ? null : criteriaBuilder.equal(root.get("instructorId"), instructorId));
    }

    public static Specification<Instructor> hasInstructorName(String instructorName) {
        return ((root, query, criteriaBuilder) ->
                instructorName == null || instructorName.isEmpty() ? null
                        : criteriaBuilder.like(criteriaBuilder.lower(root.get("instructorName")), "%"+instructorName.toLowerCase()+"%"));
    }

    public static Specification<Instructor> byAssignedCourseName(String courseName) {
        return ((root, query, criteriaBuilder) ->
                courseName == null || courseName.isEmpty() ? null
                        : criteriaBuilder.like(criteriaBuilder.lower(root.join("courses").get("courseName")), "%"+courseName.toLowerCase()+"%"));
    }
}
