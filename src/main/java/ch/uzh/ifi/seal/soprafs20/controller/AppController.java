package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserGetDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * App Controller
 * This class is responsible for handling all REST request.
 * The controller will receive the request and delegate the execution to the services and finally return the result.
 */

@RestController
public class AppController {
    private UserService userService;

    AppController(UserService userService){
        this.userService = userService;
    };

    @PutMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> loginUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // search if user exists in database and change status
        return userService.loginUser(userInput);
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // create user
        userService.createUser(userInput);

        // returns new path
        return "/login";
    }

    @PutMapping("/lobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void logoutUser(Long userId) {
        // TODO: use userId?
    }

    @GetMapping("/lobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getGames() {
        // TODO: implement
    }

    @PostMapping("/lobby/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void createLobby(@PathVariable ("gameId") Long gameId) {
        // TODO: why gameId?
    }

    @PutMapping("/lobby/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void joinLobby(@PathVariable ("gameId") Long gameId) {
        // TODO: implement
    }

    @PostMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void createPlayer(@PathVariable ("userId") Long userId) {
        // TODO: implement
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUser(@PathVariable("userId") Long userId) {

        // search if user exists in database
        User userInput = userService.getUserById(userId);

        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userInput);
    }

    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateUser(@PathVariable("userId") long userId, @RequestBody UserPutDTO userPutDTO) {
        // convert API user to internal representation
        User userInputUpdate = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // update the user infos
        userService.updateUser(userInputUpdate, userId);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void deleteUser(@PathVariable ("userId") Long userId) {
        // TODO: implement
    }

    @GetMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getGlobalMessages() {
        //TODO: implement
    }

    @PutMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void sendGlobalMessages() {
        //TODO: implement
    }

    @GetMapping("/chat/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getLocalMessages(@PathVariable ("gameId") Long gameId) {
        //TODO: implement
    }

    @PutMapping("/chat/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void sendLocalMessages() {
        //TODO: implement
    }

    @GetMapping("/game")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getBoard() {
        //TODO: implement
    }

    @DeleteMapping("/game")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void endGame() {
        //TODO: implement
    }

    @PutMapping("/game/stones/{stoneId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void layStones() {
        //TODO: implement
    }

    @GetMapping("/game/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getScore() {
        //TODO: implement
    }

    @DeleteMapping("/game/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void leaveGame() {
        //TODO: implement
    }
}
