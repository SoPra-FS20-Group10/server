package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository")GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game getGame(long gameId) {
        // fetch game from db
        Game game;
        Optional<Game> foundGame = gameRepository.findByIdIs(gameId);

        // check if game exists
        if (foundGame.isEmpty()) {
            throw new NotFoundException("The game with the id " + gameId + " is not existing.");
        } else {
            game = foundGame.get();
        }

        return game;
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public Game createGame(Game game, Player owner) {
        // check if owner has no other game
        if (owner.getUser().getGame() != null) {
            throw new ConflictException("The user with the id " + owner.getUser().getId() + " is hosting another game.");
        }

        //game.setChat(new Chat());
        game.setOwner(owner.getUser());
        game.setStatus(GameStatus.WAITING);

        // initialise list and add player
        game.initGame();
        game.addPlayer(owner);

        // save changes
        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
    }

    public Game joinGame(long gameId, Player player, String password) {
        // fetch game from db
        Game game = getGame(gameId);

        // check if password is correct
        if (!game.getPassword().equals(password)) {
            throw new ConflictException("Wrong password. Therefore the player could not join the game");
        }

        // add player to the game
        game.addPlayer(player);

        // save the game
        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
    }

    public void startGame(Game game, String token) {
        // check if user is authorized to start the game
        if (!game.getOwner().getToken().equals(token)) {
            throw new UnauthorizedException("The user is not authorized to start the game");
        }
        // check if all players are ready
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            if (player.getStatus() == PlayerStatus.NOT_READY) {
                throw new ConflictException("Not all players are ready to start.");
            }
        }

        // check if the game can be started
        if (game.getStatus() == GameStatus.RUNNING) {
            throw new ConflictException("The game is already running.");
        } else if (game.getStatus() == GameStatus.ENDED) {
            throw new ConflictException("The game has already ended.");
        }

        // add stones to the game
        addAllStones(game);

        // set flag to running
        game.setStatus(GameStatus.RUNNING);

        // save change
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void leaveGame(Game game, Player player, String token) {
        // check if player is the lobbyLeader
        if (player.getUser().getId().equals(game.getOwner().getId())) {
            throw new UnauthorizedException("The game owner cannot leave the game. Choose to end the game.");
        }

        // check if the user is authorized to leave the game
        if (!player.getUser().getToken().equals(token)) {
            throw new ConflictException("The user is not authorized to leave this game");
        }

        // remove the player
        game.removePlayer(player);

        // save changes
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void endGame(long gameId, String token) {
        // fetch game from db
        Game game = getGame(gameId);

        //check if user is authorized to end game
        if (!game.getOwner().getToken().equals(token)) {
            throw new UnauthorizedException("The game can not be ended by this user");
        }

        // delete the game
        gameRepository.delete(game);
        gameRepository.flush();
    }

    public void addScores(Game game){
        //adds the scores at the end of a game to the user

        List<Player> players = game.getPlayers();
        for(Player player: players){
            User user = player.getUser();
            user.setOverallScore(user.getOverallScore() + player.getScore());
            player.setScore(0);
        }

    }

    public List<Player> getPlayers(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        return game.getPlayers();
    }

    private void addAllStones(Game game) {
        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("a", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("b", 3));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("c", 3));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("d", 2));
        }

        for (int i = 0; i < 13; i++) {
            game.addStone(new Stone("e", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("f", 4));
        }

        for (int i = 0; i < 4; i++) {
            game.addStone(new Stone("g", 2));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("h", 4));
        }

        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("i", 1));
        }

        game.addStone(new Stone("j", 8));

        game.addStone(new Stone("k", 5));

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("l", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("m", 3));
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("n", 1));
        }

        for (int i = 0; i < 9; i++) {
            game.addStone(new Stone("o", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("p", 3));
        }

        game.addStone(new Stone("q", 10));

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("r", 1));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("s", 1));
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("t", 1));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("u", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("v", 4));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("w", 4));
        }

        game.addStone(new Stone("x", 8));

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("y", 4));
        }

        game.addStone(new Stone("z", 10));
    }
}
