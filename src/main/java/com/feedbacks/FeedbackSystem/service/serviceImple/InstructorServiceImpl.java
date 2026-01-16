package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.InstructorMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.repository.CourseRepository;
import com.feedbacks.FeedbackSystem.repository.InstructorRepository;
import com.feedbacks.FeedbackSystem.service.interfaces.InstructorService;
import com.feedbacks.FeedbackSystem.specification.InstructorSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepo;
    private final CourseRepository courseRepo;
    private final InstructorMapper instructorMapper;

    public InstructorServiceImpl(InstructorRepository instructorRepo, CourseRepository courseRepo, InstructorMapper instructorMapper) {
        this.instructorRepo = instructorRepo;
        this.courseRepo = courseRepo;
        this.instructorMapper = instructorMapper;
    }


    @Override
    public List<Instructor> getAllInstructor() {
        return instructorRepo.findAll();
    }

    // Helper to all other methods
    @Override
    public Instructor getInstructorById(Integer instructorId) {
        return instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with Id: "+instructorId));
    }

    @Override
    public InstructorResponseDTO getInstructorResponseById(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        return instructorMapper.toResponse(instructor);
    }

    @Transactional
    @Override
    public void updateInstructorStateOnFeedbackAdd(Instructor instructor, int rating, boolean isEditing){
        long count = instructor.getFeedbackCount();
        if (!isEditing){
            count += 1;
        }
        double avgRating = ((instructor.getAvgRating() * instructor.getFeedbackCount()) + rating) / count;
        instructor.setAvgRating(avgRating);
        instructor.setFeedbackCount(count);
    }

    @Transactional
    @Override
    public void updateInstructorStateOnFeedbackRemove(Instructor instructor, int rating){
        long count = instructor.getFeedbackCount() - 1;

        if (count == 0){
            instructor.setFeedbackCount(0L);
            instructor.setAvgRating(0.0);
            return;
        }

        double avgRating = ((instructor.getAvgRating() * count + 1) - rating) / count;
        instructor.setFeedbackCount(count);
        instructor.setAvgRating(avgRating);
    }

    @Override
    public InstructorResponseDTO addInstructor(InstructorRequestDTO requestDTO) {
        Instructor instructor = instructorMapper.toEntity(requestDTO);
        log.info(
                "event=INSTRUCTOR_CREATED instructorName={}",
                requestDTO.getInstructorName()
        );
        return instructorMapper.toResponse(instructorRepo.save(instructor));
    }

    @Override
    public InstructorResponseDTO updateInstructorById(int instructorId, InstructorRequestDTO requestDTO){
        Instructor exist = getInstructorById(instructorId);
        exist.setInstructorName(requestDTO.getInstructorName());
        log.info(
                "event=INSTRUCTOR_UPDATED instructorId={} instructorName={}",
                instructorId, requestDTO.getInstructorName()
        );
        return instructorMapper.toResponse(instructorRepo.save(exist));
    }

    @Override
    public void deleteInstructorById(int instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        instructor.setDeletedAt(LocalDateTime.now());
        instructor.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        log.info(
                "event=INSTRUCTOR_DELETED instructorId={} deletedBy={}",
                instructorId, instructor.getDeletedBy()
        );
        instructorRepo.delete(instructor);
    }

    @Override
    public InstructorResponseDTO assignCourseToInstructor(int instructorId, int courseId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
        Instructor instructor = getInstructorById(instructorId);

        if(course.getInstructor() != null){
            throw new BadRequestException("Course already assigned to an instructor "+course.getInstructor().getInstructorName());
        }

        instructor.addCourse(course);

        log.info(
                "event=COURSE_ASSIGNED courseId={} instructorId={} assignedAt={}",
                courseId,
                instructorId,
                LocalDateTime.now()
        );

        return instructorMapper.toResponse(instructorRepo.save(instructor));
    }

    @Override
    public void unassignCourseFromInstructor(int instructorId, int courseId) {
        Instructor instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getInstructor() == null) {
            throw new NotAllowedException("Can't unassign! No instructor has assigned to this course.");
        }

        if (course.getInstructor().getInstructorId() == instructorId) {
            instructor.removeCourse(course);
            courseRepo.save(course);
        } else {
            throw new NotAllowedException("This course is not assigned to this instructor");
        }

        log.info(
                "event=COURSE_UNASSIGNED courseId={} instructorId={} assignedAt={}",
                courseId,
                instructorId,
                LocalDateTime.now()
        );
    }

    @Override
    public List<InstructorResponseDTO> findAllSoftDeletedInstructors(){
        List<Instructor> deletedInstructors = instructorRepo.findAllDeletedInstructor();
        if(deletedInstructors.isEmpty()){
            return new ArrayList<>();
        }
        return deletedInstructors.stream()
                .map(instructorMapper::toResponse)
                .toList();
    }

    @Override
    public InstructorResponseDTO restoreInstructor(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        instructor.setDeleted(false);
        instructor.setDeletedAt(null);
        instructor.setRestoredBy(SecurityContextHolder.getContext().getAuthentication().getName());
        instructorRepo.save(instructor);
        return instructorMapper.toResponse(instructor);

    }

    @Transactional
    @Override
    public void deleteInstructorPermanently(int instructorId){
        if(getInstructorById(instructorId) == null){
            throw new ResourceNotFoundException("Course not found");
        }
        courseRepo.deletePermanently(instructorId);
    }





    @Override
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

    @Override
    public List<String> viewAssignedCourseForInstructor(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        if(instructor.getCourses().isEmpty()){
            throw new ResourceNotFoundException("No courses assigned to this instructor.");
        }
        return instructor.getCourses().stream()
                .map(Course::getCourseName)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<FeedbacksByInstructor> getAllFeedbacksByInstructor(){
        return instructorRepo.getAllFeedbacksByInstructor();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TopRatedInstructorsDTO> getAllTopRatedInstructors() {
        return instructorRepo.findTopRatedInstructor();
    }

    @Override
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


    @Override
    @Cacheable(
            value = "TopInstructors",
            key = "#page +':'+ #size"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<InstructorRankingDTO> getTopRatedInstructor(int page, int size){
        return instructorRepo.getTopRatedInstructor(PageRequest.of(page, size));
    }
}

