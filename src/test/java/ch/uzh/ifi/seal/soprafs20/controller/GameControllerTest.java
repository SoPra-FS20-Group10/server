package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GameControllerTest
 * This is a WebMvcTest which allows to test the GameController i.e. GET/POST request without actually sending them over the network.
 * This tests if the GameController works.
 */
@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private RoundService roundService;

    @MockBean
    private ChatService chatService;

    @Test
    public void getGame_validInput_noLists() throws Exception {
        // given
        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.initGame();

        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(game.getName())))
                .andExpect(jsonPath("$.status", is(game.getStatus().toString())))
                .andExpect(jsonPath("$.currentPlayerId", is(2)))
                .andExpect(jsonPath("$.stones", is(game.getBag())))
                .andExpect(jsonPath("$.board", is(game.getGrid())));
    }

    @Test
    public void getGame_validInput_withLists() throws Exception {
        // given
        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.initGame();

        // declare bag and grid
        ArrayList<Stone> bag = new ArrayList<>();
        ArrayList<Tile> grid = new ArrayList<>();

        // declare stones and add them to bag
        Stone stone1 = new Stone("a", 1);
        stone1.setId(1L);
        bag.add(stone1);

        // declare tiles and add them to grid
        Tile tile1 = new Tile(1, null, "l");
        tile1.setValue(5);
        grid.add(tile1);

        // add lists to the game
        game.setBag(bag);
        game.setGrid(grid);

        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(game.getName())))
                .andExpect(jsonPath("$.status", is(game.getStatus().toString())))
                .andExpect(jsonPath("$.currentPlayerId", is(2)))
                .andExpect(jsonPath("$.stones[0].id", is(1)))
                .andExpect(jsonPath("$.stones[0].symbol", is(stone1.getSymbol())))
                .andExpect(jsonPath("$.stones[0].value", is(stone1.getValue())))
                .andExpect(jsonPath("$.board[0].multiplier", is(tile1.getMultiplier())))
                .andExpect(jsonPath("$.board[0].stoneSymbol", is(tile1.getStoneSymbol())))
                .andExpect(jsonPath("$.board[0].multivariant", is(tile1.getMultivariant())))
                .andExpect(jsonPath("$.board[0].value", is(tile1.getValue())));
    }

    @Test
    public void getGame_noGrid() throws Exception {
        // given
        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.initGame();

        // set grid to null
        game.setGrid(null);

        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }

    @Test
    public void getGame_noBag() throws Exception {
        // given
        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.initGame();

        // set bag to null
        game.setBag(null);

        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }

    @Test
    public void getGames_validInput() throws Exception {
        // given
        Game game = new Game();
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);

        List<Game> allGames = Collections.singletonList(game);

        // this mocks the GameService
        given(gameService.getGames()).willReturn(allGames);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games").
                contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(game.getName())))
                .andExpect(jsonPath("$[0].status", is(game.getStatus().toString())));
    }

    @Test void getPlayerScore_validInput() throws Exception {
        // given
        Player player = new Player();
        player.setId(1);
        player.setUsername("testName");
        player.setStatus(PlayerStatus.NOT_READY);
        player.setScore(0);

        Game game = new Game();
        game.initGame();
        game.addPlayer(player);

        // mocks the services
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk());
    }

    @Test
    public void getPlayerScore_invalidInput_playerNotInGame() throws Exception {
        // given
        Player player = new Player();
        player.setId(1);
        player.setUsername("testName");
        player.setStatus(PlayerStatus.NOT_READY);
        player.setScore(0);

        Game game = new Game();
        game.initGame();

        // mocks the services
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isConflict());
    }

    @Test
    public void getPlayersFromGame_validInput() throws Exception {
        // given
        Player player = new Player();
        player.setId(1);
        player.setUsername("testName");
        player.setStatus(PlayerStatus.NOT_READY);
        player.setScore(0);

        List<Player> allPlayers = Collections.singletonList(player);

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(gameService.getPlayers(anyLong())).willReturn(allPlayers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1/players")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(notNullValue())))
                .andExpect(jsonPath("$[0].username", is(player.getUsername())))
                .andExpect(jsonPath("$[0].status", is(player.getStatus().toString())))
                .andExpect(jsonPath("$[0].score", is(player.getScore())));
    }

    @Test
    public void getPlayersFromGame_noPlayers() throws Exception {
        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(gameService.getPlayers(anyLong())).willReturn(null);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1/players")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }

    @Test
    public void getStones_validInput() throws Exception {
        // given
        Stone stone = new Stone();
        stone.setId(1L);
        stone.setSymbol("b");
        stone.setValue(2);

        List<Stone> allStones = Collections.singletonList(stone);

        // this mocks the GameService -> we define above what the gameService should return when getGames() is called
        given(playerService.getStones(Mockito.anyLong(), Mockito.anyLong())).willReturn(allStones);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1/players/1/bag")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].symbol", is(stone.getSymbol())))
                .andExpect(jsonPath("$[0].value", is(stone.getValue())));
    }

    @Test
    public void getWords_validInput() throws Exception {
        // given
        Word word = new Word();
        word.setId(1L);
        word.setWord("test");
        word.setValue(10);

        // add word to list
        List<Word> words = new ArrayList<>();
        words.add(word);

        // this mocks the GameService -> define what should be returned when getWords() is called
        given(gameService.getWords(Mockito.anyLong())).willReturn(words);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1/words")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].word", is(word.getWord())))
                .andExpect(jsonPath("$[0].value", is(word.getValue())));
    }

    @Test
    public void createGame_validInput() throws Exception {
        // given request
        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setOwnerId(1);
        gamePostDTO.setName("TestGame");
        gamePostDTO.setPassword("");

        // given user
        User user = new User();
        user.setUsername("TestUsername");
        user.setPassword("TestPassword");
        userService.createUser(user);

        // given game
        Game testGame = new Game();
        testGame.setId(1);
        testGame.setOwner(user);
        testGame.setName("testGame");
        testGame.setPassword("");

        // given chat
        Chat chat = new Chat();

        // mocks the services
        given(gameService.createGame(Mockito.any(), Mockito.any())).willReturn(testGame);
        given(chatService.createChat(Mockito.any())).willReturn(chat);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gamePostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isCreated());
    }

    @Test
    public void createGame_InputWithPassword() throws Exception {
        // given request
        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setOwnerId(2L);
        gamePostDTO.setName("otherTestGame");
        gamePostDTO.setPassword("test123");

        // given user
        User user = new User();
        user.setUsername("TestUsername");
        user.setPassword("TestPassword");
        userService.createUser(user);

        // given game
        Game testGame = new Game();
        testGame.setId(2L);
        testGame.setOwner(user);
        testGame.setName("testGame");
        testGame.setPassword("");

        given(gameService.createGame(Mockito.any(), Mockito.any())).willReturn(testGame);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gamePostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isCreated());
    }

    @Test
    public void joinGame_withoutPassword_validInput() throws Exception {
        // given DTO
        JoinGameDTO joinGameDTO = new JoinGameDTO();
        joinGameDTO.setId(2);
        joinGameDTO.setPassword("");

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/2/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(joinGameDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk());
    }

    @Test
    public void startGame_validInput() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        // given
        Player player = new Player();
        player.setId(1L);
        player.initPlayer();
        Player player1 = new Player();
        player1.initPlayer();
        List<Player> players = new ArrayList<>();
        players.add(player);
        players.add(player1);

        Game game = new Game();
        game.initGame();
        game.setPlayers(players);

        // when -> then: return player
        given(gameService.getGame(anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk());
    }

    @Test
    public void startGame_noToken() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken(null);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound());
    }

    @Test
    public void startGame_noPlayers() throws Exception  {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        Game game = new Game();
        game.initGame();
        game.setPlayers(null);

        // when -> then: return player
        given(gameService.getGame(anyLong())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound());
    }

    @Test
    public void startGame_otherInput() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        // given
        Player player = new Player();
        player.setId(1L);
        player.setScore(0);
        player.initPlayer();
        Player player1 = new Player();
        player.setId(2L);
        player1.setScore(2324234);
        player1.initPlayer();
        List<Player> players = new ArrayList<>();
        players.add(player);
        players.add(player1);

        Game game = new Game();
        game.initGame();
        game.setPlayers(players);

        // when -> then: return player
        given(gameService.getGame(anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk());
    }

    @Test
    public void leaveGame_validInput() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        Player player = new Player();

        Game game = new Game();
        game.setId(1L);
        game.initGame();
        game.addPlayer(player);

        // when -> then: return game
        given(gameService.getGame(anyLong())).willReturn(game);
        given(gameService.leaveGame(Mockito.any(), Mockito.any(), Mockito.anyString())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder deleteRequest = delete("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isOk());
    }

    @Test
    public void leaveGame_validInput_lastPlayer() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        Game game = new Game();
        game.setId(1L);
        game.initGame();

        // when -> then: return game
        given(gameService.getGame(anyLong())).willReturn(game);
        given(gameService.leaveGame(Mockito.any(), Mockito.any(), Mockito.anyString())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder deleteRequest = delete("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isOk());
    }

    @Test
    public void endGame_validInput() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testToken");

        User user = new User();
        user.setToken("testToken");

        Player player = new Player();
        player.setUser(user);

        Game game = new Game();
        game.initGame();
        game.addPlayer(player);

        // mock services
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder patchRequest = patch("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(patchRequest).andExpect(status().isOk());
    }

    @Test
    public void endGame_invalidInput_wrongToken() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("test");

        User user = new User();
        user.setToken("testToken");

        Player player = new Player();
        player.setUser(user);

        Game game = new Game();
        game.initGame();
        game.addPlayer(player);

        // mock services
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder patchRequest = patch("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(patchRequest).andExpect(status().isConflict());

    }

    @Test
    public void deleteGame_validInput() throws Exception {
        // given
        Game game = new Game();
        game.initGame();

        // mock services
        given(gameService.getPlayers(Mockito.anyLong())).willReturn(game.getPlayers());
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder deleteRequest = delete("/games/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isOk());
    }

    @Test
    public void deleteGame_invalidInput_playersRemaining() throws Exception {
        // given
        Game game = new Game();
        game.initGame();

        // add player to the game
        Player player = new Player();
        game.addPlayer(player);

        // mock services
        given(gameService.getPlayers(Mockito.anyLong())).willReturn(game.getPlayers());
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder deleteRequest = delete("/games/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isConflict());
    }

    @Test
    public void placeStones_validInput() throws Exception {
        // given
        User user = new User();
        Game game = new Game();
        Stone stone = new Stone();
        Player player = new Player();
        PlaceWordDTO placeWordDTO = new PlaceWordDTO();

        //initialise user
        user.setToken("testToken");

        // initialise player
        player.setId(1L);
        player.setUser(user);
        player.initPlayer();

        // initialise game
        game.initGame();
        game.addPlayer(player);
        game.setStatus(GameStatus.RUNNING);
        game.setCurrentPlayerId(player.getId());

        // initialise placeWordDTO
        placeWordDTO.setToken("testToken");
        placeWordDTO.setStoneIds(new ArrayList<>());
        placeWordDTO.setCoordinates(new ArrayList<>());

        // this mocks the services
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);
        given(roundService.drawStone(Mockito.any())).willReturn(stone);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(placeWordDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk());
    }

    @Test
    public void placeStones_invalidInput_gameNotRunning() throws Exception {
        // given
        User user = new User();
        Game game = new Game();
        Stone stone = new Stone();
        Player player = new Player();
        PlaceWordDTO placeWordDTO = new PlaceWordDTO();

        //initialise user
        user.setToken("testToken");

        // initialise player
        player.setId(1L);
        player.setUser(user);
        player.initPlayer();

        // initialise game
        game.initGame();
        game.addPlayer(player);
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(player.getId());

        // initialise placeWordDTO
        placeWordDTO.setToken("testToken");
        placeWordDTO.setStoneIds(new ArrayList<>());
        placeWordDTO.setCoordinates(new ArrayList<>());

        // this mocks the services
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);
        given(roundService.drawStone(Mockito.any())).willReturn(stone);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(placeWordDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isConflict());
    }

    @Test
    public void placeStones_invalidInput_userNotAuthorized() throws Exception {
        // given
        User user = new User();
        Game game = new Game();
        Stone stone = new Stone();
        Player player = new Player();
        PlaceWordDTO placeWordDTO = new PlaceWordDTO();

        //initialise user
        user.setToken("testToken");

        // initialise player
        player.setId(1L);
        player.setUser(user);
        player.initPlayer();

        // initialise game
        game.initGame();
        game.addPlayer(player);
        game.setStatus(GameStatus.RUNNING);
        game.setCurrentPlayerId(player.getId());

        // initialise placeWordDTO
        placeWordDTO.setToken("test");
        placeWordDTO.setStoneIds(new ArrayList<>());
        placeWordDTO.setCoordinates(new ArrayList<>());

        // this mocks the services
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);
        given(roundService.drawStone(Mockito.any())).willReturn(stone);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(placeWordDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void placeStones_invalidInput_notPlayersTurn() throws Exception {
        // given
        User user = new User();
        Game game = new Game();
        Stone stone = new Stone();
        Player player = new Player();
        PlaceWordDTO placeWordDTO = new PlaceWordDTO();

        //initialise user
        user.setToken("testToken");

        // initialise player
        player.setId(1L);
        player.setUser(user);
        player.initPlayer();

        // initialise game
        game.initGame();
        game.addPlayer(player);
        game.setStatus(GameStatus.RUNNING);
        game.setCurrentPlayerId(2L);

        // initialise placeWordDTO
        placeWordDTO.setToken("testToken");
        placeWordDTO.setStoneIds(new ArrayList<>());
        placeWordDTO.setCoordinates(new ArrayList<>());

        // this mocks the services
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);
        given(roundService.drawStone(Mockito.any())).willReturn(stone);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(placeWordDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isConflict());
    }

    @Test
    public void exchangeStones_validInput() throws Exception {
        // given
        User user = new User();
        Game game = new Game();
        Stone stone = new Stone();
        Player player = new Player();
        ExchangeStonesDTO exchangeStonesDTO = new ExchangeStonesDTO();

        //initialise user
        user.setToken("testToken");

        // initialise player
        player.setId(1L);
        player.setUser(user);
        player.initPlayer();

        // initialise game
        game.initGame();
        game.addPlayer(player);
        game.setStatus(GameStatus.RUNNING);
        game.setCurrentPlayerId(player.getId());

        // initialise exchangeStonesDTO
        exchangeStonesDTO.setToken("testToken");
        exchangeStonesDTO.setStoneIds(new ArrayList<>());

        // this mocks the services
        given(playerService.getPlayer(Mockito.anyLong())).willReturn(player);
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);
        given(roundService.getCurrentPlayer(Mockito.any(), Mockito.any())).willReturn(player);
        given(roundService.drawStone(Mockito.any())).willReturn(stone);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/games/1/players/1/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(exchangeStonesDTO));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk());
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     * @param object takes an object parameter
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new SopraServiceException(String.format("The request body could not be created.%s", e.toString()));
        }
    }
}