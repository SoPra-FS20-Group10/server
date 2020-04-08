package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;
    private Game testGame;
    private Player testPlayer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given game
        testGame = new Game();
        testGame.setOwnerId(1L);
        testGame.setName("testName");
        testGame.setPassword("testPassword");

        // given player
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setUsername("testUsername");

        // when -> any object is being save in the gameRepository -> return the dummy testGame
        Mockito.when(gameRepository.save(Mockito.any())).thenReturn(testGame);
    }

    @Test
    public void createGame_validInputs_success() {
        // when -> any object is being save in the userRepository -> return the dummy testUser
        Game createdGame = gameService.createGame(testGame, testPlayer);

        // then
        Mockito.verify(gameRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testGame.getOwnerId(), createdGame.getOwnerId());
        assertEquals(testGame.getName(), createdGame.getName());
        assertNotNull(testGame.getPlayers());
        assertEquals(GameStatus.WAITING, createdGame.getStatus());
    }

    @Test
    public void createGame_duplicateInputs_throwsException() {
        // given -> a first game has already been created
        gameService.createGame(testGame, testPlayer);
        Optional<Game> found = Optional.ofNullable(testGame);

        // when
        Mockito.when(gameRepository.findByOwnerId(Mockito.anyLong())).thenReturn(found);

        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "The user with the id 1 is hosting another game.";
        SopraServiceException exception = assertThrows(SopraServiceException.class,
                () -> gameService.createGame(testGame, testPlayer), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}

