package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Chat;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.repository.ChatRepository;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for the GameResource REST resource
 *
 * @see GameService
 */

@Transactional
@WebAppConfiguration
@SpringBootTest
public class GameServiceIntegrationTest {

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Qualifier("chatRepository")
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private GameService gameService;

    private Game game;
    private Player player;
    private User user;
    private Chat chat;

    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();

        // setup user
        user = new User();
        user.setId(2L);
        user.setToken("testToken");
        user.setUsername("user");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        user.setCakeDay(new Date());

        userRepository.save(user);
        userRepository.flush();

        //setup chat
        chat = new Chat();
        chat.setId(2L);

        chatRepository.save(chat);
        chatRepository.flush();


        // setup game
        game = new Game();
        game.setOwner(user);
        game.setName("TestName");
        game.setPassword("TestPassword");

        // setup player
        player = new Player();
        player.setId(2L);
        player.setUser(user);
        player.setScore(0);
        player.setStatus(PlayerStatus.NOT_READY);
        player.setUsername("TestUsername");
    }

    @Test
    public void getGame_validInput() {
        // given game
        Game createdGame = gameService.createGame(game, player);

        // search game
        Game foundGame = gameService.getGame(createdGame.getId());

        // then check if they are equal
        assertEquals(createdGame.getName(), foundGame.getName());
        assertEquals(createdGame.getStatus(), foundGame.getStatus());
        assertEquals(createdGame.getOwner().getId(), foundGame.getOwner().getId());
        assertEquals(createdGame.getId(), foundGame.getId());
    }

    @Test
    public void createGame_validInput() {
        // when
        Game createdGame = gameService.createGame(game, player);

        // then
        assertEquals(game.getName(), createdGame.getName());
        assertEquals(game.getPassword(), createdGame.getPassword());
        assertEquals(game.getOwner().getId(), createdGame.getOwner().getId());
        assertEquals(GameStatus.WAITING, createdGame.getStatus());
    }

    @Test
    public void createGame_duplicateOwner_throwsException() {
        // given first game creation
        gameService.createGame(game, player);
        user.setGame(game);

        // check that an error is thrown
        String exceptionMessage = "The user with the id 2 is hosting another game.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> gameService.createGame(game, player), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
