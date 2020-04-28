package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

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

    @Qualifier("tileRepository")
    @Autowired
    private TileRepository tileRepository;

    @Autowired
    private GameService gameService;

    private Game game;
    private Player player;
    private User user;

    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();

        // setup user
        user = new User();
        user.setId(2L);

        // setup game
        game = new Game();
        game.setOwner(user);
        game.setName("TestName");
        game.setPassword("TestPassword");

        // setup player
        player = new Player();
        player.setId(2);
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
        assertEquals(createdGame.getName(), game.getName());
        assertEquals(createdGame.getPassword(), game.getPassword());
        assertEquals(createdGame.getOwner().getId(), game.getOwner().getId());
        assertEquals(createdGame.getStatus(), GameStatus.WAITING);
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
