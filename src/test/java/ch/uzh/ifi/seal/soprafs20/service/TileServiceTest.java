package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TileServiceTest {

    @Mock
    private TileRepository tileRepository;

    @InjectMocks
    private GameService tileService;
    private Game testGame;
    private Player testPlayer;
    private User testUser;
    private Tile testTile;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given user
        testUser = new User();
        testUser.setId(2L);

        // given game
        testGame = new Game();
        testGame.setId(99);
        testGame.setOwner(testUser);
        testGame.setName("testName");
        testGame.setPassword("testPassword");

        //given tile
        testTile = new Tile();
        testTile.setMultiplier(1);
        testTile.setStoneSymbol("empty");


        // given player
        testPlayer = new Player();
        testPlayer.setUser(testUser);
        testPlayer.setId(2L);
        testPlayer.setUsername("testUsername");
        testPlayer.setScore(100);

        // when -> any object is being save in the gameRepository -> return the dummy testGame
        Mockito.when(tileRepository.save(Mockito.any())).thenReturn(testTile);
    }

    @Test
    public void test_tile_search() {

        tileRepository.save(testTile);
        tileRepository.flush();
        Optional<Tile> found = tileRepository.findByMultiplierAndStoneSymbol(1,"empty");
        found.get();


    }

}
