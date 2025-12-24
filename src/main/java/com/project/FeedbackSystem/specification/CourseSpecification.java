package com.example.FeedbackSystem.specification;

import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Enrollment;
import com.example.FeedbackSystem.model.Feedback;
import com.example.FeedbackSystem.model.Instructor;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> hasCourseId(Integer courseId){
        return ((root, query, criteriaBuilder) ->
                courseId == null ? null : criteriaBuilder.equal(root.get("courseId"), courseId));
    }

    public static Specification<Course> hasInstructorId(Integer instructorId){
        return ((root, query, criteriaBuilder) ->
                instructorId == null ? null : criteriaBuilder.equal(root.get("instructor").get("instructorId"), instructorId));
    }

    public static Specification<Course> hasCourseName(String courseName) {
        return ((root, query, criteriaBuilder) ->
                courseName == null || courseName.isEmpty() ? null
                        : criteriaBuilder.like(criteriaBuilder.lower(root.get("courseName")), "%"+courseName.toLowerCase()+"%"));
    }

    public static Specification<Course> hasInstructorName(String instructorName) {
        return ((root, query, criteriaBuilder) ->
                instructorName == null || instructorName.isEmpty() ? null
                        : criteriaBuilder.like(criteriaBuilder.lower(root.join("instructor").get("instructorName")), "%"+instructorName.toLowerCase()+"%"));
    }

    public static Specification<Course> coursesWithoutFeedback(){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
            subquery.select(feedbackRoot.get("course").get("courseId"));

            return criteriaBuilder.not(root.get("courseId").in(subquery));
        };
    }

    public static Specification<Course> coursesNotAssigned(){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Instructor> instructorRoot = subquery.from(Instructor.class);
            subquery.select(instructorRoot.get("courses").get("courseId"));

            return criteriaBuilder.not(root.get("courseId")).in(subquery);
        };
    }

    public static Specification<Course> greaterThanAvgRatingCourses(){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
            subquery.select(criteriaBuilder.avg(feedbackRoot.get("courseRating")))
                    .where(criteriaBuilder.equal(feedbackRoot.get("course"), root));

            return criteriaBuilder.greaterThanOrEqualTo(subquery, 2.5);
        };
    }
}
