package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    private User guestUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        // given
        testUser = new User();
        testUser.setId(1L);
        testUser.setToken("testToken");
        testUser.setUsername("testUsername");
        testUser.setPassword("pw");
        testUser.setBirthday(new Date());
        testUser.setCakeDay(new Date());

        guestUser = new User();
        guestUser.setId(2L);
        guestUser.setUsername("guestUsername");
        guestUser.setPassword("");

        // when -> any object is being save in the userRepository -> return the dummy testUser
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    @Test
    void getUser_validInput() {
        // mock the userRepository
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(java.util.Optional.ofNullable(testUser));

        // call function to return the testUser
        User user = userService.getUser(1);

        // test if correct user
        assertEquals(testUser.getUsername(), user.getUsername());
        assertEquals(testUser.getToken(), user.getToken());
        assertEquals(testUser.getBirthday(), user.getBirthday());
        assertEquals(testUser.getCakeDay(), user.getCakeDay());
    }

    @Test
    void getUser_invalidInput_notExistingUser() {
        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());

        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "No user with the id 1 found.";
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUser(1), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    void getAllUsers_validInput() {
        // given
        List<User> returnedUsers;
        List<User> users = new ArrayList<>();
        users.add(testUser);

        // mock the userRepository
        Mockito.when(userRepository.findAll()).thenReturn(users);

        // call function to retrieve all users
        returnedUsers = userService.getUsers();

        // test returnedUsers
        assertEquals(1, returnedUsers.size());
        assertEquals(testUser, returnedUsers.get(0));
    }

    @Test
    void createUser_validInputs_success() {
        // when -> any object is being save in the userRepository -> return the dummy testUser
        User createdUser = userService.createUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
    }

    @Test
    void createGuest_validInputs_success() {
        userService.createUser(guestUser);

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(guestUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals("guest", guestUser.getType());
    }

    @Test
    void createUser_duplicateInputs_throwsException() {
        // given -> a first user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(java.util.Optional.ofNullable(testUser));

        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "The username provided is not unique. Therefore, the user could not be created!";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(testUser), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    void addGame_validInput() {
        // given
        Game testGame = new Game();
        testGame.setOwner(testUser);

        // call function
        userService.addGame(testGame);

        // verify that player gets saved
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // test conditions
        assertEquals(testGame, testUser.getGame());
    }

    @Test
    void removeGame_validInput() {
        // given
        Game testGame = new Game();
        testGame.setOwner(testUser);

        // call function
        userService.removeGame(testGame);

        // verify that player gets saved
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // test conditions
        assertNull(testUser.getGame());
    }

    @Test
    void addPlayer_validInput() {
        // given
        Player player = new Player();
        player.setUser(testUser);

        // call function
        userService.addPlayer(player);

        // verify that player gets saved
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // test conditions
        assertEquals(player, testUser.getPlayer());
    }

    @Test
    void removePlayer_validInput() {
        // given
        Player player = new Player();
        player.setUser(testUser);

        // call function
        userService.removePlayer(player);

        // verify that player gets saved
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // test conditions
        assertNull(testUser.getPlayer());
    }
}
