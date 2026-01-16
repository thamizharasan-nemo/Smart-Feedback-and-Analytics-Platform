package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RateLimitInfo;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.configure.FeedbackMetrics;
import com.feedbacks.FeedbackSystem.mapper.FeedbackMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import com.feedbacks.FeedbackSystem.repository.FeedbackRepository;
import com.feedbacks.FeedbackSystem.service.interfaces.FeedbackService;
import com.feedbacks.FeedbackSystem.specification.FeedbackSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final UserServiceImpl userService;
    private final CourseServiceImpl courseService;
    private final EnrollmentRepository enrollmentRepo;
    private final FeedbackMapper feedbackMapper;
    private final InstructorServiceImpl instructorService;
    private final FeedbackRateLimiterService rateLimiterService;
    private final FeedbackMetrics feedbackMetrics;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepo, UserServiceImpl userService, CourseServiceImpl courseService, EnrollmentRepository enrollmentRepo, FeedbackMapper feedbackMapper, InstructorServiceImpl instructorService, FeedbackRateLimiterService rateLimiterService, FeedbackMetrics feedbackMetrics) {
        this.feedbackRepo = feedbackRepo;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentRepo = enrollmentRepo;
        this.feedbackMapper = feedbackMapper;
        this.instructorService = instructorService;
        this.rateLimiterService = rateLimiterService;
        this.feedbackMetrics = feedbackMetrics;
    }

    public Feedback getFeedbackById(Integer feedbackId) {
        return feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found! Id: " + feedbackId));
    }

    @Override
    public FeedbackResponseDTO getFeedbackResponseById(Integer feedbackId) {
        return feedbackMapper.toResponse(getFeedbackById(feedbackId));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public List<FeedbackResponseDTO> getFeedbacks() {
        List<Feedback> feedbacks = feedbackRepo.findAll();
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(
            value = {"feedbackTrends",
                    "feedbackDistribution",
                    "popularCourses",
                    "unpopularCourses",
                    "courseRanking",
                    "TopInstructors"},
            allEntries = true
    )
    public FeedbackResponseDTO submitFeedback(@Valid FeedbackRequestDTO feedbackRequestDTO) {
        // rate limiting logic first
        rateLimiterService.checkRateLimit(feedbackRequestDTO.getStudentId());

        //fetch student
        int studentId = feedbackRequestDTO.getStudentId();
        User student = userService.getUserById(studentId);

        if(!student.getRole().toString().equals("STUDENT")){
            throw new NotAllowedException("Only students can submit feedbacks.");
        }

        //fetch course
        int courseId = feedbackRequestDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);
        //check whether student enrolled or not
        if(!enrollmentRepo.existsByCourseIdAndStudentId(courseId, studentId)){
            throw new NotAllowedException("Student "+student.getUsername()+" has not enrolled to this course "+course.getCourseName()+".");
        }

        Instructor instructor = instructorService.getInstructorById(feedbackRequestDTO.getInstructorId());
        if (instructor == null){
            throw new ResourceNotFoundException("Instructor not found");
        }
        Feedback feedback = new Feedback();
        //convert the FeedbackDTO into Feedback
        feedbackMapper.toEntity(feedbackRequestDTO, feedback, course, student, instructor);
        //save it to the database
        feedbackRepo.save(feedback);

        log.info(
                "event=FEEDBACK_SUBMITTED studentId={} courseId={} rating={}",
                studentId, courseId, feedback.getCourseRating()
        );

        // Actuator metrics check
        feedbackMetrics.incrementFeedbackSubmittedCount();

        // update state of count and avg rating separately
        courseService.updateCourseStateOnFeedbackAdd(
                course, feedback.getCourseRating(), false);
        instructorService.updateInstructorStateOnFeedbackAdd(
                feedback.getInstructor(), feedback.getInstructorRating(), false);

        //returning response as FeedbackResponseDTO
        return feedbackMapper.toResponse(feedback);
        //same procedure for updating feedback
    }


    @Override
    public FeedbackResponseDTO editFeedback(Integer feedbackId,
                                            @Valid FeedbackRequestDTO feedbackRequestDTO) {
        if(!feedbackRepo.existsById(feedbackId)){
            throw new ResourceNotFoundException("Feedback not found. Can't update");
        }

        int studentId = feedbackRequestDTO.getStudentId();
        User student = userService.getUserById(studentId);

        int courseId = feedbackRequestDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);

        if(!enrollmentRepo.existsByCourseIdAndStudentId(courseId, studentId)){
            throw new NotAllowedException("Student "+student.getUsername()+" has not enrolled in this course "+course.getCourseName()+".");
        }

        Instructor instructor = instructorService.getInstructorById(feedbackRequestDTO.getInstructorId());
        if (instructor == null){
            throw new ResourceNotFoundException("Instructor not found");
        }

        Feedback feedback = getFeedbackById(feedbackId);
        Feedback editedFeedback = feedbackMapper.toEntity(feedbackRequestDTO, feedback, course, student, instructor);
        feedbackRepo.save(editedFeedback);

        log.info(
                "event=FEEDBACK_EDITED studentId={} courseId={} rating={}",
                studentId, courseId, feedback.getCourseRating()
        );

        courseService.updateCourseStateOnFeedbackAdd(course, editedFeedback.getCourseRating(), true);
        instructorService.updateInstructorStateOnFeedbackAdd(feedback.getInstructor(), editedFeedback.getInstructorRating(), true);
        return feedbackMapper.toResponse(editedFeedback);
    }


    @Transactional
    @Override
    public void deleteFeedbackById(int userId, int feedbackId) {
        if(feedbackRepo.existsById(feedbackId)) {
            Feedback feedback = getFeedbackById(feedbackId);

            User accessor = userService.getUserById(userId);

            if (feedback.getStudent().getUserId() != userId
                    && accessor.getRole() == User.Role.STUDENT){
                throw new NotAllowedException("You can not delete someone's feedback");
            }

            feedback.setDeletedAt(LocalDateTime.now());
            feedback.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

            courseService.updateCourseStateOnFeedbackRemove(feedback.getCourse(), feedback.getCourseRating());
            instructorService.updateInstructorStateOnFeedbackRemove(feedback.getCourse().getInstructor(), feedback.getInstructorRating());

            log.info(
                    "event=FEEDBACK_DELETED userId={} feedbackId={} deletedBy={}",
                    userId, feedbackId, feedback.getDeletedBy()
            );

            feedbackRepo.delete(feedback);

            return;
        }
        throw new ResourceNotFoundException("Feedback not found.");
    }

    @Override
    public List<FeedbackResponseDTO> findAllSoftDeletedFeedbacks(){
        List<Feedback> deletedFeedbacks = feedbackRepo.findAllDeletedFeedback();
        if(deletedFeedbacks.isEmpty()){
            return new ArrayList<>();
        }
        return deletedFeedbacks.stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    public FeedbackResponseDTO restoreFeedback(int userId, int feedbackId){
        Feedback feedback = getFeedbackById(feedbackId);

        User accessor = userService.getUserById(userId);

        if (feedback.getStudent().getUserId() != userId
                && accessor.getRole() == User.Role.STUDENT){
            throw new NotAllowedException("You can not restore someone's feedback");
        }

        feedback.setDeleted(false);
        feedback.setDeletedBy(null);
        feedback.setDeletedAt(null);
        feedback.setRestoredBy(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info(
                "event=FEEDBACK_RESTORED studentId={} feedbackId={} restoredBy={}",
                userId, feedbackId, accessor.getUsername()
        );

        feedbackRepo.save(feedback);

        courseService.updateCourseStateOnFeedbackRemove(feedback.getCourse(), feedback.getCourseRating());
        instructorService.updateInstructorStateOnFeedbackRemove(feedback.getCourse().getInstructor(), feedback.getInstructorRating());

        return feedbackMapper.toResponse(feedback);
    }

    @Transactional
    @Override
    public void deleteFeedbackPermanently(int userId, int feedbackId){
        if(getFeedbackById(feedbackId) == null){
            throw new ResourceNotFoundException("Feedback not found");
        }

        Feedback feedback = getFeedbackById(feedbackId);

        User accessor = userService.getUserById(userId);

        if (feedback.getStudent().getUserId() != userId
                && accessor.getRole() == User.Role.STUDENT){
            throw new NotAllowedException("You can not delete someone's feedback");
        }

        log.info(
                "event=FEEDBACK_PERMANENTLY_DELETED studentId={} feedbackId={} deletedBy={}",
                userId, feedbackId, accessor.getUsername()
        );

        courseService.updateCourseStateOnFeedbackRemove(feedback.getCourse(), feedback.getCourseRating());
        instructorService.updateInstructorStateOnFeedbackRemove(feedback.getCourse().getInstructor(), feedback.getInstructorRating());

        feedbackRepo.deletePermanently(feedbackId);
    }

    //Sorting Method
    @Override
    public Sort sortingFunction(String sort){
        String[] sortParam = sort.split(","); //Split the string by ',' store them in an array
        String sortBy = sortParam[0];   // sorting order is 0th value which is field name
        Sort.Direction direction = Sort.Direction.fromString(sortParam.length > 1 ? sortParam[1] : "ASC"); //sorting direction is 1st value
        return Sort.by(direction, sortBy);
    }

    @Override
    public Page<FeedbackResponseDTO> getSortedFeedbackByCourseId(int courseId,
                                                                 int page,
                                                                 int size,
                                                                 String sort){
        Sort sorting = sortingFunction(sort);

        if(courseService.getCourseById(courseId) == null){
            throw new ResourceNotFoundException("Course not found!");
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Feedback> feedbacks = feedbackRepo.findByCourse_CourseId(courseId, pageable);

        return feedbacks.map(feedbackMapper::toResponse);
    }


    @Override
    public List<FeedbackResponseDTO> getFilteredFeedback(int courseId, int minRating, int maxRating,
                                                         LocalDate fromDate, LocalDate toDate, boolean anonymous) {
        // parameters can be null
        List<Feedback> feedbacks = feedbackRepo.filterFeedback(
                courseId, minRating, maxRating, fromDate, toDate, anonymous
        );

        // It converts the feedbacks into FeedBackResponseDTO
        return  feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FeedbackResponseDTO> getFeedbackByUserId(int userId, int pageNo,
                                                         int size, String sortString){

        Sort sorting = sortingFunction(sortString);
        Pageable pageable = PageRequest.of(pageNo, size, sorting);
        Page<Feedback> feedbackPages = feedbackRepo.findByStudentUserId(userId, pageable);
        return feedbackPages.map(feedbackMapper::toResponse);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Double getAverageRatingForCourse(int courseId){
        Double avgRating = feedbackRepo.findAverageRatingByCourseId(courseId);
        if(avgRating == null) {
            throw new ResourceNotFoundException("No average rating to this course.");
        }
        return avgRating;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Double getAverageRatingForInstructor(int instructorId){
        Double avgRating = feedbackRepo.findAverageRatingByInstructorId(instructorId);
        if(avgRating == null) {
            throw new ResourceNotFoundException("No average rating to this instructor.");
        }
        return avgRating;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseFeedbackSummary> getCourseSummary(){
        return feedbackRepo.findCourseSummaries();
    }

    @Override
    public List<FeedbackResponseDTO> getFeedbacksByStudentAndCourse(int studentId, int courseId) {
        List<Feedback> feedbacks = feedbackRepo.findByStudent_UserIdAndCourse_CourseId(studentId, courseId);
        if(feedbacks.isEmpty()){
            throw new ResourceNotFoundException("No feedbacks found!");
        }
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getRecentFeedbacksByCourseId(int courseId){
        LocalDate date = LocalDate.now().minusDays(7);
        return feedbackRepo.getRecentFeedbacksByCourseId(courseId, date).stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    public List<FeedbackResponseDTO> searchFeedback(Integer courseId, Integer studentId,
                                                    Integer minRating, String keyword,
                                                    String studentName, Boolean anonymous,
                                                    LocalDate fromDate, LocalDate toDate
    ){
        Specification<Feedback> specification = Specification.allOf(
                FeedbackSpecification.hasCourseId(courseId),
                FeedbackSpecification.hasStudentId(studentId),
                FeedbackSpecification.courseRatingGreaterThan(minRating),
                FeedbackSpecification.containsKeyword(keyword),
                FeedbackSpecification.hasStudentName(studentName),
                FeedbackSpecification.anonymousFeedbacks(anonymous),
                FeedbackSpecification.feedbackSubmittedBetween(fromDate, toDate)
                );
        return feedbackRepo.findAll(specification).stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(
            value = "feedbackTrends",
            key = "'trends'"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<FeedbackTrendDTO> getFeedbackTrends(String groupBy){
        return switch (groupBy.toUpperCase()){
            case "DAY" -> feedbackRepo.getDailyTrends();
            case "MONTH" -> feedbackRepo.getMonthlyTrends();
            case "YEAR" -> feedbackRepo.getYearlyTrends();
            default -> feedbackRepo.getMonthlyTrends();
        };
    }

    @Override
    @Cacheable(
            value = "feedbackDistribution",
            key = "'distribution'"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RatingDistributionDTO> getFeedbackRatings() {
        return feedbackRepo.getRatingDistribution();
    }


    public Double avgRatingOfInstructorsLast7days(){
        LocalDate sevenDaysLess = LocalDate.now().minusDays(7);
        return feedbackRepo.avgInstructRatingLast7Days(sevenDaysLess);
    }

    public Double avgRatingOfCoursesLast7days(){
        LocalDate sevenDaysLess = LocalDate.now().minusDays(7);
        return feedbackRepo.avgCourseRatingLast7Days(sevenDaysLess);
    }

    public List<Object[]> instructorRatingDistribution(Integer instructorId){
        return feedbackRepo.ratingDistribution(instructorId);
    }
}
