package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.*;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.StoneRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
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
import static org.mockito.ArgumentMatchers.eq;


public class RoundServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StoneRepository stoneRepository;

    @Mock
    private TileRepository tileRepository;

    @InjectMocks
    private RoundService roundService;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private Player testPlayer;
    private Stone stone1, stone2;

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
        stone1.setSymbol("g");
        stone1.setValue(3);

        stone2 = new Stone();
        stone2.setId(2L);
        stone2.setSymbol("o");
        stone2.setValue(5);

        Tile tile = new Tile(1, null, "l");

        // when -> any object is being save in the gameRepository -> return the dummy testGame
        Mockito.when(gameRepository.save(Mockito.any())).thenReturn(testGame);

        // when -> tile is searched -> return tile
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(Mockito.anyInt(), Mockito.isNull(),
                Mockito.anyString())).thenReturn(Optional.of(tile));
    }

    @Test
    public void test_pointCalculation_no_multiplier() {
        Tile tile1 = new Tile(1, null,"l");
        tile1.setValue(4);

        Tile tile2 = new Tile(1, null,"w");
        tile2.setValue(1);

        Tile tile3 = new Tile(1, null,"l");
        tile3.setValue(6);

        Tile tile4 = new Tile(1, null,"l");
        tile4.setValue(3);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(14, roundService.calculatePoints(tiles));
    }

    @Test
    public void test_pointCalculation_doubleLetter() {
        Tile tile1 = new Tile(2, null,"l");
        tile1.setValue(4);

        Tile tile2 = new Tile(1, null,"w");
        tile2.setValue(1);

        Tile tile3 = new Tile(1, null,"l");
        tile3.setValue(6);

        Tile tile4 = new Tile(2, null,"l");
        tile4.setValue(3);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(21, roundService.calculatePoints(tiles));
    }

    @Test
    public void test_pointCalculation_tripleLetter() {
        Tile tile1 = new Tile(3, null,"l");
        tile1.setValue(4);

        Tile tile2 = new Tile(1, null,"w");
        tile2.setValue(1);

        Tile tile3 = new Tile(1, null,"l");
        tile3.setValue(6);

        Tile tile4 = new Tile(3, null,"l");
        tile4.setValue(3);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(28, roundService.calculatePoints(tiles));
    }

    @Test
    public void test_pointCalculation_doubleWord() {
        Tile tile1 = new Tile(1, null,"l");
        tile1.setValue(4);

        Tile tile2 = new Tile(2, null,"w");
        tile2.setValue(2);

        Tile tile3 = new Tile(1, null,"l");
        tile3.setValue(6);

        Tile tile4 = new Tile(3, null,"l");
        tile4.setValue(2);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(36, roundService.calculatePoints(tiles));
    }

    @Test
    public void test_pointCalculation_tripleWord() {
        Tile tile1 = new Tile(1, null,"l");
        tile1.setValue(4);

        Tile tile2 = new Tile(3, null,"w");
        tile2.setValue(2);

        Tile tile3 = new Tile(1, null,"l");
        tile3.setValue(6);

        Tile tile4 = new Tile(3, null,"l");
        tile4.setValue(2);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(54, roundService.calculatePoints(tiles));
    }

    @Test
    public void test_pointCalculation_everything_mixed() {
        Tile tile1 = new Tile(2, null,"l");
        tile1.setValue(4);

        Tile tile2 = new Tile(3, null,"w");
        tile2.setValue(2);

        Tile tile3 = new Tile(1, null,"l");
        tile3.setValue(6);

        Tile tile4 = new Tile(3, null,"l");
        tile4.setValue(2);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);

        assertEquals(66, roundService.calculatePoints(tiles));
    }

    @Test
    public void get_currentPlayer_successful() {
        // given
        Player player = new Player();
        player.setId(25);

        testGame.initGame();
        testGame.addPlayer(testPlayer);
        testGame.addPlayer(player);

        assertEquals(player.getId(), roundService.getCurrentPlayer(testGame, testPlayer).getId());
        assertEquals(testPlayer.getId(), roundService.getCurrentPlayer(testGame, player).getId());
        assertEquals(player.getId(), roundService.getCurrentPlayer(testGame, testPlayer).getId());
    }

    @Test
    public void test_exchangeStones_successful() {
        List<Stone> stonesGame = new ArrayList<>();
        List<Stone> stonesPlayer = new ArrayList<>();
        List<Long> request = new ArrayList<>();

        stonesGame.add(stone1);
        stonesPlayer.add(stone2);
        request.add(stone2.getId());

        testGame.setBag(stonesGame);
        testPlayer.setBag(stonesPlayer);

        Mockito.when(stoneRepository.findByIdIs(Mockito.anyLong())).thenReturn(Optional.ofNullable(stone2));
        Mockito.when(gameRepository.findByIdIs(Mockito.anyLong())).thenReturn(java.util.Optional.ofNullable(testGame));
        Mockito.when(playerRepository.findById(Mockito.anyLong())).thenReturn(java.util.Optional.ofNullable(testPlayer));

        List<Stone> exchanged = roundService.exchangeStone(testGame, testPlayer, request);

        assertEquals(1, exchanged.size());
        assertEquals(stone2.getId(), testGame.getBag().get(0).getId());
        assertEquals(stone1.getId(), testPlayer.getBag().get(0).getId());
    }

    @Test
    public void drawStone_successful() {
        testPlayer.initPlayer();
        testGame.initGame();
        testGame.addStone(stone1);
        testGame.addStone(stone2);

        Stone stone = roundService.drawStone(testGame);
        testPlayer.addStone(stone);
        testGame.removeStone(stone);
        assertEquals(1, testPlayer.getBag().size());
        assertEquals(1, testGame.getBag().size());

        stone = roundService.drawStone(testGame);
        testPlayer.addStone(stone);
        testGame.removeStone(stone);
        assertEquals(2, testPlayer.getBag().size());
        assertEquals(0, testGame.getBag().size());

        assertTrue(testPlayer.getBag().contains(stone1));
        assertTrue(testPlayer.getBag().contains(stone2));
    }

    @Test
    public void test_placeWord_successful() {
        // given tiles, user, player and game
        Tile tile1 = new Tile(3, null, "w");
        Tile tile2 = new Tile(1, null, "l");
        Tile tile3 = new Tile(3, "g", "w");
        Tile tile4 = new Tile(1, "o", "l");

        User user = new User();
        user.setId(1L);
        user.setUsername("test");

        testPlayer.setUser(user);
        testPlayer.initPlayer();
        testPlayer.addStone(stone1);
        testPlayer.addStone(stone2);

        // create game
        gameService.createGame(testGame, testPlayer);

        // stones to place
        List<Long> stoneIds = new ArrayList<>();
        stoneIds.add(1L);
        stoneIds.add(2L);

        // where to place
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(0);
        coordinates.add(1);

        // when -> then
        Mockito.when(stoneRepository.findByIdIs(1)).thenReturn(Optional.ofNullable(stone1));
        Mockito.when(stoneRepository.findByIdIs(2)).thenReturn(Optional.ofNullable(stone2));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(3), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile1));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), Mockito.isNull(),
                eq("l"))).thenReturn(Optional.of(tile2));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("g"),
                eq("l"))).thenReturn(Optional.of(tile3));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("o"),
                eq("l"))).thenReturn(Optional.of(tile4));

        // test method
        roundService.placeWord(testGame, testPlayer, stoneIds, coordinates);

        // assertions
        assertEquals("g", testGame.getGrid().get(0).getStoneSymbol());
        assertEquals("o", testGame.getGrid().get(1).getStoneSymbol());
        assertEquals(1, testGame.getWords().size());
        assertEquals("go", testGame.getWords().get(0).getWord());
        assertTrue(testPlayer.getBag().isEmpty());
    }

    @Test
    public void test_placeWord_successful_twoWords_horizontalAndVertical() {
        // given tiles, user, player and game
        Tile tile1 = new Tile(3, null, "w");
        Tile tile2 = new Tile(1, null, "l");
        Tile tile3 = new Tile(3, "g", "w");
        Tile tile4 = new Tile(1, "o", "l");
        Tile tile5 = new Tile(2, null, "w");
        Tile tile6 = new Tile(2, "p", "w");

        Stone stone3 = new Stone("p", 3);
        stone3.setId(3L);

        User user = new User();
        user.setId(1L);
        user.setUsername("test");

        testPlayer.setUser(user);
        testPlayer.initPlayer();
        testPlayer.addStone(stone1);
        testPlayer.addStone(stone2);
        testPlayer.addStone(stone3);

        // create game
        gameService.createGame(testGame, testPlayer);

        // stones to place
        List<Long> stoneIds = new ArrayList<>();
        stoneIds.add(1L);
        stoneIds.add(2L);

        // where to place
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(0);
        coordinates.add(1);

        // when -> then stones
        Mockito.when(stoneRepository.findByIdIs(1)).thenReturn(Optional.of(stone1));
        Mockito.when(stoneRepository.findByIdIs(2)).thenReturn(Optional.of(stone2));
        Mockito.when(stoneRepository.findByIdIs(3)).thenReturn(Optional.of(stone3));

        // when -> then empty tiles
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(3), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile1));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), Mockito.isNull(),
                eq("l"))).thenReturn(Optional.of(tile2));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(2), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile5));

        // when -> tiles with stones
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("g"),
                eq("l"))).thenReturn(Optional.of(tile3));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("o"),
                eq("l"))).thenReturn(Optional.of(tile4));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("p"),
                eq("l"))).thenReturn(Optional.of(tile6));

        // test method
        roundService.placeWord(testGame, testPlayer, stoneIds, coordinates);

        // clean lists
        stoneIds.clear();
        coordinates.clear();

        // what and where to place
        stoneIds.add(3L);
        coordinates.add(16);

        // place second word
        roundService.placeWord(testGame, testPlayer, stoneIds, coordinates);

        // assertions
        assertEquals("g", testGame.getGrid().get(0).getStoneSymbol());
        assertEquals("o", testGame.getGrid().get(1).getStoneSymbol());
        assertEquals("p", testGame.getGrid().get(16).getStoneSymbol());
        assertEquals(2, testGame.getWords().size());
        assertEquals("go", testGame.getWords().get(0).getWord());
        assertEquals("op", testGame.getWords().get(1).getWord());
        assertTrue(testPlayer.getBag().isEmpty());
    }

    @Test
    void placeWord_unsuccessful_secondWordNotNextToFirstWord() {
        // given tiles, user, player and game
        Tile tile1 = new Tile(3, null, "w");
        Tile tile2 = new Tile(1, null, "l");
        Tile tile3 = new Tile(3, "g", "w");
        Tile tile4 = new Tile(1, "o", "l");
        Tile tile5 = new Tile(2, null, "w");
        Tile tile6 = new Tile(2, "p", "w");

        Stone stone3 = new Stone("p", 3);
        stone3.setId(3L);

        User user = new User();
        user.setId(1L);
        user.setUsername("test");

        testPlayer.setUser(user);
        testPlayer.initPlayer();
        testPlayer.addStone(stone1);
        testPlayer.addStone(stone2);
        testPlayer.addStone(stone2);
        testPlayer.addStone(stone3);

        // create game
        gameService.createGame(testGame, testPlayer);

        // stones to place
        List<Long> stoneIds = new ArrayList<>();
        stoneIds.add(1L);
        stoneIds.add(2L);

        // where to place
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(0);
        coordinates.add(1);

        // when -> then stones
        Mockito.when(stoneRepository.findByIdIs(1)).thenReturn(Optional.of(stone1));
        Mockito.when(stoneRepository.findByIdIs(2)).thenReturn(Optional.of(stone2));
        Mockito.when(stoneRepository.findByIdIs(3)).thenReturn(Optional.of(stone3));

        // when -> then empty tiles
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(3), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile1));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), Mockito.isNull(),
                eq("l"))).thenReturn(Optional.of(tile2));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(2), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile5));

        // when -> tiles with stones
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("g"),
                eq("l"))).thenReturn(Optional.of(tile3));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("o"),
                eq("l"))).thenReturn(Optional.of(tile4));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("p"),
                eq("l"))).thenReturn(Optional.of(tile6));

        // test method
        roundService.placeWord(testGame, testPlayer, stoneIds, coordinates);

        // clean lists
        stoneIds.clear();
        coordinates.clear();

        // what and where to place
        stoneIds.add(3L);
        stoneIds.add(2L);
        coordinates.add(31);
        coordinates.add(32);

        // then -> attempt to place the second word somewhere not connected to the first -> check that an error is thrown
        String exceptionMessage = "The played word is not connected to an already played word.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> roundService.placeWord(testGame, testPlayer, stoneIds, coordinates), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void test_placeWord_unsuccessful_twoWords_horizontalAndVerticalInOneTurn() {
        // given tiles, user, player and game
        Tile tile1 = new Tile(3, null, "w");
        Tile tile2 = new Tile(1, null, "l");
        Tile tile3 = new Tile(3, "g", "w");
        Tile tile4 = new Tile(1, "o", "l");
        Tile tile5 = new Tile(2, null, "w");
        Tile tile6 = new Tile(2, "p", "w");

        Stone stone3 = new Stone("p", 3);
        stone3.setId(3L);

        User user = new User();
        user.setId(1L);
        user.setUsername("test");

        testPlayer.setUser(user);
        testPlayer.initPlayer();
        testPlayer.addStone(stone1);
        testPlayer.addStone(stone2);
        testPlayer.addStone(stone3);

        // create game
        gameService.createGame(testGame, testPlayer);

        // stones to place
        List<Long> stoneIds = new ArrayList<>();
        stoneIds.add(1L);
        stoneIds.add(2L);
        stoneIds.add(3L);

        // where to place
        List<Integer> coordinates = new ArrayList<>();
        coordinates.add(0);
        coordinates.add(1);
        coordinates.add(16);

        // when -> then stones
        Mockito.when(stoneRepository.findByIdIs(1)).thenReturn(Optional.of(stone1));
        Mockito.when(stoneRepository.findByIdIs(2)).thenReturn(Optional.of(stone2));
        Mockito.when(stoneRepository.findByIdIs(3)).thenReturn(Optional.of(stone3));

        // when -> then empty tiles
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(3), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile1));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), Mockito.isNull(),
                eq("l"))).thenReturn(Optional.of(tile2));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(2), Mockito.isNull(),
                eq("w"))).thenReturn(Optional.of(tile5));

        // when -> tiles with stones
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("g"),
                eq("l"))).thenReturn(Optional.of(tile3));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("o"),
                eq("l"))).thenReturn(Optional.of(tile4));
        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(eq(1), eq("p"),
                eq("l"))).thenReturn(Optional.of(tile6));

        // then -> attempt to place the second word somewhere not connected to the first -> check that an error is thrown
        String exceptionMessage = "The letters can only be placed in one line.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> roundService.placeWord(testGame, testPlayer, stoneIds, coordinates), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
