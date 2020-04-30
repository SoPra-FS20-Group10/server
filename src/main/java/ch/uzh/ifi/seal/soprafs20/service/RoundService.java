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

            List<Stone> stones = getStones(stoneId);

            // fetch game from db, grid from game
            List<Tile> grid = game.getGrid();

            // check if placing is valid
            for (int i = 0; i < stones.size(); ++i) {
                placeStoneValid(grid, coordinates.get(i));
            }

            List<String> words = checkBoard(new ArrayList<>(grid), stones, coordinates);

            /*
            // check if word is vertical or horizontal
            if ((coordinates.get(stones.size() - 1) % 15) == (coordinates.get(0) % 15)) {
                word = buildWord(grid, stones, coordinates, "vertical");
            }
            else {
                word = buildWord(grid, stones, coordinates, "horizontal");
            }
            */

            // check if words exists
            try {
                for(String word:words) {
                    checkWord(word.toLowerCase());
                }
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

        // check if game has stones
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

            // check if stone exists
            if (found.isEmpty()) {
                throw new NotFoundException("The stone with the id " + id + " could not be found");
            } else {
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
        StringBuilder word = new StringBuilder();
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
        } else {
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

    public String buildString(List<Triplet> word){
        StringBuilder newWord = new StringBuilder();

        // append the letter of all triplets to the word
        for (Triplet letter : word){
            if (letter.tile.getStoneSymbol() != null) {
                newWord.append(letter.tile.getStoneSymbol());
            }
        }

        return newWord.toString();
    }

    public List<String> checkBoard(List<Tile> board, List<Stone> stones, List<Integer> coordinates ) {
        Tile[][] board2d = new Tile[15][15];
        Boolean[][] visitedVertical = new Boolean[15][15];
        Boolean[][] visitedHorizontal = new Boolean[15][15];
        ArrayList<String> words = new ArrayList<>();

        // place stones on temporary board copy
        for (int i = 0; i < stones.size(); ++i) {
            placeStone(board, stones.get(i), coordinates.get(i));
        }

        // convert to 2dArray
        for (int j = 0; j < 15; ++j) {
            for (int i = 0; i < 15; ++i) {
                board2d[i][j] = board.get((j * 15) + i);
                visitedVertical[i][j] = false;
                visitedHorizontal[i][j] = false;
            }
        }

        // list with all words
        List<Triplet> word;

        // consider every character and look for all words
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board2d[i][j].getStoneSymbol() != null) {
                    word = findVerticalWords(board2d, visitedVertical, i, j);

                    if (word.size() > 1) {
                        for (Triplet tile : word) {
                            if (coordinates.contains(tile.j * 15 + tile.i)) {
                                String newWord = this.buildString(word);

                                if (!words.contains(newWord)) {
                                    words.add(newWord);
                                }
                            }
                        }
                    }

                    word = findHorizontalWords(board2d, visitedHorizontal, i, j);

                    if (word.size() > 1) {
                        for (Triplet tile : word) {
                            if (coordinates.contains(tile.j * 15 + tile.i)) {
                                String newWord = this.buildString(word);

                                if (!words.contains(newWord)) {
                                    words.add(newWord);
                                }
                            }
                        }
                    }
                }
            }
        }

        return words;
    }

    private List<Triplet> findVerticalWords(Tile[][] board, Boolean[][] visited, int i, int j) {
        Triplet currentLetter = new Triplet(board[i][j], i, j);
        List<Triplet> words = new ArrayList<>();

        // if already visited return empty list
        if (visited[i][j]) {
            return new ArrayList<>();
        }

        // Mark current cell as visited
        visited[i][j] = true;

        // return nothing if there are no more letters
        if (currentLetter.tile.getStoneSymbol() == null || i >= 15 || j >= 15) {
            return new ArrayList<>();
        }

        // check edge case i = 0 and right tile has not been visited
        if (i == 0 && !visited[i + 1][j]) {
            words.add(currentLetter);
            words.addAll(findVerticalWords(board, visited, i + 1, j));
            return words;
        }

        // check edge case i = 0 and right tile has been visited
        if (i == 0 && visited[i + 1][j]) {
            words.add(currentLetter);
            return words;
        }

        // check edge case i = 14 and left tile has not been visited
        else if (i == 14 && !visited[i - 1][j]) {
            words.addAll(findVerticalWords(board, visited, i - 1, j));
            words.add(currentLetter);
            return words;
        }

        // check edge case i = 14 and left tile has been visited
        else if (i == 14 && visited[i - 1][j]) {
            words.add(currentLetter);
            return words;
        }

        // if tiles left and right have not been visited
        else if (!visited[i - 1][j] && !visited[i + 1][j]) {
            words.addAll(findVerticalWords(board, visited, i - 1, j));
            words.add(currentLetter);
            words.addAll(findVerticalWords(board, visited, i + 1, j));
            return words;
        }

        // if tile left has not been visited
        else if (!visited[i - 1][j] && visited[i + 1][j]) {
            words.addAll(findVerticalWords(board, visited, i - 1, j));
            words.add(currentLetter);
            return words;
        }

        // if tile right has not been visited
        else if (visited[i - 1][j] && !visited[i + 1][j]) {
            words.add(currentLetter);
            words.addAll(findVerticalWords(board, visited, i + 1, j));
            return words;
        }

        // if tiles left and right have both been visited
        else {
            words.add(currentLetter);
            return words;
        }
    }

    private List<Triplet> findHorizontalWords(Tile[][] board, Boolean[][] visited, int i, int j) {
        Triplet currentLetter = new Triplet(board[i][j], i, j);
        List<Triplet> words = new ArrayList<>();

        // if already visited return empty list
        if (visited[i][j]) {
            return new ArrayList<>();
        }

        // Mark current cell as visited
        visited[i][j] = true;

        // return nothing if there are no more letters
        if (currentLetter.tile == null || i >= 15 || j >= 15) {
            return new ArrayList<>();
        }

        // check edge case j = 0 and down tile has not been visited
        if (j == 0 && !visited[i][j + 1]) {
            words.add(currentLetter);
            words.addAll(findHorizontalWords(board, visited, i, j + 1));
            return words;
        }

        // check edge case j = 0 and down tile has been visited
        if (j == 0 && visited[i][j + 1]) {
            words.add(currentLetter);
            return words;
        }

        // check edge case j = 14 and up tile has not been visited
        else if (j == 14 && !visited[i][j - 1]) {
            words.addAll(findHorizontalWords(board, visited, i, j - 1));
            words.add(currentLetter);
            return words;
        }

        // check edge case j = 14 and up tile has been visited
        else if (j == 14 && visited[i][j - 1]) {
            words.add(currentLetter);
            return words;
        }

        // if tiles up and down have not been visited
        if (!visited[i][j - 1] && !visited[i][j + 1] ) {
            words.addAll(findHorizontalWords(board, visited, i, j - 1));
            words.add(currentLetter);
            words.addAll(findHorizontalWords(board, visited, i, j + 1));
            return words;
        }

        // if tile up has not been visited
        else if (!visited[i][j - 1] && visited[i][j + 1]) {
            words.addAll(findHorizontalWords(board, visited, i, j - 1));
            words.add(currentLetter);
            return words;
        }

        // if tile down has not been visited
        else if (visited[i][j - 1] && !visited[i][j + 1]) {
            words.add(currentLetter);
            words.addAll(findHorizontalWords(board, visited, i , j + 1));
            return words;

        }

        // if tiles up and down have both been visited
        else {
            words.add(currentLetter);
            return new ArrayList<>();
        }
    }

    private static class Triplet{
        public final Tile tile;
        public final int i;
        public final int j;

        private Triplet(Tile tile, int i, int j) {
            this.tile = tile;
            this.i = i;
            this.j = j;
        }
    }
}