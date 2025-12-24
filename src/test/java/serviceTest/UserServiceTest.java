package serviceTest;

import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.UserRepository;
import com.example.FeedbackSystem.service.UserService;
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
    UserService userService;

    @Test
    void testGetUserById(){
        when(userRepo.findById(1)).thenReturn(Optional.of(
                new User(1, "Thamizharasan", "7142admin01", "thamizharasan2555@gamil.com", "09112005tmk", User.Role.ADMIN, null)));
        User user = userService.getUserById(1);
        Assertions.assertEquals("Thamizharasan", user.getUsername());
        Assertions.assertTrue(()-> userService.getUserById(1).equals(user));
//        verify(userRepo, times(1)).findById(1);
    }

    @Test
    void testByRollNo(){
        when(userRepo.findByRollNo("22CS01")).thenReturn(
                Optional.of(new User(1, "Thamizharasan", "22cs01".toUpperCase(), "thamizharasan2555@gamil.com", "09112005tmk", User.Role.ADMIN, null)));
        UserResponseDTO userResponse = userService.getByRollNo("22CS01");
        Assertions.assertEquals("22CS01", userResponse.getIdentityNo());
    }
}
