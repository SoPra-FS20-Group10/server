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
    public RoundService(@Qualifier("gameRepository") GameRepository gameRepository,
                        @Qualifier("tileRepository") TileRepository tileRepository,
                        @Qualifier("playerRepository") PlayerRepository playerRepository,
                        @Qualifier("stoneRepository") StoneRepository stoneRepository) {
        this.gameRepository = gameRepository;
        this.tileRepository = tileRepository;
        this.playerRepository = playerRepository;
        this.stoneRepository = stoneRepository;
    }

    public int calculatePoints(List<Tile> tiles) {
        // define sum and multiplicand
        int sum = 0;
        int triplew = 0;
        int doublew = 0;


        //count word multiplicands
        for (Tile tile : tiles) {
            if ((tile.getMultiplier() == 2) && tile.getMultivariant().equals("w")) {
                doublew += 1;
            }
            else if ((tile.getMultiplier() == 3) && tile.getMultivariant().equals("w")) {
                triplew += 1;
            }
        }

        // calculate wordScore with letterBonus
        for (Tile tile : tiles) {
            if (tile.getMultivariant().equals("l")) {
                sum += (tile.getValue() * tile.getMultiplier());
            }
            else {
                sum += tile.getValue();
            }
        }

        if (doublew != 0) {
            sum *= doublew * 2;
        }

        if (triplew != 0) {
            sum *= triplew * 3;
        }

        // deploy multiplications
        return sum;
    }

    public Player getCurrentPlayer(Game game, Player player) {
        // fetch players
        List<Player> players = game.getPlayers();

        // assign currentPlayer to next player in the list
        player = players.get((players.indexOf(player) + 1) % players.size());

        // save the changes
        playerRepository.save(player);
        playerRepository.flush();

        gameRepository.save(game);
        gameRepository.flush();

        return player;
    }

    public void placeWord(Game game, Player player, List<Long> stoneId, List<Integer> coordinates) {
        if (!stoneId.isEmpty()) {
            String word;
            List<Stone> stones = getStones(stoneId);

            // fetch game from db, grid from game
            List<Tile> grid = game.getGrid();

            // check if placing is valid
            for (int i = 0; i < stones.size(); ++i) {
                placeStoneValid(grid, coordinates.get(i));
            }

            // check if word is vertical or horizontal
            if ((coordinates.get(stones.size() - 1) % 15) == (coordinates.get(0) % 15)) {
                word = buildWord(grid, stones, coordinates, "vertical");
            }
            else {
                word = buildWord(grid, stones, coordinates, "horizontal");
            }

            // check if word exists
            try {
                checkWord(word.toLowerCase());
            }
            catch (Exception exception) {
                throw new ConflictException(exception.getMessage());
            }

            // place new stones
            for (int i = 0; i < stones.size(); ++i) {
                placeStone(grid, stones.get(i), coordinates.get(i));
                deleteStone(player, stones.get(i));
            }

        }

        // save changes
        playerRepository.save(player);
        playerRepository.flush();

        gameRepository.save(game);
        gameRepository.flush();
    }

    public List<Stone> exchangeStone(Game game, Player player, List<Long> stoneIds) {
        List<Stone> stones;
        List<Stone> answer = new ArrayList<>();

        // fetch all stones to exchange from the db
        stones = getStones(stoneIds);

        // draw new stones for the player
        for (Stone value : stones) {
            Stone stone = drawStone(game);
            player.addStone(player.getBag().indexOf(value) + 1, stone);
            answer.add(stone);
        }

        // return the exchanged stones from the player's bag to the game's bag
        for (Stone stone : stones) {
            returnStone(game, player, stone);
        }

        // save the changes
        playerRepository.save(player);
        playerRepository.flush();

        gameRepository.save(game);
        gameRepository.flush();

        return answer;
    }

    public Stone drawStone(Game game) {
        // get stones from game
        List<Stone> stonesToChange = game.getBag();

        if (stonesToChange.isEmpty()) {
            return null;
        }

        // draw a random number
        int random = new Random().nextInt() % stonesToChange.size();

        // return stone
        return stonesToChange.remove(abs(random));
    }

    private List<Stone> getStones(List<Long> stoneIds) {
        List<Stone> stones = new ArrayList<>();
        Optional<Stone> found;

        // fetch all stones from the db
        for (Long id : stoneIds) {
            found = stoneRepository.findByIdIs(id);

            if (found.isEmpty()) {
                throw new NotFoundException("The stone with the id " + id + " could not be found");
            }
            else {
                stones.add(found.get());
            }
        }

        // return
        return stones;
    }

    private void checkWord(String word) {
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
            connection.getContent();
        }
        catch (Exception exception) {
            throw new ConflictException("There went something wrong while searching the dictionary: " +
                    exception.getMessage());
        }
    }

    private String buildWord(List<Tile> grid, List<Stone> played, List<Integer> coordinates, String mode) {
        StringBuilder word = new StringBuilder(new StringBuilder());
        int start;
        int end;
        int toAdd;

        // set start/end-point and toAdd according whether word is vertical or horizontal
        if (mode.equals("vertical")) {
            start = coordinates.get(0) % 15;
            end = 225;
            toAdd = 15;
        }
        else {
            start = coordinates.get(0) - (coordinates.get(0) % 15);
            end = start + 15;
            toAdd = 1;
        }

        // check left/on-top of the played stones
        for (int i = coordinates.get(0) - toAdd; i >= start; i -= toAdd) {
            // add letter to the front of the word if it exists
            if (grid.get(i).getStoneSymbol() != null) {
                word.insert(0, grid.get(i).getStoneSymbol());
            }

            // if tile is empty: break
            else {
                break;
            }
        }

        // check between first and last stones played
        for (int i = coordinates.get(0); i <= coordinates.get(coordinates.size() - 1); i += toAdd) {
            // stone gets played by player
            if (coordinates.contains(i)) {
                word.append(played.get(coordinates.indexOf(i)).getSymbol());
            }

            // stone is already played
            else if (grid.get(i).getStoneSymbol() != null) {
                word.append(grid.get(i).getStoneSymbol());
            }

            // if there's a gap, throw an error
            else if (grid.get(i).getStoneSymbol() == null) {
                throw new ConflictException("The stones played form more than one word");
            }
        }

        // check under/right of the stones played
        for (int i = coordinates.get(coordinates.size() - 1) + 1; i < end; i += toAdd) {
            // add letter to the back of the word if it exists
            if (grid.get(i).getStoneSymbol() != null) {
                word.append(grid.get(i).getStoneSymbol());
            }

            // if tile is empty: break
            else {
                break;
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
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(grid.get(coordinate).getMultiplier(),
                stone.getSymbol(), grid.get(coordinate).getMultivariant());

        // check if tile is present
        if (foundTile.isEmpty()) {
            throw new ConflictException("There was a problem while fetching the tile");
        }
        else {
            tile = foundTile.get();
        }

        // set value
        tile.setValue(stone.getValue());

        // place stone on tile
        grid.set(coordinate, tile);
    }

    private void deleteStone(Player player, Stone stone) {
        // check if player has stone in bag
        if (!player.getBag().contains(stone)) {
            throw new ConflictException("The player has no stone with the letter " + stone.getSymbol());
        }

        // remove stone from player
        player.removeStone(stone);

        // delete stone
        stoneRepository.delete(stone);
        stoneRepository.flush();
    }

    private void returnStone(Game game, Player player, Stone stone) {
        // check if player has stone in bag
        if (!player.getBag().contains(stone)) {
            throw new ConflictException("The player has no such stone.");
        }

        // remove stone from player
        player.removeStone(stone);

        // add stone to the game
        game.addStone(stone);

        // save changes
        playerRepository.save(player);
        playerRepository.flush();

        gameRepository.save(game);
        gameRepository.flush();
    }

    public List<String> checkBoard(List<Tile> board) {
        Tile[][] board2d = new Tile[15][15];
        Boolean[][] visited = new Boolean[15][15];
        ArrayList<String> words = new ArrayList<>();

        // Convert to 2dArray
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board2d[i][j] = board.get((i * 15) + j);
            }
        }

        // List with all words
        String word;

        // Consider every character and look for all words
        // starting with this character
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++)
                if (board2d[i][j].getStoneSymbol() != null) {
                    word = findVerticalWords(board2d, visited, i, j);

                    if (!word.isEmpty() && !words.contains(word)) {
                        words.add(word);
                    }

                    word = findHorizontalWords(board2d, visited, i, j);

                    if (!word.isEmpty() && !words.contains(word)) {
                        words.add(word);
                    }
                }

        return words;
    }

    // vertical words
    private String findVerticalWords(Tile[][] board, Boolean[][] visited, int i, int j) {

        // Mark current cell as visited
        visited[i][j] = true;
        String currentLetter = board[i][j].getStoneSymbol();
        // return nothing if there no more letters
        if (currentLetter == null || i >= 15 || j >= 15) {
            return "";
        }

        if (!visited[i - 1][j] && !visited[i + 1][j]) {
            return findVerticalWords(board, visited, i - 1, j) + currentLetter + findVerticalWords(board, visited, i + j, j);
        }
        else if (!visited[i - 1][j] && visited[i + 1][j]) {
            return findVerticalWords(board, visited, i - 1, j) + currentLetter;
        }
        else if (visited[i - 1][j] && !visited[i + 1][j]) {
            return currentLetter + findVerticalWords(board, visited, i + j, j);
        }

        return "";
    }

    // Horizontal words
    private String findHorizontalWords(Tile[][] board, Boolean[][] visited, int i, int j) {

        // Mark current cell as visited
        visited[i][j] = true;
        String currentLetter = board[i][j].getStoneSymbol();
        // return nothing if there no more letters
        if (currentLetter == null || i >= 15 || j >= 15) {
            return "";
        }

        if (!visited[i][j - 1] && !visited[i][j + 1]) {
            return findHorizontalWords(board, visited, i, j - 1) + currentLetter + findHorizontalWords(board, visited, i, j + 1);
        }
        else if (!visited[i][j - 1] && visited[i][j + 1]) {
            return findHorizontalWords(board, visited, i, j - 1) + currentLetter;
        }
        else if (visited[i][j - 1] && !visited[i][j + 1]) {
            return currentLetter + findHorizontalWords(board, visited, i, j + 1);
        }

        return "";
    }
}