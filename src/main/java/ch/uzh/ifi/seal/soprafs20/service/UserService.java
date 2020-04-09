package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<UserGetDTO> loginUser(User user) {
        if (!checkIfUserExists(user)) {
            throw new NotFoundException("The user with the username " + user.getUsername() + " does not exist");
        }

        // retrieves the user from the db
        User userLogin = userRepository.findByUsername(user.getUsername());

        // checks if the password is correct
        if (!userLogin.getPassword().equals(user.getPassword())) {
            throw new ConflictException("The password does not match the username " + user.getUsername());
        }

        // checks if the user is already online
        if (userLogin.getStatus() == UserStatus.ONLINE) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }

        // set the status of the user to online
        userLogin.setStatus(UserStatus.ONLINE);

        // saves the update to the db
        userRepository.save(userLogin);
        userRepository.flush();

        log.debug("User login successful");
        return ResponseEntity.status(HttpStatus.OK).body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(userLogin));
    }

    public void logoutUser(long userId) {
        Optional<User> found = userRepository.findById(userId);

        if (found.isEmpty()) {
            throw new NotFoundException("No user with the id " + userId + " found.");
        }

        User user = found.get();
        user.setStatus(UserStatus.OFFLINE);

        userRepository.save(user);
        userRepository.flush();
    }

    public User getUser(long userId) {
        Optional<User> found = userRepository.findById(userId);

        // if no such user is found, an exception is thrown
        if (found.isEmpty()) {
            throw new NotFoundException("No user with the id " + userId + " found.");
        }

        return found.get();
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User user) {
        // generate an random token for the new user
        user.setToken(UUID.randomUUID().toString());


        // check if the user credentials are unique
        if (checkIfUserExists(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The username provided is not unique. Therefore, the user could not be created!");
        }

        // create all fields of the user
        user.setStatus(UserStatus.OFFLINE);
        user.setCakeday(new Date());

        // saves the given entity but data is only persisted in the database once flush() is called
        user = userRepository.save(user);
        userRepository.flush();

        log.debug("Created Information for User: {}", user);
        return user;
    }

    public void updateUser(User userUpdate, long userId) {
        // check if user is authorised to update
        if (userUpdate.getId() != userId) {
            throw new UnauthorizedException("User is not authorised to update this profile");
        }

        // checkout user by userId
        Optional<User> userExisting = userRepository.findById(userId);

        // check if user exists
        if (userExisting.isEmpty()) {
            throw new NotFoundException("The user with the id " + userId + " does not exist.");
        }

        // get the existing user
        User user = userExisting.get();

        // checkout user to test if new username already exists
        User test = userRepository.findByUsername(userUpdate.getUsername());

        // check if new username is unique
        if (test != null) {
            if (test.getUsername().equals(user.getUsername())) {
                log.debug("username stays the same");
            } else {
                throw new ConflictException("Username exists already, please choose another one.");
            }
        }

        // update user
        user.setUsername(userUpdate.getUsername());
        user.setPassword(userUpdate.getPassword());
        user.setBirthday(userUpdate.getBirthday());

        // save updated user
        userRepository.save(user);
        userRepository.flush();
    }

    public void deleteUser(long userId) {
        User user;
        Optional<User> foundUser = userRepository.findById(userId);

        if (foundUser.isPresent()) {
            user = foundUser.get();
        } else {
            throw new NotFoundException("The user with the id " + userId + " could not be found.");
        }

        userRepository.delete(user);
        userRepository.flush();
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the username defined in the User entity.
     *
     * @param userToBeCreated: The user given by the calling function.
     * @see User
     */
    private boolean checkIfUserExists(User userToBeCreated) {
        // search user by provided credentials
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        return userByUsername != null;
    }
}
