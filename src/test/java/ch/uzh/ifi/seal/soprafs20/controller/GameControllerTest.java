package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.GamePostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.JoinGameDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserTokenDTO;
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
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST request without actually sending them over the network.
 * This tests if the UserController works.
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
    public void getGames_validInput() throws Exception {
        // given
        Game game = new Game();
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);

        List<Game> allGames = Collections.singletonList(game);

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(gameService.getGames()).willReturn(allGames);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(game.getName())))
                .andExpect(jsonPath("$[0].status", is(game.getStatus().toString())));
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
                // and return game id
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
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setId(1L);
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("testPassword");

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder deleteRequest = delete("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isOk());
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