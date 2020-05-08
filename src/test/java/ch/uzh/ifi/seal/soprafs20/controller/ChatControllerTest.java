package ch.uzh.ifi.seal.soprafs20.controller;


import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Chat;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.rest.dto.GameGetDTO;
import ch.uzh.ifi.seal.soprafs20.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

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
    public void getMessages_single_message() throws Exception {
        // given
        Chat chat = new Chat();
        chat.initchat();

        Message message = new Message();
        message.setMessage("hello");
        message.setUsername("test");
        message.setTime(100L);

        chat.addMessage(message);



        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.setChat(chat);
        game.initGame();


        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/chat/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time", is(100)))
                .andExpect(jsonPath("$[0].message", is("hello")))
                .andExpect(jsonPath("$[0].username", is("test")));

    }

    @Test
    public void getMessages_multiple_messages() throws Exception {
        // given
        Chat chat = new Chat();
        chat.initchat();

        Message message = new Message();
        message.setMessage("hello");
        message.setUsername("test");
        message.setTime(100L);

        chat.addMessage(message);

        Message message1 = new Message();
        message1.setMessage("hello1");
        message1.setUsername("test1");
        message1.setTime(101L);

        chat.addMessage(message1);

        Message message2 = new Message();
        message2.setMessage("hello2");
        message2.setUsername("test2");
        message2.setTime(102L);

        chat.addMessage(message2);



        Game game = new Game();
        game.setId(1L);
        game.setName("testName");
        game.setStatus(GameStatus.WAITING);
        game.setCurrentPlayerId(2L);
        game.setChat(chat);
        game.initGame();

        // this mocks the GameService
        given(gameService.getGame(Mockito.anyLong())).willReturn(game);

        // when
        MockHttpServletRequestBuilder getRequest = get("/chat/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time", is(100)))
                .andExpect(jsonPath("$[0].message", is("hello")))
                .andExpect(jsonPath("$[0].username", is("test")))
                .andExpect(jsonPath("$[1].time", is(101)))
                .andExpect(jsonPath("$[1].message", is("hello1")))
                .andExpect(jsonPath("$[1].username", is("test1")))
                .andExpect(jsonPath("$[2].time", is(102)))
                .andExpect(jsonPath("$[2].message", is("hello2")))
                .andExpect(jsonPath("$[2].username", is("test2")));

    }
}
