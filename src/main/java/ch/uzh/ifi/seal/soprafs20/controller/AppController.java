package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.RoundService;
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
    private final UserService userService;
    private final GameService gameService;
    private final PlayerService playerService;
    private final RoundService roundService;

    AppController(UserService userService, GameService gameService, PlayerService playerService,
                  RoundService roundService) {
        this.userService = userService;
        this.gameService = gameService;
        this.playerService = playerService;
        this.roundService = roundService;
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
    public long createGame(@RequestBody GamePostDTO gamePostDTO) {
        // parse the input into a game instance
        Game game = DTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);
        User user = userService.getUser(gamePostDTO.getOwnerId());

        // create a player for the owner
        Player player = playerService.createPlayer(user);

        // adds player to user
        userService.addPlayer(player);

        // create the game
        Game newGame = gameService.createGame(game, player);

        // adds game to player and user
        playerService.addGame(player, game);
        userService.addGame(game);

        return newGame.getId();
    }

    @PutMapping("/games/{gameId}/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void joinGame(@PathVariable ("gameId") Long gameId, @RequestBody JoinGameDTO joinGameDTO) {
        // create a new player for the given user
        Player player = playerService.createPlayer(userService.getUser(joinGameDTO.getId()));

        // adds player to user
        userService.addPlayer(player);

        // adds the player to the game
        Game game = gameService.joinGame(gameId, player, joinGameDTO.getPassword());

        // adds the game to the player
        playerService.addGame(player, game);
    }

    @GetMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO getPlayer(@PathVariable("playerId")long playerId){
        Player player = playerService.getPlayer(playerId);

        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
    }

    @PutMapping("/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void readyPlayer(@PathVariable("playerId")long playerId, @RequestBody UserTokenDTO userTokenDTO) {
        // parse input into String
        String token = userTokenDTO.getToken();

        // ready player
        playerService.readyPlayer(playerId, token);
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

    @GetMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<TileGetDTO> getBoard(@PathVariable ("gameId") Long gameId) {
        List<TileGetDTO> grid = new ArrayList<>();

        // fetch game and grid
        Game game = gameService.getGame(gameId);
        List<Tile> ogGrid = game.getGrid();

        // parse tile into TileGetDTO
        for(Tile tile:ogGrid){
            grid.add(DTOMapper.INSTANCE.convertEntityToTileGetDTO(tile));
        }

        // return
        return grid;
    }

    @DeleteMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void endGame(@PathVariable("gameId")Long gameId, @RequestBody UserTokenDTO userTokenDTO) {
        // parse input into user entity
        String token = userTokenDTO.getToken();

        // fetch all players from the game
        List<Player> players = gameService.getPlayers(gameId);

        // remove game from user
        userService.removeGame(gameService.getGame(gameId));

        // end game
        gameService.endGame(gameId, token);

        // delete all players and remove player from user
        for (Player player : players) {
            userService.removePlayer(player);
            playerService.deletePlayer(player);
        }
    }

    @PutMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void startGame(@PathVariable("gameId")long gameId, @RequestBody UserTokenDTO userTokenDTO) {
        String token = userTokenDTO.getToken();

        // fetch game from db
        Game game = gameService.getGame(gameId);

        // fetch players from game
        List<Player> players = game.getPlayers();

        // start the game
        gameService.startGame(game, token);

        // fill the players bag
        for (Player player : players) {
            for (int i = 0; i < 8; ++i) {
                roundService.drawStone(game, player);
            }
        }
    }

    @GetMapping("/games/{gameId}/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PlayerGetDTO> getPlayersFromGame(@PathVariable("gameId")Long gameId) {
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
                          @RequestBody UserTokenDTO userTokenDTO) {
        // parse input into user
        String token = userTokenDTO.getToken();

        // get game and player instance
        Game game = gameService.getGame(gameId);
        Player player = playerService.getPlayer(playerId);

        // leave game, remove player from user and delete player
        gameService.leaveGame(game, player, token);
        userService.removePlayer(player);
        playerService.deletePlayer(player);
    }

    @PutMapping("/games/{gameId}/players{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void placeStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                            @RequestBody WordnikGetDTO wordnikGetDTO) {


    }

    @GetMapping("/games/{gameId}/players{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public long getPlayerScore(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId) {
        // fetch entities from db
        Game game = gameService.getGame(gameId);
        Player player = playerService.getPlayer(playerId);

        // check if player is part of game
        if (!game.getPlayers().contains(player)) {
            throw new ConflictException("The player " + playerId + " is not part of the game " + gameId);
        }

        return player.getScore();
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

    @GetMapping("/games/{gameId}/players/{playerId}/bag")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<StoneGetDTO> getStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId) {
        // get stones from player
        List<Stone> stones = playerService.getStones(playerId, gameId);

        // parse stone entities into stone DTOs
        List<StoneGetDTO> stoneGetDTOs = new ArrayList<>();

        for (Stone stone : stones) {
            stoneGetDTOs.add(DTOMapper.INSTANCE.convertEntityToStoneGetDTO(stone));
        }

        return stoneGetDTOs;
    }
    
    @PutMapping("/games/{gameId}/players/{playerId}/exchange")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<StoneGetDTO> exchangeStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                                            @RequestBody ExchangeStonesDTO exchangeStonesDTO) {
        List<StoneGetDTO> stoneGetDTOs = new ArrayList<>();

        // get player
        Player player = playerService.getPlayer(playerId);

        // check if user is authorized to perform exchange action
        if (!player.getUser().getToken().equals(exchangeStonesDTO.getToken())) {
            throw new UnauthorizedException("The user is not authorized to perform this action.");
        }

        // exchange the stones
        List<Stone> stones = roundService.exchangeStone(gameId, playerId, exchangeStonesDTO.getStoneIds());

        // parse stones into StoneGetDTO
        for (Stone stone : stones) {
            stoneGetDTOs.add(DTOMapper.INSTANCE.convertEntityToStoneGetDTO(stone));
        }

        // return
        return stoneGetDTOs;
    }
}
