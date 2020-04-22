package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.StoneRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.WordnikGetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.lang.Math.abs;

@Service
@Transactional
public class RoundService {
    private final GameRepository gameRepository;
    private final TileRepository tileRepository;
    private final PlayerRepository playerRepository;
    private final StoneRepository stoneRepository;

    @Autowired
    public RoundService(@Qualifier("gameRepository")GameRepository gameRepository,
                        @Qualifier("tileRepository")TileRepository tileRepository,
                        @Qualifier("playerRepository")PlayerRepository playerRepository,
                        @Qualifier("stoneRepository")StoneRepository stoneRepository) {
        this.gameRepository = gameRepository;
        this.tileRepository = tileRepository;
        this.playerRepository = playerRepository;
        this.stoneRepository = stoneRepository;
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

    public Player getCurrentPlayer(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        // fetch current player and reposition it to the last place
        Player player = game.getPlayers().get(0);
        game.removePlayer(player);
        game.addPlayer(player);

        return player;
    }

    public String placeWord(long gameId, List<Stone> stones, List<Integer> coordinates) {
        String word;

        // fetch game from db, board from game, grid from board
        Game game = getGame(gameId);
        List<Tile> grid = game.getGrid();

        // check if placing is valid
        for(int i = 0; i < stones.size(); ++i) {
            placeStoneValid(grid, coordinates.get(i));
        }

        // check if word is vertical or horizontal
        if (coordinates.get(stones.size() - 1) == (coordinates.get(0) + stones.size() * 15)) {
            word = buildWordVertical(grid, stones, coordinates);
        } else {
            word = buildWordHorizontal(grid, stones, coordinates);
        }

        // check if word exists
        String definition = checkWord(word);

        // place new stones
        for(int i = 0; i < stones.size(); ++i) {
            placeStone(grid, stones.get(i), coordinates.get(i));
        }

        // save changes
        gameRepository.save(game);
        gameRepository.flush();

        return definition;
    }

    public List<Stone> exchangeStone(long gameId, long playerId, List<Long> stoneIds) {
        List<Stone> stones;
        List<Stone> answer = new ArrayList<>();

        // fetch all stones from the db
        stones = getStones(stoneIds);

        // fetch game/player from db
        Game game = getGame(gameId);
        Player player = getPlayer(playerId);

        for (int i = 0; i < stones.size(); i++) {
            answer.add(drawStone(game, player));
        }

        for (Stone stone : stones) {
            returnStone(game, player, stone);
        }

        return answer;
    }

    public Stone drawStone(Game game, Player player) {
        // get stones from game
        List<Stone> stonesToChange;
        stonesToChange = game.getBag();

        // draw a random stone
        int random = new Random().nextInt() % stonesToChange.size();
        Stone stone = stonesToChange.get(abs(random));

        // remove stone from game
        game.removeStone(stone);

        // add stone to player
        stonesToChange = player.getBag();
        stonesToChange.add(stone);
        player.setBag(stonesToChange);

        // save change
        gameRepository.save(game);
        gameRepository.flush();

        playerRepository.save(player);
        playerRepository.flush();

        // return
        return stone;
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

    private Player getPlayer(long playerId) {
        // fetch player from db
        Player player;
        Optional<Player> foundPlayer = playerRepository.findById(playerId);

        // check if player is present
        if (foundPlayer.isEmpty()) {
            throw new NotFoundException("The player with the id " + playerId + " could not be found.");
        } else {
            player = foundPlayer.get();
        }

        return player;
    }

    private List<Stone> getStones(List<Long> stoneIds) {
        List<Stone> stones = new ArrayList<>();
        Optional<Stone> found;

        for (Long id : stoneIds) {
            found = stoneRepository.findByIdIs(id);

            if (found.isEmpty()) {
                throw new NotFoundException("The stone with the id " + id + " could not be found");
            } else {
                stones.add(found.get());
            }
        }

        return stones;
    }

    private String checkWord(String word) {
        WordnikGetDTO wordnikGetDTO;

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
            wordnikGetDTO = (WordnikGetDTO) connection.getContent();
        } catch (Exception exception) {
            throw new ConflictException(exception.getMessage());
        }

        return wordnikGetDTO.getText();
    }

    private String buildWordVertical(List<Tile> grid, List<Stone> played, List<Integer> coordinates) {
        StringBuilder word = new StringBuilder();
        int start = coordinates.get(0) % 15;

        for (int i = start; i < 225; i += 15) {
            if (coordinates.contains(i)) {
                word.append(played.get(coordinates.indexOf(i)));
            } else if (!grid.get(i).getStoneSymbol().isEmpty()) {
                word.append(grid.get(i).getStoneSymbol());
            } else if (i > coordinates.get(coordinates.size() - 1) && grid.get(i).getStoneSymbol().isEmpty()) {
                break;
            } else if (i > coordinates.get(0) && i < coordinates.get(coordinates.size() - 1) &&
                    grid.get(i).getStoneSymbol().isEmpty()) {
                throw new ConflictException("The stones played form more than one word");
            }
        }

        return word.toString();
    }

    private String buildWordHorizontal(List<Tile> grid, List<Stone> played, List<Integer> coordinates) {
        StringBuilder word = new StringBuilder();
        int start = coordinates.get(0) - (coordinates.get(0) % 15);
        int end = start + 15;

        for (int i = start; i < end; ++i) {
            if (coordinates.contains(i)) {
                word.append(played.get(coordinates.indexOf(i)));
            } else if (!grid.get(i).getStoneSymbol().isEmpty()) {
                word.append(grid.get(i).getStoneSymbol());
            } else if (i > coordinates.get(coordinates.size() - 1) && grid.get(i).getStoneSymbol().isEmpty()) {
                break;
            } else if (i > coordinates.get(0) && i < coordinates.get(coordinates.size() - 1) &&
                    grid.get(i).getStoneSymbol().isEmpty()) {
                throw new ConflictException("The stones played form more than one word");
            }
        }

        return word.toString();
    }

    private void placeStoneValid(List<Tile> grid, int coordinate) {
        // check if tile-to-be-covered is empty
        if (grid.get(coordinate).getStoneSymbol() != null) {
            throw new ConflictException("This field has already a stone on it, thus this stone could not be placed");
        }
    }

    private void placeStone(List<Tile> grid, Stone stone, int coordinate) {
        Tile tile;

        // fetch tile from db
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbol(grid.get(coordinate).getMultiplier(),
                stone.getSymbol());

        // check if tile is present
        if (foundTile.isEmpty()) {
            throw new ConflictException("There was a problem while fetching the tile");
        } else {
            tile = foundTile.get();
        }

        // place stone on tile
        grid.set(coordinate, tile);
    }

    private void returnStone(Game game, Player player, Stone stone) {
        // get stones from game and player
        List<Stone> gameStones = game.getBag();
        List<Stone> playerStones = player.getBag();

        // check if player has stone in bag
        if (!playerStones.contains(stone)) {
            throw new ConflictException("The player has no such stone.");
        }

        // remove stone from player
        playerStones.remove(stone);
        player.setBag(playerStones);

        // add stone to the game
        gameStones.add(stone);
        game.setBag(gameStones);

        // save changes
        gameRepository.save(game);
        gameRepository.flush();

        playerRepository.save(player);
        playerRepository.flush();
    }
}
