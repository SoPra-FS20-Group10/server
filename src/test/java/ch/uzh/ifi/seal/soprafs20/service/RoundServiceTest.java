package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.StoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class RoundServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StoneRepository stoneRepository;

    @InjectMocks
    private RoundService roundService;
    private Game testGame;
    private Player testPlayer;
    private Stone stone1, stone2;
    private Bag bag1, bag2;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given player
        testPlayer = new Player();
        testPlayer.setId(2L);
        testPlayer.setUsername("testUsername");
        testPlayer.setScore(100);

        // given game
        testGame = new Game();
        testGame.setId(99);
        testGame.setName("testName");
        testGame.setPassword("testPassword");

        stone1 = new Stone();
        stone1.setId(1L);
        stone1.setSymbol("a");
        stone1.setValue(3);

        stone2 = new Stone();
        stone2.setId(2L);
        stone2.setSymbol("b");
        stone2.setValue(5);

        bag1 = new Bag();
        testGame.setBag(bag1);
        bag1.setStones(new ArrayList<>());

        bag2 = new Bag();
        testPlayer.setBag(bag2);
        bag2.setStones(new ArrayList<>());

        // when -> any object is being save in the gameRepository -> return the dummy testGame
        Mockito.when(gameRepository.save(Mockito.any())).thenReturn(testGame);
    }

    @Test
    public void test_pointCalculation_validInput() {
        Tile tile1 = new Tile();
        tile1.setMultiplier(1);
        tile1.setValue(4);

        Tile tile2 = new Tile();
        tile2.setMultiplier(2);
        tile2.setValue(1);

        Tile tile3 = new Tile();
        tile3.setMultiplier(3);
        tile3.setValue(6);

        Tile tile4 = new Tile();
        tile4.setMultiplier(1);
        tile4.setValue(3);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(84, roundService.calculatePoints(tiles));
    }

    @Test
    public void test_exchangeStones_successful() {
        List<Stone> stonesGame = new ArrayList<>();
        List<Stone> stonesPlayer = new ArrayList<>();
        List<Long> request = new ArrayList<>();

        stonesGame.add(stone1);
        stonesPlayer.add(stone2);
        request.add(stone2.getId());

        bag1.setStones(stonesGame);
        bag2.setStones(stonesPlayer);

        testGame.getBag().setStones(stonesGame);
        testPlayer.getBag().setStones(stonesPlayer);

        Mockito.when(stoneRepository.findByIdIs(Mockito.anyLong())).thenReturn(Optional.ofNullable(stone2));
        Mockito.when(gameRepository.findByIdIs(Mockito.anyLong())).thenReturn(java.util.Optional.ofNullable(testGame));
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(java.util.Optional.ofNullable(testPlayer));

        List<Stone> exchanged = roundService.exchangeStone(1, 1, request);

        assertEquals(1, exchanged.size());
        assertEquals(stone2.getId(), testGame.getBag().getStones().get(0).getId());
        assertEquals(stone1.getId(), testPlayer.getBag().getStones().get(0).getId());
    }
}
