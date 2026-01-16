package serviceTest;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepo;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void testGetUserById(){
        when(userRepo.findById(1)).thenReturn(Optional.of(
                new User(1, "Thamizharasan", "7142admin01", "thamizharasan2555@gamil.com", "09112005tmk", User.Role.ADMIN, null)));
        User user = userService.getUserById(1);
        Assertions.assertEquals("Thamizharasan", user.getUsername());
        Assertions.assertTrue(()-> userService.getUserById(1).equals(user));
//        verify(userRepo, times(1)).findById(1);
    }

}
