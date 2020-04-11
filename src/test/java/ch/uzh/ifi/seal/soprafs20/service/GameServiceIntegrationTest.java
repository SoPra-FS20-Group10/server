package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.Optional;

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

    private Game game;
    private Player player;

    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();

        // setup game
        game = new Game();
        game.setOwnerId(1);
        game.setName("TestName");
        game.setPassword("TestPassword");

        // setup player
        player = new Player();
        player.setId(1);
        player.setUsername("TestUsername");
        player.setScore(0);
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
        assertEquals(createdGame.getOwnerId(), foundGame.getOwnerId());
        assertEquals(createdGame.getId(), foundGame.getId());
    }

    @Test
    public void createGame_validInput() {
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
        // given first game creation
        gameService.createGame(game, player);

        // check that an error is thrown
        String exceptionMessage = "The user with the id 1 is hosting another game.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> gameService.createGame(game, player), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
