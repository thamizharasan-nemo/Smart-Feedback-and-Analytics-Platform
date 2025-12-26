package com.feedbacks.FeedbackSystem.service;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.CourseMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.repository.CourseRepository;
import com.feedbacks.FeedbackSystem.repository.InstructorRepository;
import com.feedbacks.FeedbackSystem.specification.CourseSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepo;
    private final InstructorRepository instructorRepo;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepo, InstructorRepository instructorRepo, CourseMapper courseMapper) {
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

    public CourseResponseDTO addCourse(Course course) {
        return courseMapper.toResponse(courseRepo.save(course));
    }

//    public Course addCourseDTO(CourseDTO courseDTO){
//        Course course = new Course();
//        course.setCourseName(courseDTO.getCourseName());
//        course.setCourseDescription(courseDTO.getCourseDescription());
//        course.setInstructor(courseDTO.getInstructor());
//        return course;
//    }

    public CourseResponseDTO updateCourseById(int courseId, CourseRequestDTO requestDTO){
        Course course = getCourseById(courseId);
        course = courseMapper.forGettingExists(course, requestDTO);
        return courseMapper.toResponse(courseRepo.save(course));
    }

    // @SqlDelete Soft Delete the record instead of physically deleting it
    public void deleteCourseById(int courseId) {
        if(getCourseById(courseId) == null){
            throw new ResourceNotFoundException("User not Found");
        }
        courseRepo.deleteById(courseId);
    }

    public List<CourseResponseDTO> findAllSoftDeletedCourses(){
        List<Course> deletedCourses = courseRepo.findAllDeletedCourses();
                // this won't work because the '@Where clause' in Course entity global filters every query with a query that defined in that clause
                // so .findAll() won't get the soft deleted courses
//                findAll().stream()
//                .filter(course -> course.isDeleted())
//                .toList();
        if(deletedCourses.isEmpty()){
            throw new ResourceNotFoundException("No courses deleted.");
        }
        return deletedCourses.stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    // To restore the deleted records
    public CourseResponseDTO restoreCourse(int courseId){
        Course course = courseRepo.restoreCourseById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setDeleted(false);
        courseRepo.save(course);
        return courseMapper.toResponse(course);

    }

    @Transactional
    public void deleteCoursePermanently(int courseId){
        if(getCourseById(courseId) == null){
            throw new ResourceNotFoundException("Course not found");
        }
        courseRepo.deletePermanently(courseId);
    }

    public CourseResponseDTO assignInstructorToCourse(int courseId, int instructorId){
        Course course = getCourseById(courseId);
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with ID: "+instructorId+" not found." ));

        course.setInstructor(instructor);
        return courseMapper.toResponse(courseRepo.save(course));
    }

    public CourseResponseDTO unassignInstructorToCourse(int courseId){
        Course course = getCourseById(courseId);
        if(course.getInstructor() != null) {
            course.setInstructor(null);
        }
        return courseMapper.toResponse(courseRepo.save(course));
    }


    public List<CourseResponseDTO> searchCourseByName(String courseName){
        List<Course> courses = courseRepo.findByCourseNameContainingIgnoreCase(courseName);
        return courses.stream()
                .map(courseMapper::toResponse)
                .toList();
    }

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

    public List<CourseFeedbackCountDTO> getFeedbackCountAndAvg(){
        return courseRepo.countFeedbacksAndAvgRatePerCourse();
    }


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

    public List<PopularCourseDTO> getPopularCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepo.findPopularCourses(pageable);
    }

    // Slice<T> is useful for infinite scrolling or “Load More” UI. & Doesn't count data
    public Slice<PopularCourseDTO> getUnPopularCourses(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepo.findUnPopularCourses(pageable);
    }

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

    public List<CourseResponseDTO> hasEnrollmentsGreaterThan(int minEnrollments) {
        return courseRepo.hasEnrollmentGreaterThan(minEnrollments).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public List<CourseResponseDTO> findCoursesWithoutFeedback(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.coursesWithoutFeedback()
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public List<CourseResponseDTO> findCoursesNotAssigned(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.coursesNotAssigned()
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public List<CourseResponseDTO> findCoursesLessThanAvgRating(){
        Specification<Course> specification = Specification.allOf(
                CourseSpecification.greaterThanAvgRatingCourses()
        );
        return courseRepo.findAll(specification).stream()
                .map(courseMapper::toResponse)
                .toList();
    }
}
