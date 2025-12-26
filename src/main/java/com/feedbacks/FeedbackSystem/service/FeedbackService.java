package com.feedbacks.FeedbackSystem.service;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.FeedbackMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import com.feedbacks.FeedbackSystem.repository.FeedbackRepository;
import com.feedbacks.FeedbackSystem.specification.FeedbackSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepo;
    private final FeedbackMapper feedbackMapper;

    public FeedbackService(FeedbackRepository feedbackRepo, UserService userService, CourseService courseService, EnrollmentRepository enrollmentRepo, FeedbackMapper feedbackMapper) {
        this.feedbackRepo = feedbackRepo;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentRepo = enrollmentRepo;
        this.feedbackMapper = feedbackMapper;
    }

    public Feedback getFeedbackById(Integer feedbackId) {
        return feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found! Id: " + feedbackId));
    }

    public FeedbackResponseDTO getFeedbackResponseById(Integer feedbackId) {
        return feedbackMapper.toResponse(getFeedbackById(feedbackId));
    }

    public List<FeedbackResponseDTO> getFeedbacks() {
        List<Feedback> feedbacks = feedbackRepo.findAll();
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    public FeedbackResponseDTO submitFeedback(@Valid FeedbackRequestDTO feedbackRequestDTO) {
        //fetch student
        int studentId = feedbackRequestDTO.getStudentId();
        User student = userService.getUserById(studentId);

        // rate limiting logic
        int todayFeedbackCount = feedbackRepo.countTodayFeedbacks(studentId);
        if(todayFeedbackCount >= 10){
            throw new NotAllowedException("You can submit up to 10 feedbacks per day.");
        }

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
        Feedback feedback = new Feedback();
        //convert the FeedbackDTO into Feedback
        feedbackMapper.toEntity(feedbackRequestDTO, feedback);
        //save it to the database
        feedbackRepo.save(feedback);
        //returning response as FeedbackResponseDTO
        return feedbackMapper.toResponse(feedback);
        //same procedure for updating feedback
    }


    public FeedbackResponseDTO editFeedback(Integer feedbackDTOId,
                                            @Valid FeedbackRequestDTO feedbackRequestDTO) {
        if(!feedbackRepo.existsById(feedbackDTOId)){
            throw new ResourceNotFoundException("Feedback not found. Can't update");
        }

        int studentId = feedbackRequestDTO.getStudentId();
        User student = userService.getUserById(studentId);

        int courseId = feedbackRequestDTO.getCourseId();
        Course course = courseService.getCourseById(courseId);

        if(!enrollmentRepo.existsByCourseIdAndStudentId(courseId, studentId)){
            throw new NotAllowedException("Student "+student.getUsername()+" has not enrolled in this course "+course.getCourseName()+".");
        }

        Feedback feedback = getFeedbackById(feedbackDTOId);
        Feedback editedFeedback = feedbackMapper.toEntity(feedbackRequestDTO, feedback);
        feedbackRepo.save(editedFeedback);
        return feedbackMapper.toResponse(editedFeedback);
    }


    public void deleteFeedbackById(int feedbackId) {
        if(feedbackRepo.existsById(feedbackId)) {
            feedbackRepo.delete(getFeedbackById(feedbackId));
            return;
        }
        throw new ResourceNotFoundException("Feedback not found.");
    }


    //Sorting Method
    public Sort sortingFunction(String sort){
        String[] sortParam = sort.split(","); //Split the string by ',' store them in an array
        String sortBy = sortParam[0];   // sorting order is 0th value which is field name
        Sort.Direction direction = Sort.Direction.fromString(sortParam.length > 1 ? sortParam[1] : "ASC"); //sorting direction is 1st value
        return Sort.by(direction, sortBy);
    }

    public Page<FeedbackResponseDTO> getSortedFeedbackByCourseId(int courseId, int page, int size, String sort){
        Sort sorting = sortingFunction(sort);

        if(courseService.getCourseById(courseId) == null){
            throw new ResourceNotFoundException("Course not found!");
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Feedback> feedbacks = feedbackRepo.findByCourse_CourseId(courseId, pageable);

        return feedbacks.map(feedbackMapper::toResponse);
    }


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

    public Page<FeedbackResponseDTO> getFeedbackByUserId(int userId, int pageNo,
                                                         int size, String sortString){

        Sort sorting = sortingFunction(sortString);
        Pageable pageable = PageRequest.of(pageNo, size, sorting);
        Page<Feedback> feedbackPages = feedbackRepo.findByStudentUserId(userId, pageable);
        return feedbackPages.map(feedbackMapper::toResponse);
    }

    public Double getAverageRatingForCourse(int courseId){
        Double avgRating = feedbackRepo.findAverageRatingByCourseId(courseId);
        if(avgRating == null) {
            throw new ResourceNotFoundException("No average rating to this course.");
        }
        return avgRating;
    }

    public Double getAverageRatingForInstructor(int instructorId){
        Double avgRating = feedbackRepo.findAverageRatingByInstructorId(instructorId);
        if(avgRating == null) {
            throw new ResourceNotFoundException("No average rating to this instructor.");
        }
        return avgRating;
    }

    public List<CourseFeedbackSummary> getCourseSummary(){
        return feedbackRepo.findCourseSummaries();
    }

    public List<FeedbackResponseDTO> getFeedbacksByStudentAndCourse(int studentId, int courseId) {
        List<Feedback> feedbacks = feedbackRepo.findByStudent_UserIdAndCourse_CourseId(studentId, courseId);
        if(feedbacks.isEmpty()){
            throw new ResourceNotFoundException("No feedbacks found!");
        }
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponseDTO> getRecentFeedbacksByCourseId(int courseId){
        LocalDate date = LocalDate.now().minusDays(7);
        return feedbackRepo.getRecentFeedbacksByCourseId(courseId, date).stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

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

}
