package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository")GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public void createGame(Game game) {
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void joinGame(long gameId, long playerId) {
        Game game;
        Optional<Game> found = gameRepository.findById(gameId);

        if (found.isEmpty()) {
            throw new SopraServiceException("The game with the id " + gameId + " does not exist.");
        } else {
            game = found.get();
        }

        //game.addPlayer(playerId);

        gameRepository.save(game);
        gameRepository.flush();
    }
}
