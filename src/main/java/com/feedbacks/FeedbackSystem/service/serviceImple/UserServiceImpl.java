package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.UserMapper;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.service.interfaces.UserService;
import com.feedbacks.FeedbackSystem.specification.UserSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepo, UserMapper userMapper) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserById(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    public UserResponseDTO getUserResponseById(int userId) {
        User user =  userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return userMapper.toResponse(user);
    }

    // Return all Users as DTO
    @Override
    public List<UserResponseDTO> getAllUsersAsResponseDTO(){
        return getAllUsers().stream()
                .map(userMapper::toResponse)
                .toList();
    }
    // Return all users by userId -- fetch all required data in one query - faster
    @Override
    public List<User> getAllUsersById(){
        return userRepo.findAllById(getAllUsers().stream().
                map(User::getUserId)
                .toList());
    }


    @Override
    public UserResponseDTO addUser(UserRequestDTO userRequestDTO) {
        log.info(
                "event=NEW_USER_REGISTERED userName={} userIdentity={} registeredAt={}",
                userRequestDTO.getUsername(), userRequestDTO.getIdentityNo(), LocalDateTime.now()
        );
        Optional<User> isExist = userRepo.findByEmail(userRequestDTO.getEmail());

        if(isExist.isPresent()){
            throw new BadRequestException("This email already exists.");
        }

        User user = new User();
        user = userMapper.toEntity(user, userRequestDTO);
        userRepo.save(user);
        log.info(
                "event=NEW_USER_REGISTERED userId={} userName={} userIdentity={} registeredAt={}",
                user.getUserId(), user.getUsername(), user.getIdentityNo(), LocalDateTime.now()
        );
        return userMapper.toResponse(user);
    }


    @Override
    public UserResponseDTO updateUser(int userId, UserRequestDTO userRequestDTO) {
        if(!userRepo.existsById(userId)){
            throw new ResourceNotFoundException("User not found! Id: "+userId);
        }

        if(userRepo.existsByEmailAndUserIdNot(userRequestDTO.getEmail(), userId)) {
            throw new BadRequestException("Email already exists.");
        }

        User userExist = getUserById(userId);
        userExist = userMapper.toEntity(userExist, userRequestDTO);
        userRepo.save(userExist);
        log.info(
                "event=USER_UPDATED userId={} userName={} userIdentity={}",
                userExist.getUserId(), userExist.getUsername(), userExist.getIdentityNo()
        );
        return userMapper.toResponse(userExist);
    }

    @Override
    public void deleteByUserId(int userId) {
        if(!userRepo.existsById(userId)){
            throw new ResourceNotFoundException("User not exist - ID:"+userId);
        }

        userRepo.deleteById(userId);
        log.info(
                "event=USER_DELETED userId={}",
                userId
        );
    }

    @Override
    public UserResponseDTO getByRollNo(String rollNo){
        User user = userRepo.findByIdentityNo(rollNo)
                .orElseThrow(() ->new ResourceNotFoundException("Student with rollNo "+rollNo+" not found."));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsersByRole(User.Role roleEnum){
        return userRepo.findByRole(roleEnum).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getByEmail(String email){
        User student = userRepo.findByEmail(email)
                .orElseThrow(() ->new ResourceNotFoundException("Student with email " + email + " not found."));
        return userMapper.toResponse(student);
    }

    @Override
    public Integer getTotalStudentsCount() {
        return userRepo.totalStudentsCount();
    }

    @Override
    public List<TopRatedStudentsDTO> getTopRatedStudents(int pageNumber, int limit) {
        Pageable pageable = PageRequest.of(pageNumber, limit);
        return userRepo.findTopStudentsByFeedbacks(pageable);
    }

    @Override
    public List<UserResponseDTO> searchUser(Integer userId,
                                            Integer studentId,
                                            String studentName,
                                            String rollNo){
        Specification<User> specification = Specification.allOf(
                UserSpecification.hasUserId(userId),
                UserSpecification.studentById(studentId),
                UserSpecification.hasStudentName(studentName),
                UserSpecification.hasRollNo(rollNo)
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // ADMIN search specifications
    @Override
    public List<UserResponseDTO> searchAdmin(Integer adminUId,
                                             String adminId) {
        Specification<User> specification = Specification.allOf(
                UserSpecification.adminById(adminUId),
                UserSpecification.hasAdminIdNo(adminId)
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // Single method to return any of the below methods
    @Override
    public List<UserResponseDTO> getStudents(Boolean hasFeedbacks,
                                             Boolean enrolled,
                                             Integer courseId) {
        if (hasFeedbacks) {
            return findStudentsWithoutFeedback();
        } else if (enrolled) {
            return findStudentsWithoutEnrollment();
        } else {
            return studentsEnrolledToThisCourse(courseId);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<UserResponseDTO> findStudentsWithoutFeedback(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithoutFeedback()
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<UserResponseDTO> findStudentsWithFeedback(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithFeedback()
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponseDTO> findStudentsWithoutEnrollment(){
        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsWithoutEnrollments()
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponseDTO> studentsEnrolledToThisCourse(Integer courseId){

        Specification<User> specification = Specification.allOf(
                UserSpecification.studentsEnrolledToThisCourse(courseId)
        );
        return userRepo.findAll(specification).stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
