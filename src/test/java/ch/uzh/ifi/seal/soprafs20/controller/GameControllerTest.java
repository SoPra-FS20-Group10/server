package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.RoundService;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
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

    @Test
    public void getGame_validInput() throws Exception {
        // given
        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.initGame();

        GameGetDTO gameGetDTO = new GameGetDTO();
        gameGetDTO.setId(1L);
        gameGetDTO.setName("testName");
        gameGetDTO.setStatus(GameStatus.WAITING);

        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/1").contentType(MediaType.APPLICATION_JSON);

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
    public void getGames_validInput() throws Exception {
        // given
        Game game = new Game();
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);

        List<Game> allGames = Collections.singletonList(game);

        // this mocks the GameService -> we define above what the gameService should return when getGames() is called
        given(gameService.getGames()).willReturn(allGames);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games").contentType(MediaType.APPLICATION_JSON);

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
        MockHttpServletRequestBuilder getRequest = get("/games/1/players/1").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk());
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
        MockHttpServletRequestBuilder getRequest = get("/games/1/players/1/bag").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].symbol", is(stone.getSymbol())))
                .andExpect(jsonPath("$[0].value", is(stone.getValue())));
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

        given(gameService.createGame(Mockito.any(), Mockito.any())).willReturn(testGame);

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