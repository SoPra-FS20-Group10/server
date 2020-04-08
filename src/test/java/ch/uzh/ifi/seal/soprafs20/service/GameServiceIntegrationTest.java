package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the GameResource REST resource
 *
 * @see GameService
 */
@WebAppConfiguration
@SpringBootTest
public class GameServiceIntegrationTest {

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();
    }

    @Test
    public void createGame_validInput() {
        // given game
        Game game = new Game();
        game.setOwnerId(1);
        game.setName("TestName");
        game.setPassword("TestPassword");

        // given player
        Player player = new Player();
        player.setId(1);
        player.setUsername("TestUsername");
        player.setScore(0);

        // when
        Game createdGame = gameService.createGame(game, player);

        // then
        assertEquals(createdGame.getName(), game.getName());
        assertEquals(createdGame.getPassword(), game.getPassword());
        assertEquals(createdGame.getOwnerId(), game.getOwnerId());
        assertEquals(createdGame.getStatus(), GameStatus.WAITING);
    }

    @Test
    public void createGame_duplicateOwner_throwsException() {
        // given game
        Game game = new Game();
        game.setOwnerId(1);
        game.setName("TestName");
        game.setPassword("TestPassword");

        // given player
        Player player = new Player();
        player.setId(1);
        player.setUsername("TestUsername");
        player.setScore(0);

        // given first game creation
        gameService.createGame(game, player);

        // check that an error is thrown
        String exceptionMessage = "The user with the id 1 is hosting another game.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> gameService.createGame(game, player), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
