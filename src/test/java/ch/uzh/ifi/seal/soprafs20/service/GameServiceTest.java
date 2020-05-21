package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
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

    @Mock
    private TileRepository tileRepository;

    @InjectMocks
    private GameService gameService;
    private Game testGame;
    private Player testPlayer;
    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // given user
        testUser = new User();
        testUser.setId(2L);
        testUser.setToken("testToken");

        // given game
        testGame = new Game();
        testGame.setId(99);
        testGame.setOwner(testUser);
        testGame.setName("testName");
        testGame.setPassword("testPassword");
        testGame.initGame();

        //given tile
        Tile testTile = new Tile(1, null, null);


        // given player
        testPlayer = new Player();
        testPlayer.setUser(testUser);
        testPlayer.setId(2L);
        testPlayer.setUsername("testUsername");
        testPlayer.setScore(100);
        testPlayer.initPlayer();

        // when -> any object is being save in the gameRepository -> return the dummy testGame
        Mockito.when(gameRepository.save(Mockito.any())).thenReturn(testGame);
        Mockito.when(tileRepository.save(Mockito.any())).thenReturn(testTile);

        Mockito.when(tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(Mockito.anyInt(), Mockito.eq(null),Mockito.anyString()))
                .thenReturn(Optional.of(testTile));
    }

    @Test
    public void getGame_validInput_success() {
        // given game
        Game createdGame = gameService.createGame(testGame, testPlayer);
        Optional<Game> found = Optional.ofNullable(createdGame);

        // when
        Mockito.when(gameRepository.findByIdIs(Mockito.anyLong())).thenReturn(found);

        // search game
        assert createdGame != null;
        Game foundGame = gameService.getGame(99);

        // then check if they are equal
        assertEquals(createdGame.getName(), foundGame.getName());
        assertEquals(createdGame.getStatus(), foundGame.getStatus());
        assertEquals(createdGame.getOwner().getId(), foundGame.getOwner().getId());
        assertEquals(createdGame.getId(), foundGame.getId());
    }

    @Test void getGame_notExistingGame_throwsException() {
        // given
        Optional<Game> found = Optional.empty();

        // when/then
        Mockito.when(gameRepository.findById(Mockito.anyLong())).thenReturn(found);

        // test
        String exceptionMessage = "The game with the id 404 is not existing.";
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> gameService.getGame(404), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void createGame_validInputs_success() {
        // given game
        Game createdGame = gameService.createGame(testGame, testPlayer);

        // when/then
        Mockito.verify(gameRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testGame.getOwner().getId(), createdGame.getOwner().getId());
        assertEquals(testGame.getName(), createdGame.getName());
        assertNotNull(testGame.getPlayers());
        assertEquals(GameStatus.WAITING, createdGame.getStatus());
    }

    @Test
    public void createGame_duplicateInputs_throwsException() {
        // given -> a first game has already been created
        gameService.createGame(testGame, testPlayer);
        //Optional<Game> found = Optional.ofNullable(testGame);

        // when
        testUser.setGame(testGame);

        // then -> attempt to create second user with same user -> check that an error is thrown
        String exceptionMessage = "The user with the id 2 is hosting another game.";
        ConflictException exception = assertThrows(ConflictException.class,
                () -> gameService.createGame(testGame, testPlayer), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void startGame_successful() {
        // given
        Player player = new Player();
        player.setUsername("player");
        player.setStatus(PlayerStatus.READY);
        player.setId(3L);
        player.setScore(0);
        player.initPlayer();

        testGame.setStatus(GameStatus.WAITING);
        testGame.addPlayer(testPlayer);
        testGame.addPlayer(player);

        gameService.startGame(testGame, testPlayer.getUser().getToken());

        assertNotNull(testPlayer.getBag());
        assertNotNull(player.getBag());
        assertNotNull(testGame.getBag());
        assertEquals(GameStatus.RUNNING, testGame.getStatus());
    }

    @Test
    public void checkIfGameEnded_successful() {
        testGame.addPlayer(testPlayer);

        gameService.checkIfGameEnded(testGame);

        assertEquals(GameStatus.ENDED, testGame.getStatus());
    }

    @Test
    public void updateScore_afterGameEnded_successful() {
        // given
        User user = new User();
        Player player = new Player();
        player.setScore(200);
        player.setUser(user);
        player.initPlayer();

        testGame.setStatus(GameStatus.RUNNING);
        testGame.addPlayer(testPlayer);
        testGame.addPlayer(player);

        gameService.checkIfGameEnded(testGame);

        assertEquals(100, testUser.getOverallScore());
        assertEquals(200, user.getOverallScore());
    }

    @Test
    public void manageHistory_successful() {
        // given
        User user = new User();
        Player player = new Player();
        player.setScore(200);
        player.setUser(user);
        player.initPlayer();

        gameService.manageHistory(player,user);

        assertEquals("200 ", user.getHistory());
    }

    @Test
    public void manageHistory_multiple_matches_successful() {
        // given
        User user = new User();
        Player player = new Player();
        player.setScore(200);
        player.setUser(user);
        player.initPlayer();

        gameService.manageHistory(player,user);

        player.setScore(300);
        gameService.manageHistory(player,user);

        player.setScore(400);
        gameService.manageHistory(player,user);

        assertEquals("200 300 400 ", user.getHistory());
    }

    @Test
    public void manageHistory_more_than_ten_matches_successful() {
        // check if its got more matches the oldest one gets deleted
        User user = new User();
        Player player = new Player();
        player.setScore(200);
        player.setUser(user);
        player.initPlayer();

        gameService.manageHistory(player,user);

        player.setScore(300);
        gameService.manageHistory(player,user);

        player.setScore(400);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);
        gameService.manageHistory(player,user);

        assertEquals("300 400 400 400 400 400 400 400 400 400 ", user.getHistory());
    }

    @Test
    public void manageHistoryTime_successful() {
        // given
        User user = new User();

        gameService.manageHistoryTime(user);

        assertEquals((Long.toString(System.currentTimeMillis())+ " ").substring(0,(Long.toString(System.currentTimeMillis())+ " ").length() - 4), user.getHistoryTime().substring(0,user.getHistoryTime().length()-4));
    }

    @Test
    public void update_score_successful(){

        User user = new User();
        Player player = new Player();
        player.setScore(200);
        player.setUser(user);
        player.initPlayer();

        User user1 = new User();
        Player player1 = new Player();
        player1.setScore(100);
        player1.setUser(user1);
        player1.initPlayer();


        testGame.setStatus(GameStatus.RUNNING);
        testGame.addPlayer(testPlayer);
        testGame.addPlayer(player);
        testGame.addPlayer(player1);


        gameService.checkIfGameEnded(testGame);

        assertEquals(100, testUser.getOverallScore());
        assertEquals(200, user.getOverallScore());
        assertEquals(1, user.getWonGames());
        assertEquals(0, user1.getWonGames());




    }


}

