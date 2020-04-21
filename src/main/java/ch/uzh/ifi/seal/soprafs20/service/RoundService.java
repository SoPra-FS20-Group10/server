package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.WordnikGetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// TODO: check again if stone and tile entity are implemented

@Service
@Transactional
public class RoundService {
    private final GameRepository gameRepository;
    private final TileRepository tileRepository;

    @Autowired
    public RoundService(@Qualifier("gameRepository")GameRepository gameRepository,
                        @Qualifier("tileRepository")TileRepository tileRepository) {
        this.gameRepository = gameRepository;
        this.tileRepository = tileRepository;
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

    private String getWord(List<Stone> letters) {
        StringBuilder word = new StringBuilder();

        for (Stone letter : letters) {
            word.append(letter.toString());
        }

        return word.toString();
    }

    public void checkWord(List<Stone> letters) {
        // get word as String
        String word = getWord(letters);

        // check if word is empty
        if (word.isEmpty()) {
            throw new ConflictException("Cannot look up an empty word.");
        }

        // url for request
        String uri = "https://api.wordnik.com/v4/word.json/" + word +
                "/definitions?limit=1&includeRelated=false&useCanonical=false&includeTags=false&" +
                "api_key=out7ek18doyp1jvmhndr9evfsj1jtsjd8piodr5vkbr47m2s9";

        // send request and get answer
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            WordnikGetDTO wordnikGetDTO = (WordnikGetDTO) connection.getContent();
        } catch (Exception exception) {
            throw new ConflictException(exception.getMessage());
        }
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
        if (grid.get(coordinate).getStoneSymbol() != null) {
            throw new ConflictException("This field has already a stone on it, thus this stone could not be placed");
        }
    }

    public void placeStone(long gameId, Stone stone, int coordinate) {
        Tile tile;

        // fetch game from db
        Game game = getGame(gameId);

        // fetch board from game
        List<Tile> grid = game.getBoard().getGrid();

        // fetch tile from db
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbol(grid.get(coordinate).getMultiplier(),
                stone.getLetter());

        // check if tile is present
        if (foundTile.isEmpty()) {
            throw new ConflictException("There was a problem while fetching the tile");
        } else {
            tile = foundTile.get();
        }

        // TODO: change after stone is implemented
        grid.set(coordinate, tile);
    }

    public int calculatePoints(List<Tile> tiles) {
        // define sum and multiplicand
        int sum = 0;
        int multiplicand = 1;

        // calculate sum and multiplicand
        for (Tile tile : tiles) {
            sum += tile.getValue();
            multiplicand *= tile.getMultiplier();
        }

        // deploy multiplications
        sum *= multiplicand;

        return sum;
    }
}
