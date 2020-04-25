package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UnauthorizedException;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final TileRepository tileRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository")GameRepository gameRepository, @Qualifier("tileRepository")TileRepository tileRepository) {
        this.gameRepository = gameRepository;
        this.tileRepository = tileRepository;
    }

    public Game getGame(long gameId) {
        // fetch game from db
        Game game;
        Optional<Game> foundGame = gameRepository.findByIdIs(gameId);

        // check if game exists
        if (foundGame.isEmpty()) {
            throw new NotFoundException("The game with the id " + gameId + " is not existing.");
        } else {
            game = foundGame.get();
        }

        return game;
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public Game createGame(Game game, Player owner) {
        // check if owner has no other game
        if (owner.getUser().getGame() != null) {
            throw new ConflictException("The user with the id " + owner.getUser().getId() + " is hosting another game.");
        }

        //game.setChat(new Chat());
        game.setOwner(owner.getUser());
        game.setStatus(GameStatus.WAITING);

        // initialise list and add player
        game.initGame();
        initTiles();
        game.addPlayer(owner);
        createGrid(game);

        // save changes
        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
    }

    public Game joinGame(long gameId, Player player, String password) {
        // fetch game from db
        Game game = getGame(gameId);

        // check if password is correct
        if (!game.getPassword().equals(password)) {
            throw new ConflictException("Wrong password. Therefore the player could not join the game");
        }

        // add player to the game
        game.addPlayer(player);

        // save the game
        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
    }

    public void startGame(Game game, String token) {
        // check if user is authorized to start the game
        if (!game.getOwner().getToken().equals(token)) {
            throw new UnauthorizedException("The user is not authorized to start the game");
        }
        // check if all players are ready
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            if (player.getStatus() == PlayerStatus.NOT_READY) {
                throw new ConflictException("Not all players are ready to start.");
            }
        }

        // check if the game can be started
        if (game.getStatus() == GameStatus.RUNNING) {
            throw new ConflictException("The game is already running.");
        } else if (game.getStatus() == GameStatus.ENDED) {
            throw new ConflictException("The game has already ended.");
        }

        // add stones to the game
        addAllStones(game);

        // set flag to running
        game.setStatus(GameStatus.RUNNING);

        // save change
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void leaveGame(Game game, Player player, String token) {
        // check if player is the lobbyLeader
        if (player.getUser().getId().equals(game.getOwner().getId())) {
            throw new UnauthorizedException("The game owner cannot leave the game. Choose to end the game.");
        }

        // check if the user is authorized to leave the game
        if (!player.getUser().getToken().equals(token)) {
            throw new ConflictException("The user is not authorized to leave this game");
        }

        // remove the player
        game.removePlayer(player);

        // save changes
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void endGame(long gameId, String token) {
        // fetch game from db
        Game game = getGame(gameId);

        //check if user is authorized to end game
        if (!game.getOwner().getToken().equals(token)) {
            throw new UnauthorizedException("The game can not be ended by this user");
        }

        // delete the game
        gameRepository.delete(game);
        gameRepository.flush();
    }

    public void addScores(Game game){
        //adds the scores at the end of a game to the user
        List<Player> players = game.getPlayers();
        for(Player player: players){
            User user = player.getUser();
            user.setOverallScore(user.getOverallScore() + player.getScore());
            player.setScore(0);
        }
    }

    public List<Player> getPlayers(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        return game.getPlayers();
    }

    public void createGrid(Game game){
        List<Tile> grid = new ArrayList<>();

        Integer[] doubles = {3, 11,16,20,24,28,32,36,38,42,45,48,52,56,59,64,70,76,80,84,88,92,96,98,102,108};
        Integer[] triples= {0,7,14,105};

        //create half of board, then flip and append testTest
        for(int i =0; i < 112; i++){

            //check if double field
            if(Arrays.asList(doubles).contains(i)){
                Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbol(2,null);

                // check if tile exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException("Grid cannot be initialized since the tiles couldn't be found");
                } else {
                    grid.add(foundTile.get());
                }
            }

            //check if triple tile
            else if(Arrays.asList(triples).contains(i)){
                Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbol(3,null);

                // check if tile exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException("Grid cannot be initialized since the tiles couldn't be found");
                } else {
                    grid.add(foundTile.get());
                }
            }

            //else its single tile
            else{
                Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbol(1,null);

                // check if tile exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException("Grid cannot be initialized since the tiles couldn't be found");
                } else {
                    grid.add(foundTile.get());
                }
            }
        }

        //make a clone and reverse it
        List<Tile> clone = new ArrayList<>(grid);
        Collections.reverse(clone);

        //add the middle star
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbol(2,null);

        // check if tile exists
        if (foundTile.isEmpty()) {
            throw new NotFoundException("Grid cannot be initialized since the tiles couldn't be found");
        } else {
            grid.add(foundTile.get());
        }

        // fill second half of the grid
        grid.addAll(clone);

        // add grid to game
        game.setGrid(grid);
    }


    public void initTiles(){
        String alpha = "abcdefghijklmnopqrstuvwxyz";

        //create and save empty tiles
        for (int i = 1; i < 4; ++i) {
            tileRepository.save(new Tile(i, null));
            tileRepository.flush();
        }

        //add all other stone-tile combinations ot the repo
        for(int i = 0; i < alpha.length();i++){
            String symbol = Character.toString(alpha.charAt(i));
            for(int j = 1; j < 4; j++){
                tileRepository.save(new Tile(j, symbol));
                tileRepository.flush();
            }
        }
    }

    private void addAllStones(Game game) {

        long id = 0;
        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("A", 1, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("B", 3, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("C", 3, id));
            id++;
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("D", 2, id));
            id++;
        }

        for (int i = 0; i < 13; i++) {
            game.addStone(new Stone("E", 1, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("F", 4, id));
            id++;
        }

        for (int i = 0; i < 4; i++) {
            game.addStone(new Stone("G", 2, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("H", 4, id));
            id++;
        }

        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("I", 1, id));
            id++;
        }

        game.addStone(new Stone("J", 8, id));
        id++;

        game.addStone(new Stone("K", 5, id));
        id++;

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("L", 1, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("M", 3, id));
            id++;
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("N", 1, id));
            id++;
        }

        for (int i = 0; i < 9; i++) {
            game.addStone(new Stone("O", 1, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("P", 3, id));
            id++;
        }

        game.addStone(new Stone("Q", 10, id));
        id++;

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("R", 1, id));
            id++;
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("S", 1, id));
            id++;
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("T", 1, id));
            id++;
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("U", 1, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("V", 4, id));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("W", 4, id));
            id++;
        }

        game.addStone(new Stone("X", 8, id));
        id++;

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("Y", 4, id));
            id++;
        }

        game.addStone(new Stone("Z", 10, id));
    }

}
