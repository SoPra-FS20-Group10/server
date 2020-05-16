package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.*;
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
    private final ChatService chatService;

    GameController(UserService userService, GameService gameService, PlayerService playerService,
                   RoundService roundService,ChatService chatService) {
        this.userService = userService;
        this.gameService = gameService;
        this.playerService = playerService;
        this.roundService = roundService;
        this.chatService = chatService;
    }

    @GetMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameGetDTO getGame(@PathVariable ("gameId") Long gameId) {
        List<TileGetDTO> gridGetDTO = new ArrayList<>();
        List<StoneGetDTO> stoneGetDTOs = new ArrayList<>();

        // fetch game from db
        Game game = gameService.getGame(gameId);

        // fetch grid and bag from game
        List<Tile> grid = game.getGrid();
        List<Stone> bag = game.getBag();

        // check if grid is present
        if (grid == null) {
            throw new NotFoundException("Something went wrong while fetching the grid.");
        }

        // check if bag is present
        if (bag == null){
            throw new NotFoundException("Something went wrong while fetching the bag");
        }

        // parse tile into TileGetDTO
        for(Tile tile : grid){
            gridGetDTO.add(DTOMapper.INSTANCE.convertEntityToTileGetDTO(tile));
        }

        // parse stone into StoneGetDTO
        for (Stone stone : bag) {
            stoneGetDTOs.add(DTOMapper.INSTANCE.convertEntityToStoneGetDTO(stone));
        }

        // parse Game into GameGetDTO and add currentPlayer, bag and grid
        GameGetDTO gameGetDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(game);
        gameGetDTO.setCurrentPlayerId(game.getCurrentPlayerId());
        gameGetDTO.setStones(stoneGetDTOs);
        gameGetDTO.setBoard(gridGetDTO);

        // return
        return gameGetDTO;
    }


    @GetMapping("/games")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<GameGetDTO> getGames() {
        // fetch all games in the internal representation
        List<Game> games = gameService.getGames();

        // parse Game entities into GameGetDTOs
        List<GameGetDTO> gameGetDTOs = new ArrayList<>();
        for (Game game : games) {
            gameGetDTOs.add(DTOMapper.INSTANCE.convertEntityToGameGetDTO(game));
        }

        return gameGetDTOs;
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

    @GetMapping("/games/{gameId}/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PlayerGetDTO> getPlayersFromGame(@PathVariable("gameId")Long gameId) {
        // fetch all users in the internal representation
        List<Player> players = gameService.getPlayers(gameId);

        // check if players exist
        if(players == null){
            throw new NotFoundException("the players could not be found");
        }

        // parse Player entities into playerGetDTOs
        List<PlayerGetDTO> playerGetDTOs = new ArrayList<>();
        for (Player player : players) {
            playerGetDTOs.add(DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player));
        }

        return playerGetDTOs;
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

    @GetMapping("/games/{gameId}/words")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<WordGetDTO> getWords(@PathVariable("gameId")long gameId) {
        // get words from player
        List<Word> words = gameService.getWords(gameId);

        // parse Word entities into wordDTOs
        List<WordGetDTO> wordGetDTOs = new ArrayList<>();
        for (Word word : words) {
            wordGetDTOs.add(DTOMapper.INSTANCE.convertEntityToWordGetDTO(word));
        }

        return wordGetDTOs;
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

        //create new chat
        Chat chat = chatService.createChat(game);

        gameService.addChat(chat);


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

    @PutMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void startGame(@PathVariable("gameId")long gameId, @RequestBody UserTokenDTO userTokenDTO) {
        String token = userTokenDTO.getToken();

        // check if token is existing
        if(token == null){
            throw new NotFoundException("the token could not be found");
        }

        // fetch game from db
        Game game = gameService.getGame(gameId);

        // fetch players from game
        List<Player> players = game.getPlayers();

        // check if players are existing
        if(players == null){
            throw new NotFoundException("the players could not be found");
        }

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

        // save changes
        gameService.saveGame(game);
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
        game = gameService.leaveGame(game, player, token);
        userService.removePlayer(player);
        playerService.deletePlayer(player);

        // delete game if no players are left
        if (game.getPlayers().isEmpty()) {
            chatService.deleteChat(game.getChat());
            userService.removeGame(game);
            gameService.endGame(game.getId());
        }
    }

    @PatchMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void endGame(@PathVariable("gameId")Long gameId, @RequestBody UserTokenDTO userTokenDTO) {
        boolean authorised = false;

        // parse input into String
        String token = userTokenDTO.getToken();

        // fetch game from db
        Game game = gameService.getGame(gameId);

        // fetch all players from the game
        List<Player> players = game.getPlayers();

        // check if user is authorised to end the game
        for (Player player : players) {
            if (player.getUser().getToken().equals(token)) {
                authorised = true;
                break;
            }
        }

        // if the token belongs to no player, throw exception
        if (!authorised) {
            throw new ConflictException("The user is not authorised to end the game");
        }

        // empty bag of game and all players
        game.setBag(new ArrayList<>());
        for (Player player : players) {
            player.setBag(new ArrayList<>());
        }

        // check if game should be ended -> all bags are emptied => game ends
        gameService.checkIfGameEnded(game);
    }

    @DeleteMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteGame(@PathVariable("gameId")Long gameId) {
        // fetch game from db
        Game game = gameService.getGame(gameId);

        // fetch all players from the game
        List<Player> players = game.getPlayers();

        // check if there are still players in the game
        if (!players.isEmpty()){
            throw new ConflictException("There are still players in the game");
        }

        // remove game from owner
        userService.removeGame(game);

        //delete gameChat
        chatService.deleteChat(game.getChat());

        // end game
        gameService.endGame(gameId);
    }

    @PutMapping("/games/{gameId}/players/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void placeStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                            @RequestBody PlaceWordDTO placeWordDTO) {
        // get player and game
        Player player = playerService.getPlayer(playerId);
        Game game = gameService.getGame(gameId);

        // check if game is running, user is authorized and it is the users turn
        checkIfValidAction(game, player, placeWordDTO.getToken());

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
    
    @PutMapping("/games/{gameId}/players/{playerId}/exchange")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void exchangeStones(@PathVariable("gameId")long gameId, @PathVariable("playerId")long playerId,
                                            @RequestBody ExchangeStonesDTO exchangeStonesDTO) {
        // get player and game
        Player player = playerService.getPlayer(playerId);
        Game game = gameService.getGame(gameId);

        // check if game is running, user is authorized and it is the users turn
        checkIfValidAction(game, player, exchangeStonesDTO.getToken());

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

    private void checkIfValidAction(Game game, Player player, String token) {
        // check if game is running
        if (game.getStatus() != GameStatus.RUNNING) {
            throw new ConflictException("The game with the id " + game.getId() + " is not running." );
        }

        // check if user is authorized to perform exchange action
        if (!player.getUser().getToken().equals(token)) {
            throw new UnauthorizedException("The user is not authorized to perform this action.");
        }

        // check if it's the players turn
        if (game.getCurrentPlayerId() != player.getId()) {
            throw new ConflictException("It's not the turn of the player " + player.getId());
        }
    }
}