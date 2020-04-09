package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
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
    private final PlayerRepository playerRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository")GameRepository gameRepository,
                       @Qualifier("playerRepository")PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public Game getGame(long gameId) {
        Game game;
        Optional<Game> foundGame = gameRepository.findById(gameId);

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

        game.initGame();
        game.addPlayer(owner);

        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
    }

    public void joinGame(long gameId, Player player, String password) {
        // fetch the game by id
        Game game;
        Optional<Game> foundGame = gameRepository.findById(gameId);

        // check if the game exists
        if (foundGame.isEmpty()) {
            throw new NotFoundException("The game with the id " + gameId + " does not exist.");
        } else {
            game = foundGame.get();
        }

        // check if password is correct
        if (!game.getPassword().equals(password)) {
            throw new ConflictException("Wrong password. Therefore the player could not join the game");
        }

        // add player to the game
        game.addPlayer(player);

        // save the game
        gameRepository.save(game);
        gameRepository.flush();
    }
}
