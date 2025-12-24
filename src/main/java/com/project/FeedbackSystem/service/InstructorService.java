package com.example.FeedbackSystem.service;

import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.example.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.example.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.example.FeedbackSystem.Exception.BadRequestException;
import com.example.FeedbackSystem.Exception.NotAllowedException;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.mapper.InstructorMapper;
import com.example.FeedbackSystem.model.Course;
import com.example.FeedbackSystem.model.Instructor;
import com.example.FeedbackSystem.repository.CourseRepository;
import com.example.FeedbackSystem.repository.InstructorRepository;
import com.example.FeedbackSystem.specification.InstructorSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

