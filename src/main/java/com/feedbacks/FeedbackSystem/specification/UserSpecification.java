package com.feedbacks.FeedbackSystem.specification;

import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.User;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasUserId(Integer userId){
        return ((root, query, criteriaBuilder) ->
                userId == null ? null : criteriaBuilder.equal(root.get("userId"), userId));
    }

    public static Specification<User> studentById(Integer studentId){
        return ((root, query, criteriaBuilder) ->
            studentId == null ? null : criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), User.Role.STUDENT),
                    criteriaBuilder.equal(root.get("userId"), studentId)
            )
        );
    }

    public static Specification<User> hasStudentName(String studentName) {
        return ((root, query, criteriaBuilder) ->
                studentName == null || studentName.isEmpty() ? null
                        : criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%"+studentName.toLowerCase()+"%"));
    }

    public static Specification<User> hasRollNo(String rollNo) {
        return ((root, query, criteriaBuilder) ->
                rollNo == null || rollNo.isEmpty() ? null
                        : criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("role"), User.Role.STUDENT),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("rollNo")), "%"+rollNo.toLowerCase()+"%")
                )
        );
    }



    public static Specification<User> adminById(Integer adminUId){
        return ((root, query, criteriaBuilder) ->
                adminUId == null ? null : criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("role"), User.Role.ADMIN),
                        criteriaBuilder.equal(root.get("userId"), adminUId)
                )
        );
    }

    public static Specification<User> hasAdminIdNo(String adminId) {
        return ((root, query, criteriaBuilder) ->
                adminId == null || adminId.isEmpty() ? null
                        : criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("role"), User.Role.ADMIN),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("rollNo")), "%"+adminId.toLowerCase()+"%")
                )
        );
    }

    public static Specification<User> studentsWithoutFeedback() {
        return (root, query, criteriaBuilder) ->{
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
            subquery.select(feedbackRoot.get("student").get("userId"));

            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), User.Role.STUDENT),
                    criteriaBuilder.not(root.get("userId").in(subquery))    // not & in used when guarantee that no null values exists
            );
        };
    }

    public static Specification<User> studentsWithFeedback() {
        return (root, query, criteriaBuilder) ->{
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
            subquery.select(feedbackRoot.get("student").get("userId"));

            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), User.Role.STUDENT),
                    criteriaBuilder.in(root.get("userId").in(subquery))
            );
        };
    }

    public static Specification<User> studentsWithoutEnrollments(){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Enrollment> enrollmentRoot = subquery.from(Enrollment.class);
            subquery.select(enrollmentRoot.get("student").get("userId"))
                    // correlate: enrollment.student -> user(root)
                    .where(criteriaBuilder.equal(enrollmentRoot.get("student"), root));

            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("role"), User.Role.STUDENT),
                    criteriaBuilder.not(criteriaBuilder.exists(subquery))   // not & exists gives null safety
            );
        };
    }

    public static Specification<User> studentsEnrolledToThisCourse(Integer courseId){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Enrollment> subquery = query.subquery(Enrollment.class);
            Root<Enrollment> enrollmentRoot = subquery.from(Enrollment.class);
            subquery.select(enrollmentRoot);
            subquery.where(
                    // correlate: enrollment.student = user (root)
                    criteriaBuilder.equal(enrollmentRoot.get("student"), root),
                    criteriaBuilder.equal(enrollmentRoot.get("course").get("courseId"), courseId)
            );

            return criteriaBuilder.exists(subquery);
        };
    }
}
