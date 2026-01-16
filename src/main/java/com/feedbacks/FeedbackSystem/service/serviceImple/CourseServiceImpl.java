package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.CourseMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.repository.CourseRepository;
import com.feedbacks.FeedbackSystem.repository.InstructorRepository;
import com.feedbacks.FeedbackSystem.service.interfaces.CourseService;
import com.feedbacks.FeedbackSystem.specification.CourseSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepo;
    private final InstructorRepository instructorRepo;
    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseRepository courseRepo, InstructorRepository instructorRepo, CourseMapper courseMapper) {
        this.courseRepo = courseRepo;
        this.instructorRepo = instructorRepo;
        this.courseMapper = courseMapper;
    }

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    public List<CourseResponseDTO> getAllCoursesDTO() {
        List<Course> courses = courseRepo.findAll();
        return courses.stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Course getCourseById(int courseId) {
        return courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
    }

    @Override
    public CourseResponseDTO getCourseResponseById(int courseId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
        return courseMapper.toResponse(course);
    }

    @Override
    public CourseResponseDTO addCourse(CourseRequestDTO requestDTO) {
        Instructor instructor = instructorRepo.findById(requestDTO.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found."));
        Course course = courseMapper.toEntity(requestDTO, instructor);

        log.info(
                "event=COURSE_CREATED courseId={} courseName={}",
                 course.getCourseId(), requestDTO.getCourseName()
        );

        return courseMapper.toResponse(courseRepo.save(course));
    }

//    public Course addCourseDTO(CourseDTO courseDTO){
//        Course course = new Course();
//        course.setCourseName(courseDTO.getCourseName());
//        course.setCourseDescription(courseDTO.getCourseDescription());
//        course.setInstructor(courseDTO.getInstructor());
//        return course;
//    }

    @Transactional
    @Override
    public void updateCourseStateOnFeedbackAdd(Course course, int rating, boolean isEditing){
        long count = course.getFeedbackCount();
        if (!isEditing) {
            count += 1;
        }
        double avgRating = ((course.getAvgRating() * course.getFeedbackCount()) + rating) / count;
        course.setFeedbackCount(count);
        course.setAvgRating(avgRating);
    }

    @Transactional
    @Override
    public void updateCourseStateOnFeedbackRemove(Course course, int rating){
        long count = course.getFeedbackCount() - 1;

        if (count == 0){
            course.setFeedbackCount(0L);
            course.setAvgRating(0.0);
            return;
        }

        double avgRating = ((course.getAvgRating() * count + 1) - rating) / count;
        course.setFeedbackCount(count);
        course.setAvgRating(avgRating);
    }

    @Override
    public CourseResponseDTO updateCourseById(int courseId, CourseRequestDTO requestDTO){
        Course course = getCourseById(courseId);
        course = courseMapper.forGettingExists(course, requestDTO);

        log.info(
                "event=COURSE_UPDATED courseId={} courseName={}",
                course.getCourseId(), requestDTO.getCourseName()
        );

        return courseMapper.toResponse(courseRepo.save(course));
    }

    // @SqlDelete Soft Delete the record instead of physically deleting it
    @Override
    public void deleteCourseById(int courseId) {
        Course course = getCourseById(courseId);
        course.setDeletedAt(LocalDateTime.now());
        course.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info(
                "event=COURSE_DELETED courseId={} courseName={} deletedBy={}",
                course.getCourseId(), course.getCourseName(), course.getDeletedBy()
        );

        courseRepo.deleteById(courseId);
    }

    @Override
    public List<CourseResponseDTO> findAllSoftDeletedCourses(){
        List<Course> deletedCourses = courseRepo.findAllDeletedCourses();
        if(deletedCourses.isEmpty()){
            throw new ResourceNotFoundException("No courses deleted.");
        }
        return deletedCourses.stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    // To restore the deleted records
    @Override
    public CourseResponseDTO restoreCourse(int courseId){
        Course course = courseRepo.restoreCourseById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setDeleted(false);
        course.setDeletedAt(null);
        course.setDeletedBy(null);
        course.setRestoredBy(SecurityContextHolder.getContext().getAuthentication().getName());
        courseRepo.save(course);

        log.info(
                "event=COURSE_RESTORED courseId={} courseName={} restoredBy={}",
                course.getCourseId(), course.getCourseName(), course.getRestoredBy()
        );

        return courseMapper.toResponse(course);

    }

    @Transactional
    @Override
    public void deleteCoursePermanently(int courseId){
        if(getCourseById(courseId) == null){
            throw new ResourceNotFoundException("Course not found");
        }
        log.info("event=COURSE_PERMANENTLY_DELETED courseId={}", courseId);

        courseRepo.deletePermanently(courseId);
    }

    @Override
    public CourseResponseDTO assignInstructorToCourse(int courseId, int instructorId){
        Course course = getCourseById(courseId);
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with ID: "+instructorId+" not found." ));

        course.setInstructor(instructor);
        return courseMapper.toResponse(courseRepo.save(course));
    }

    @Override
    public CourseResponseDTO unassignInstructorToCourse(int courseId){
        Course course = getCourseById(courseId);
        if(course.getInstructor() != null) {
            course.setInstructor(null);
        }
        return courseMapper.toResponse(courseRepo.save(course));
    }

    @Override
    public Page<CourseResponseDTO> getCoursesScalable(Integer courseId,
                                                      String courseName,
                                                      Integer instructorId,
                                                      String instructorName,
                                                      Boolean popular,
                                                      Integer minEnrollments,
                                                      Double minAvgRating,
                                                      String sortBy,
                                                      String sortDirection,
                                                      int page, int size) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Course> specification = Specification.anyOf(
                CourseSpecification.hasCourseId(courseId),
                CourseSpecification.hasInstructorId(instructorId),
                CourseSpecification.hasCourseName(courseName),
                CourseSpecification.hasInstructorName(instructorName),
                CourseSpecification.popularCourse(popular),
                CourseSpecification.greaterThanAvgRatingCourses(minAvgRating)

        );
        Page<Course> course = courseRepo.findAll(specification, pageable);
        return course.map(courseMapper::toResponse);
    }

    @Override
    public List<CourseResponseDTO> searchCourseByName(String courseName){
        List<Course> courses = courseRepo.findByCourseNameContainingIgnoreCase(courseName);
        return courses.stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    public List<CourseResponseDTO> getCoursesForInstructor(int instructorId){
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with ID: "+instructorId+" not found." ));

        List<CourseResponseDTO> courseList = instructor.getCourses().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());

        if(courseList.isEmpty()){
            throw new ResourceNotFoundException("No courses available for instructor");
        }
        return courseList;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseFeedbackCountDTO> getFeedbackCountAndAvg(){
        return courseRepo.countFeedbacksAndAvgRatePerCourse();
    }


    @Override
    public List<CourseResponseDTO> getSortedCourses(String sortBy) {
        if(!sortBy.equals("ASC".toLowerCase()) || !sortBy.equals("DESC".toLowerCase())){
            throw new BadRequestException("Provide correct sorting order ASC or DESC");
        }

        Sort sort = sortBy.equalsIgnoreCase("desc") ? Sort.by("courseName").descending() :
                Sort.by("courseName").ascending();
        // findAll method accept Sort as a parameter
        return courseRepo.findAll(sort).stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(
            value = "popularCourses",
            key = "#pageNumber + ':' + #pageSize"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PopularCourseDTO> getPopularCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepo.findPopularCourses(pageable);
    }

    // Slice<T> is useful for infinite scrolling or “Load More” UI. & Doesn't count data
    @Override
    @Cacheable(
            value = "unpopularCourses",
            key = "#pageNumber + ':' + #pageSize"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Slice<PopularCourseDTO> getUnPopularCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepo.findUnPopularCourses(pageable);
    }

    @Override
    public List<CourseResponseDTO> searchCourse(Integer courseId,
                                                Integer instructorId,
                                                String courseName,
                                                String instructorName
    ){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.hasCourseId(courseId),
                CourseSpecification.hasInstructorId(instructorId),
                CourseSpecification.hasCourseName(courseName),
                CourseSpecification.hasInstructorName(instructorName)
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    public List<CourseResponseDTO> hasEnrollmentsGreaterThan(int minEnrollments) {
        return courseRepo.hasEnrollmentGreaterThan(minEnrollments).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseResponseDTO> findCoursesWithoutFeedback(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.coursesWithoutFeedback()
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseResponseDTO> findCoursesNotAssigned(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.coursesNotAssigned()
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseResponseDTO> findCoursesLessThanAvgRating(Double avgRating){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.greaterThanAvgRatingCourses(avgRating)
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(
            value = "courseRanking",
            key = "#page +':'+ #size"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseRankingDTO> getCourseRanking(int page, int size){
        return courseRepo.getCourseRaking(PageRequest.of(page, size));
    }

}
