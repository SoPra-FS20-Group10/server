package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
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
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Controller
 * This class is responsible for handling all REST request regarding the game.
 * The controller will receive the request and delegate the execution to the services and finally return the result.
 */

@RestController
@Transactional
public class GameController {
    private final UserService userService;
    private final GameService gameService;
    private final PlayerService playerService;
    private final RoundService roundService;

    GameController(UserService userService, GameService gameService, PlayerService playerService,
                   RoundService roundService) {
        this.userService = userService;
        this.gameService = gameService;
        this.playerService = playerService;
        this.roundService = roundService;
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

    @GetMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameGetDTO getGame(@PathVariable ("gameId") Long gameId) {
        List<TileGetDTO> grid = new ArrayList<>();
        List<StoneGetDTO> stoneGetDTOs = new ArrayList<>();

        // fetch game and grid
        Game game = gameService.getGame(gameId);
        List<Tile> ogGrid = game.getGrid();
        List<Stone> bag = game.getBag();
        GameGetDTO gameGetDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(game);

        // parse tile into TileGetDTO
        for(Tile tile : ogGrid){
            grid.add(DTOMapper.INSTANCE.convertEntityToTileGetDTO(tile));
        }

        // parse stone into StoneGetDTO
        for (Stone stone : bag) {
            stoneGetDTOs.add(DTOMapper.INSTANCE.convertEntityToStoneGetDTO(stone));
        }

        gameGetDTO.setCurrentPlayerId(game.getCurrentPlayerId());
        gameGetDTO.setStones(stoneGetDTOs);
        gameGetDTO.setBoard(grid);

        // return
        return gameGetDTO;
    }

    @DeleteMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void endGame(@PathVariable("gameId")Long gameId, @RequestBody UserTokenDTO userTokenDTO) {
        // parse input into user entity
        String token = userTokenDTO.getToken();

        // fetch all players from the game
        List<Player> players = gameService.getPlayers(gameId);

        // remove game from owner
        userService.removeGame(gameService.getGame(gameId));

        // end game
        gameService.endGame(gameId, token);

        // delete all players and remove player from user
        for (Player player : players) {
            User user = player.getUser();
            user.setGame(null);
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
            for (int i = 0; i < 7; ++i) {
                // draw stone
                Stone stone = roundService.drawStone(game);

                // add stone to player's bag
                player.addStone(stone);
                playerService.savePlayer(player);

                // remove stone from game's bag
                game.removeStone(stone);
            }
        }
        gameService.saveGame(game);
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

    @PutMapping("/games/{gameId}/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void placeStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                            @RequestBody PlaceWordDTO placeWordDTO) {
        // get game
        Game game = gameService.getGame(gameId);

        // get player
        Player player = playerService.getPlayer(playerId);

        // check if game is running
        if (game.getStatus() != GameStatus.RUNNING) {
            throw new ConflictException("The game with the id " + gameId + " is not running." );
        }

        // check if user is authorized to perform exchange action
        if (!player.getUser().getToken().equals(placeWordDTO.getToken())) {
            throw new UnauthorizedException("The user is not authorized to perform this action.");
        }

        // check if it's the players turn
        if (game.getCurrentPlayerId() != playerId) {
            throw new ConflictException("It's not the turn of the player " + playerId);
        }

        // check if placing is valid and place it if possible
        roundService.placeWord(game, player, placeWordDTO.getStoneIds(), placeWordDTO.getCoordinates());

        // set new current player after a successful turn
        game.setCurrentPlayerId(roundService.getCurrentPlayer(game, player).getId());

        // fill the players bag
        for (int i = 0; i < placeWordDTO.getStoneIds().size(); ++i) {
            // draw stone
            Stone stone = roundService.drawStone(game);

            // add stone to player's bag
            player.addStone(stone);
            playerService.savePlayer(player);

            // remove stone from game's bag
            game.removeStone(stone);
        }

        // check if game is over
        gameService.checkIfGameEnded(game);

        // save changes to the game
        playerService.savePlayer(player);
        gameService.saveGame(game);
    }

    @GetMapping("/games/{gameId}/players/{playerId}")
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
    public void exchangeStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                                            @RequestBody ExchangeStonesDTO exchangeStonesDTO) {

        // get player and game
        Player player = playerService.getPlayer(playerId);
        Game game = gameService.getGame(gameId);

        // check if game is running
        if (game.getStatus() != GameStatus.RUNNING) {
            throw new ConflictException("The game with the id " + gameId + " is not running." );
        }

        // check if user is authorized to perform exchange action
        if (!player.getUser().getToken().equals(exchangeStonesDTO.getToken())) {
            throw new UnauthorizedException("The user is not authorized to perform this action.");
        }

        // check if it's the players turn
        if (game.getCurrentPlayerId() != playerId) {
            throw new ConflictException("It's not the turn of the player " + playerId);
        }

        // exchange the stones
        roundService.exchangeStone(game, player, exchangeStonesDTO.getStoneIds());

        // end turn and set new currentPlayer
        game.setCurrentPlayerId(roundService.getCurrentPlayer(game, player).getId());

        // check if game has ended
        gameService.checkIfGameEnded(game);

        // save changes
        playerService.savePlayer(player);
        gameService.saveGame(game);
    }
}
