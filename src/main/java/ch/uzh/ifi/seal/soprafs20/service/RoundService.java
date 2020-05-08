package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.dictionary.WordLists;
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
import java.io.IOException;
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

        // deploy multiplications
        if (doublew != 0) {
            sum *= doublew * 2;
        }

        if (triplew != 0) {
            sum *= triplew * 3;
        }

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
        Tuple tuple;
        List<Tile> grid;
        List<Stone> stones;

        // check if list of stones is empty
        if (stoneId.isEmpty()) {
            throw new ConflictException("Player cannot lay empty word.");
        }

        // check if list of coordinates is empty
        if (coordinates.isEmpty()) {
            throw new ConflictException("The stones must be played on the board");
        }

        // fetch all stones from list
        stones = getStones(stoneId);

        // fetch game from db, grid from game
        grid = game.getGrid();

        // check if placing is available
        for (int i = 0; i < stones.size(); ++i) {
            placeStoneValid(grid, coordinates.get(i));
        }

        // scan board for all new words with length > 1
        tuple = checkBoard(new ArrayList<>(grid), stones, coordinates);

        // check if there are any valid words
        if (tuple.words.isEmpty()) {
            throw new ConflictException("The stones placed form no valid word.");
        }

        // add score to the player
        player.setScore(player.getScore() + tuple.score);

        // check if words exists
        try {
            for (String word : tuple.words) {
                checkWord(word.toLowerCase());
            }
        }

        // if not throw exception
        catch (Exception exception) {
            throw new ConflictException(exception.getMessage());
        }

        // place new stones
        for (int i = 0; i < stones.size(); ++i) {
            placeStone(grid, stones.get(i), coordinates.get(i));
            deleteStone(player, stones.get(i));
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
        Optional<Stone> found;
        List<Stone> stones = new ArrayList<>();

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

    private void checkWord(String word) throws IOException {
        // check if word is empty
        if (word.isEmpty()) {
            throw new ConflictException("Cannot look up an empty word.");
        }

        //check if word is in dictionary
        WordLists wordLists = WordLists.getInstance();
        if(wordLists.contains(word)){
            return;
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

        // throw exception if api returns an exception
        catch (Exception exception) {
            throw new ConflictException("There went something wrong while searching the dictionary: " +
                    exception.getMessage());
        }
    }

    private void placeStoneValid(List<Tile> grid, int coordinate) {
        // check if tile-to-be-covered is empty
        if (grid.get(coordinate).getStoneSymbol() != null) {
            throw new ConflictException("This field has already a stone on it, thus this stone could not be placed");
        }
    }

    private void placeStone(List<Tile> grid, Stone stone, int coordinate) {
        Tile tile;
        Optional<Tile> foundTile;

        // fetch tile from db
        foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(grid.get(coordinate).getMultiplier(),
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

    private String buildString(List<Triplet> word){
        StringBuilder newWord = new StringBuilder();

        // append the letter of all triplets to the word
        for (Triplet letter : word){
            if (letter.tile.getStoneSymbol() != null) {
                newWord.append(letter.tile.getStoneSymbol());
            }
        }

        return newWord.toString();
    }

    private List<Tile> buildList(List<Triplet> triplets) {
        List<Tile> tiles = new ArrayList<>();

        // build a tile list
        for (Triplet triplet : triplets) {
            tiles.add(triplet.tile);
        }

        // return
        return tiles;
    }

    private Tuple checkBoard(List<Tile> board, List<Stone> stones, List<Integer> coordinates ) {
        int score = 0;
        Tile[][] board2d = new Tile[15][15];
        ArrayList<String> words = new ArrayList<>();
        boolean[][] visitedVertical = new boolean[15][15];
        boolean[][] visitedHorizontal = new boolean[15][15];

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
                    // check board vertical
                    word = findVerticalWords(board2d, visitedVertical, i, j);
                    score += checkIfNewWordAndCalculatePoints(coordinates, words, word);

                    // check board horizontal
                    word = findHorizontalWords(board2d, visitedHorizontal, i, j);
                    score += checkIfNewWordAndCalculatePoints(coordinates, words, word);
                }
            }
        }

        return new Tuple(words, score);
    }

    private int checkIfNewWordAndCalculatePoints(List<Integer> coordinates, ArrayList<String> words, List<Triplet> word) {
        String newWord = this.buildString(word);

        // check if word is only one letter
        if (newWord.length() <= 1) {
            return 0;
        }

        // check if word belongs to new words and add score and word to lists if yes
        for (Triplet triplet : word) {
            if (coordinates.contains(triplet.j * 15 + triplet.i)) {
                if (!words.contains(newWord)) {
                    words.add(newWord);
                    return calculatePoints(buildList(word));
                }
            }
        }

        // return score=0 if the word contains no played stones
        return 0;
    }

    private List<Triplet> findVerticalWords(Tile[][] board, boolean[][] visited, int i, int j) {
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

    private List<Triplet> findHorizontalWords(Tile[][] board, boolean[][] visited, int i, int j) {
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

    private static class Tuple {
        public final List<String> words;
        public final int score;

        private Tuple(List<String> words, int score) {
            this.words = words;
            this.score = score;
        }
    }
}