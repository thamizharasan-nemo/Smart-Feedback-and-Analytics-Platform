package com.feedbacks.FeedbackSystem.service;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.InstructorMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.repository.CourseRepository;
import com.feedbacks.FeedbackSystem.repository.InstructorRepository;
import com.feedbacks.FeedbackSystem.specification.InstructorSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepo;
    private final CourseRepository courseRepo;
    private final InstructorMapper instructorMapper;

    public InstructorService(InstructorRepository instructorRepo, CourseRepository courseRepo, InstructorMapper instructorMapper) {
        this.instructorRepo = instructorRepo;
        this.courseRepo = courseRepo;
        this.instructorMapper = instructorMapper;
    }


    public List<Instructor> getAllInstructor() {
        return instructorRepo.findAll();
    }

    // Helper to all other methods
    public Instructor getInstructorById(Integer instructorId) {
        return instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with Id: "+instructorId));
    }

    public InstructorResponseDTO getInstructorResponseById(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        return instructorMapper.toResponse(instructor);
    }

    public InstructorResponseDTO addInstructor(Instructor instructor) {
        return instructorMapper.toResponse(instructorRepo.save(instructor));
    }

    public InstructorResponseDTO updateInstructorById(int instructorId, InstructorRequestDTO requestDTO){
        Instructor exist = getInstructorById(instructorId);
        exist.setInstructorName(requestDTO.getInstructorName());
        return instructorMapper.toResponse(instructorRepo.save(exist));
    }

    public void deleteInstructorById(int instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        instructorRepo.delete(instructor);
    }

    public InstructorResponseDTO assignCourseToInstructor(int instructorId, int courseId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
        Instructor instructor = getInstructorById(instructorId);

        if(course.getInstructor() != null){
            throw new BadRequestException("Course already assigned to an instructor "+course.getInstructor().getInstructorName());
        }

        instructor.addCourse(course);

        return instructorMapper.toResponse(instructorRepo.save(instructor));
    }

    public void unassignCourseFromInstructor(int instructorId, int courseId) {
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getInstructor() != null && course.getInstructor().getInstructorId() == instructorId) {
            instructor.removeCourse(course);
            courseRepo.save(course);
        } else {
            throw new NotAllowedException("This course is not assigned to this instructor");
        }
    }

    public List<InstructorResponseDTO> getUnassignedInstructors(){
//        List<Instructor> instructors = getAllInstructor();
//        return instructors.stream()
//                .filter(instructor -> instructor.getCourses().isEmpty())
//                .collect(Collectors.toList());
        if(instructorRepo.findUnassignedInstructors().isEmpty()){
            throw new ResourceNotFoundException("Every instructors are assigned to courses");
        }
        return instructorRepo.findUnassignedInstructors().stream()
                .map(instructorMapper::toResponse)
                .toList();
    }

    public List<String> viewAssignedCourseForInstructor(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        if(instructor.getCourses().isEmpty()){
            throw new ResourceNotFoundException("No courses assigned to this instructor.");
        }
        return instructor.getCourses().stream()
                .map(Course::getCourseName)
                .toList();
    }

    public List<FeedbacksByInstructor> getAllFeedbacksByInstructor(){
        return instructorRepo.getAllFeedbacksByInstructor();
    }

    public List<TopRatedInstructorsDTO> getAllTopRatedInstructors() {
        return instructorRepo.findTopRatedInstructor();
    }

    public List<InstructorResponseDTO> searchInstructor(Integer instructorId,
                                                        String instructorName,
                                                        String courseName
    ){
        Specification<Instructor> specification = Specification.allOf(
                InstructorSpecification.hasInstructorId(instructorId),
                InstructorSpecification.hasInstructorName(instructorName),
                InstructorSpecification.byAssignedCourseName(courseName)
        );
        return instructorRepo.findAll(specification).stream()
                .map(instructorMapper::toResponse)
                .toList();
    }
}

