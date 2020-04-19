package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// TODO: check again if stone and tile entity are implemented

@Service
@Transactional
public class RoundService {
    private final GameRepository gameRepository;

    @Autowired
    public RoundService(@Qualifier("gameRepository")GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    private Game getGame(long gameId) {
        // fetch game from db
        Game game;
        Optional<Game> foundGame = gameRepository.findByIdIs(gameId);

        // check if game exists
        if (foundGame.isEmpty()) {
            throw new NotFoundException("The game with the id " + gameId + " is not existing.");
        }
        else {
            game = foundGame.get();
        }

        return game;
    }

    public Player getCurrentPlayer(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        // fetch current player and reposition it to the last place
        Player player = game.getPlayers().get(0);
        game.removePlayer(player);
        game.addPlayer(player);

        return player;
    }

    public Stone drawStone(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        // get stones from game
        List<Stone> stones = game.getBag().getStones();

        // draw a random stone
        int random = new Random().nextInt() % stones.size();
        Stone stone = stones.get(random);

        // remove stone from game
        game.getBag().removeStone(stone);

        // return
        return stone;
    }

    public void placeStoneValid(long gameId, Stone stone, int coordinate) {
        // fetch game from db
        Game game = getGame(gameId);

        // fetch board from game, grid from board
        List<Tile> grid = game.getBoard().getGrid();

        // check if tile-to-be-covered is empty
        if (grid.get(coordinate).getStonesymbol() != null) {
            throw new ConflictException("This field has already a stone on it, thus this stone could not be placed");
        }
    }

    public void placeStone(long gameId, Stone stone, int coordinate) {
        // fetch game from db
        Game game = getGame(gameId);

        // fetch board from game
        List<Tile> grid = game.getBoard().getGrid();

        // TODO: change after stone is implemented
        grid.get(coordinate).setStonesymbol(stone.toString());
    }

    public int calculatePoints(List<Stone> word, List<Tile> tiles) {
        assert(word.size() == tiles.size());

        // define sum and multiplicand
        int sum = 0;
        int multiplicand = 1;

        // calculate sum and multiplicand
        for (int i = 0; i < word.size(); ++i) {
            sum += word.get(i).getValue();
            multiplicand *= tiles.get(i).getMultiplier();
        }

        // deploy multiplications
        sum *= multiplicand;

        return sum;
    }
}
