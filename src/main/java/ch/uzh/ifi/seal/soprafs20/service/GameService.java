package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
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
        Optional<Game> foundGame = gameRepository.findByOwnerId(game.getOwnerId());

        if (foundGame.isPresent()) {
            throw new ConflictException("The user with the id " + game.getOwnerId() + " is hosting another game.");
        }

        //game.setChat(new Chat());
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

    public void startGame(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

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

        // set flag to running
        game.setStatus(GameStatus.RUNNING);

        // save change
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void leaveGame(Game game, Player player, User user) {
        // check if the user is authorized to leave the game
        if (player.getId().equals(user.getId())) {
            throw new ConflictException("The user is not authorized to leave this game");
        }

        // remove the player
        game.removePlayer(player);

        // save changes
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void endGame(long gameId, Player player) {
        // fetch game from db
        Game game;
        Optional<Game> foundGame = gameRepository.findById(gameId);

        // check if game exists
        if (foundGame.isEmpty()) {
            throw new NotFoundException("The game with the id " + gameId + " could not be found.");
        } else {
            game = foundGame.get();
        }

        //check if user is authorized to end game
        if (!game.getOwnerId().equals(player.getId())) {
            throw new UnauthorizedException("The game can not be ended by this user");
        }

        // delete the game
        gameRepository.delete(game);
        gameRepository.flush();
    }

    public List<Player> getPlayers(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        return game.getPlayers();
    }
}
