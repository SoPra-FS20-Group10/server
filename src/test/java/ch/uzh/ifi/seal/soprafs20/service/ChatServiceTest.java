package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.repository.ChatRepository;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatService chatService;
    private Game testGame;
    private Player testPlayer;
    private User testUser;
    private Chat chat;
    private Message message;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given user
        testUser = new User();
        testUser.setId(2L);
        testUser.setToken("testToken");

        chat = new Chat();
        chat.initchat();

        message = new Message();
        message.setMessage("hello");
        message.setUsername("test");
        message.setTime(100L);

        chat.addMessage(message);

        // given game
        testGame = new Game();
        testGame.setId(99);
        testGame.setOwner(testUser);
        testGame.setName("testName");
        testGame.setPassword("testPassword");
        testGame.setChat(chat);
        testGame.initGame();


        // given player
        testPlayer = new Player();
        testPlayer.setUser(testUser);
        testPlayer.setId(2L);
        testPlayer.setUsername("testUsername");
        testPlayer.setScore(100);
        testPlayer.initPlayer();

        // when -> any object is being save in the gameRepository -> return the dummy testGame
        Mockito.when(chatRepository.save(Mockito.any())).thenReturn(chat);
        Mockito.when(messageRepository.save(Mockito.any())).thenReturn(message);

    }

    @Test
    public void add_message_test() {

        Chat addedchat = chatService.addMessage(chat,message);

        Message addedmessage = chat.getMessages().get(0);

        // search game

        // then check if they are equal
        assertEquals(addedmessage.getMessage(),message.getMessage());
        assertEquals(addedmessage.getUsername(),message.getUsername());
        assertEquals(addedmessage.getTime(),message.getTime());


    }

}
