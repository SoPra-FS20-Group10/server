package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
            throw new SopraServiceException("The game with the id " + gameId + " is not existing.");
        } else {
            game = foundGame.get();
        }

        return game;
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public void createGame() {

        Game newgame = new Game();
        newgame.setStatus(GameStatus.ONLINE);

        gameRepository.save(newgame);
        gameRepository.flush();
    }

    public void joinGame(long gameId, long playerId) {
        // fetch the game by id
        Game game;
        Optional<Game> foundGame = gameRepository.findById(gameId);

        // check if the game exists
        if (foundGame.isEmpty()) {
            // TODO: throw the right exception
            throw new SopraServiceException("The game with the id " + gameId + " does not exist.");
        } else {
            game = foundGame.get();
        }

        // fetch the player by id
        Player player;
        Optional<Player> foundPlayer = playerRepository.findById(playerId);

        //check if the player exists
        if (foundPlayer.isEmpty()) {
            // TODO: throw the right exception
            throw new SopraServiceException("The game with the id " + gameId + " does not exist.");
        } else {
            player = foundPlayer.get();
        }

        // add player to the game
        game.addPlayer(player);

        // save the game
        gameRepository.save(game);
        gameRepository.flush();
    }
}
