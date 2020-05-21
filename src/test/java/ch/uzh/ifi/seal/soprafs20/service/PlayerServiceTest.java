package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private User testUser;
    private Player testPlayer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        // given
        testUser = new User();
        testPlayer = new Player();

        // init testUser
        testUser.setId(2L);
        testUser.setUsername("General Kenobi");
        testUser.setStatus(UserStatus.ONLINE);

        // init createdPlayer
        testPlayer.setId(2L);
        testPlayer.setUser(testUser);
        testPlayer.setUsername("General Kenobi");
        testPlayer.setStatus(PlayerStatus.NOT_READY);
        testPlayer.initPlayer();

        // mock the playerRepository
        Mockito.when(playerRepository.save(Mockito.any())).thenReturn(testPlayer);
    }

    @Test
    void getPlayer_validInput_playerFound() {
        // mock the playerRepository
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testPlayer));

        Player player = playerService.getPlayer(1);

        assertEquals(2, player.getId());
        assertEquals("General Kenobi", player.getUsername());
        assertEquals(testUser, player.getUser());
        assertEquals(PlayerStatus.NOT_READY, player.getStatus());
    }

    @Test
    void getPlayer_validInput_noPlayerFound() {
        // mock the playerRepository
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        // then -> attempt to fetch player from db -> check that an error is thrown
        String exceptionMessage = "The player with the id 2 does not exist.";
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> playerService.getPlayer(2L), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    void getStones_validInput() {
        // given
        List<Stone> bag;
        Game game = new Game();
        game.setId(1L);

        // add game to player
        testPlayer.setGame(game);

        // mock the playerRepository
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testPlayer));

        bag = playerService.getStones(2, 1);

        assertEquals(0, bag.size());
    }

    @Test
    void getStones_invalidInput_wrongGame() {
        // given
        List<Stone> bag;
        Game game = new Game();
        game.setId(1L);

        // add game to player
        testPlayer.setGame(game);

        // mock the playerRepository
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testPlayer));

        // then -> attempt to create second player with same user -> check that an error is thrown
        String exceptionMessage = "The player is playing in another game.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> playerService.getStones(2, 2), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    void createPlayer_validInput() {
        // mock the playerRepository
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        // create player
        testPlayer = playerService.createPlayer(testUser);

        // then
        Mockito.verify(playerRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testUser.getId(), testPlayer.getId());
        assertEquals(testUser.getUsername(), testPlayer.getUsername());
        assertEquals(testUser, testPlayer.getUser());
    }

    @Test
    void createPlayer_invalidInput_alreadyExistingPlayer() {
        // create first player
        playerService.createPlayer(testUser);

        // mock the playerRepository
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testPlayer));

        // then -> attempt to create second player with same user -> check that an error is thrown
        String exceptionMessage = "The user is already in another game.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> playerService.createPlayer(testUser), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}