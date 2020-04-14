package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * App Controller
 * This class is responsible for handling all REST request.
 * The controller will receive the request and delegate the execution to the services and finally return the result.
 */

@RestController
public class AppController {
    private UserService userService;
    private GameService gameService;
    private PlayerService playerService;

    AppController(UserService userService, GameService gameService, PlayerService playerService) {
        this.userService = userService;
        this.gameService = gameService;
        this.playerService = playerService;
    }

    @PutMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> loginUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // search if user exists in database and change status
        return userService.loginUser(userInput);
    }

    @PostMapping("/users")
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

    @PatchMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void logoutUser(@PathVariable("userId")long userId, @RequestBody UserTokenDTO userTokenDTO) {
        // parse to String
        String token = userTokenDTO.getToken();

        // logout user
        userService.logoutUser(token, userId);
    }

    @GetMapping("/games")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<GameGetDTO> getGames() {
        // fetch all games in the internal representation
        List<Game> games = gameService.getGames();
        List<GameGetDTO> gameGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (Game game : games) {
            gameGetDTOs.add(DTOMapper.INSTANCE.convertEntityToGameGetDTO(game));
        }

        return gameGetDTOs;
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public long createLobby(@RequestBody GamePostDTO gamePostDTO) {
        // parse the input into a game instance
        Game game = DTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);

        // create a player for the owner
        Player player = playerService.createPlayer(userService.getUser(game.getOwnerId()));

        // adds player to user
        userService.addPlayer(player);

        // create the game
        Game newGame = gameService.createGame(game, player);

        // adds game to player
        playerService.addGame(player, game);

        return newGame.getId();
    }

    @PutMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void joinLobby(@PathVariable ("gameId") Long gameId, @RequestBody JoinGameDTO joinGameDTO) {
        // create a new player for the given user
        Player player = playerService.createPlayer(userService.getUser(joinGameDTO.getId()));

        // fetch the game
        Game game = gameService.getGame(gameId);

        // adds player to user
        userService.addPlayer(player);

        // adds the player to the game
        game = gameService.joinGame(game, player, joinGameDTO.getPassword());

        // adds the game to the player
        playerService.addGame(player, game);
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUser(@PathVariable("userId") Long userId) {
        // search if user exists in database
        User userInput = userService.getUser(userId);

        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userInput);
    }

    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
        // delegate to userService
        userService.deleteUser(userId);
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

    @DeleteMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void endGame(@PathVariable("gameId")Long gameId, @RequestBody UserPutDTO userPutDTO) {
        // parse input into user entity
        User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // fetch all players from the game
        List<Player> players = gameService.getPlayers(gameId);

        // end game
        gameService.endGame(gameId, user.getPlayer());

        // delete all players and remove player from user
        for (Player player : players) {
            userService.removePlayer(player);
            playerService.deletePlayer(player);
        }
    }

    @GetMapping("/games/{gameId}/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PlayerGetDTO> getPlayers(@PathVariable("gameId")Long gameId) {
        // fetch all users in the internal representation
        List<Player> players = gameService.getPlayers(gameId);
        List<PlayerGetDTO> playerGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (Player player : players) {
            playerGetDTOs.add(DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player));
        }
        return playerGetDTOs;
    }

    @DeleteMapping("/games/{gameId}/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void leaveGame(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                          @RequestBody UserPutDTO userPutDTO) {
        // parse input into user
        User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // get game and player instance
        Game game = gameService.getGame(gameId);
        Player player = playerService.getPlayer(playerId);

        // leave game, remove player from user and delete player
        gameService.leaveGame(game, player, user);
        userService.removePlayer(player);
        playerService.deletePlayer(player);
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

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }
}
