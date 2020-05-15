package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.dictionary.WordLists;
import ch.uzh.ifi.seal.soprafs20.entity.*;
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
        List<Word> words;
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
        words = checkBoard(new ArrayList<>(grid), stones, coordinates);

        // check if there are any valid words
        if (words.isEmpty()) {
            throw new ConflictException("The stones placed form no valid word.");
        }

        // add score to the player
        for (Word word : words) {
            if (word == null) {
                continue;
            }

            player.setScore(player.getScore() + word.getValue());
        }

        // check if words exists
        try {
            for (Word word : words) {
                if (word == null) {
                    continue;
                }

                checkWord(word.getWord());
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

        // check if game has enough stones to exchange
        if (game.getBag().size() < stoneIds.size()) {
            throw new ConflictException("The game has only " + game.getBag().size() + " stones left. Thus, the player" +
                    " cannot exchange " + stoneIds.size() + " stones.");
        }

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

    private List<Word> checkBoard(List<Tile> board, List<Stone> stones, List<Integer> coordinates ) {
        Tile[][] board2d = new Tile[15][15];
        ArrayList<Word> words = new ArrayList<>();
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
                    words.add(checkIfNewWordAndCalculatePoints(coordinates, word));

                    // check board horizontal
                    word = findHorizontalWords(board2d, visitedHorizontal, i, j);
                    words.add(checkIfNewWordAndCalculatePoints(coordinates, word));
                }
            }
        }

        return words;
    }

    private Word checkIfNewWordAndCalculatePoints(List<Integer> coordinates, List<Triplet> word) {
        String newWord = this.buildString(word);

        // check if word is only one letter
        if (newWord.length() <= 1) {
            return null;
        }

        // check if word belongs to new words and add score and word to lists if yes
        for (Triplet triplet : word) {
            if (coordinates.contains(triplet.j * 15 + triplet.i)) {
                return new Word(newWord, calculatePoints(buildList(word)));
            }
        }

        // return null if the word contains no played stones
        return null;
    }

    private List<Triplet> findVerticalWords(Tile[][] board, boolean[][] visited, int i, int j) {
        Triplet currentLetter;
        List<Triplet> words;

        // check if index i is out of bound
        if (i < 0 || i > 14) {
            return new ArrayList<>();
        }

        // set currentLetter to current tile and coordinates
        currentLetter = new Triplet(board[i][j], i, j);

        // if already visited return empty list
        if (visited[i][j]) {
            return new ArrayList<>();
        }

        // Mark current cell as visited
        visited[i][j] = true;

        // return nothing if there are no more letters
        if (currentLetter.tile.getStoneSymbol() == null) {
            return new ArrayList<>();
        }

        // check iteratively for other letters
        words = findVerticalWords(board, visited, i - 1, j);
        words.add(currentLetter);
        words.addAll(findVerticalWords(board, visited, i + 1, j));
        return words;
    }

    private List<Triplet> findHorizontalWords(Tile[][] board, boolean[][] visited, int i, int j) {
        Triplet currentLetter;
        List<Triplet> words;

        // check if out of bounds
        if (j < 0 || j > 14) {
            return new ArrayList<>();
        }

        // set currentLetter to current tile and coordinates
        currentLetter = new Triplet(board[i][j], i, j);

        // if already visited return empty list
        if (visited[i][j]) {
            return new ArrayList<>();
        }

        // Mark current cell as visited
        visited[i][j] = true;

        // return nothing if there are no more letters
        if (currentLetter.tile.getStoneSymbol() == null) {
            return new ArrayList<>();
        }

        // check iteratively for other letters
        words = findHorizontalWords(board, visited, i, j - 1);
        words.add(currentLetter);
        words.addAll(findHorizontalWords(board, visited, i, j + 1));
        return words;
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