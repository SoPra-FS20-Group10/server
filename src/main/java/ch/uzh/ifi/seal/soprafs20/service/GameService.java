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

    public long createGame(Game game, Player owner) {
        // check if owner has no other game
        Optional<Game> foundGame = gameRepository.findByOwnerId(game.getOwnerId());

        if (foundGame.isPresent()) {
            // TODO: throw the correct exception
            throw new SopraServiceException("The user with the id " + game.getOwnerId() + " is hosting another game.");
        }

        // TODO: insert correct chatId
        game.setChatId(-1);
        game.setStatus(GameStatus.ONLINE);
        game.initList();
        game.addPlayer(owner);

        gameRepository.save(game);
        gameRepository.flush();

        return gameRepository.findByOwnerId(owner.getId()).get().getId();
    }

    public void joinGame(long gameId, Player player, String password) {
        // fetch the game by id
        Game game;
        Optional<Game> foundGame = gameRepository.findById(gameId);

        // check if the game exists
        if (foundGame.isEmpty()) {
            // TODO: throw the correct exception
            throw new SopraServiceException("The game with the id " + gameId + " does not exist.");
        } else {
            game = foundGame.get();
        }

        // check if password is correct
        if (!game.getPassword().equals(password)) {
            // TODO: throw the correct exception
            throw new SopraServiceException("Wrong password. Therefore the player could not join the game");
        }

        // add player to the game
        game.addPlayer(player);

        // save the game
        gameRepository.save(game);
        gameRepository.flush();
    }
}
