package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.*;
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
    public GameService(@Qualifier("gameRepository")GameRepository gameRepository,
                       @Qualifier("tileRepository")TileRepository tileRepository) {
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

    public void saveGame(Game game) {
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void deleteGame(Game game) {
        gameRepository.delete(game);
        gameRepository.flush();
    }

    public void addChat(Chat chat) {
        // add player to the user
        Game game = chat.getGame();
        game.setChat(chat);

        // save change
        gameRepository.save(game);
        gameRepository.flush();
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public Game createGame(Game game, Player owner) {
        // check if owner is already hosting another game
        if (owner.getUser().getGame() != null) {
            throw new ConflictException("The user with the id " + owner.getUser().getId() + " is hosting another game.");
        }

        game.setOwner(owner.getUser());
        game.setStatus(GameStatus.WAITING);

        // initialise list
        game.initGame();
        createGrid(game);

        // add stones to the game
        addAllStones(game);

        // add player
        game.addPlayer(owner);
        game.setCurrentPlayerId(owner.getId());

        // save changes
        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
    }

    public Game joinGame(long gameId, Player player, String password) {
        // fetch game from db
        Game game = getGame(gameId);

        // check if password is correct
        if(!(game.getPassword() == null || password == null)){
            if(!(game.getPassword().equals(password))){
                throw new ConflictException("Wrong password. Therefore the player could not join the game");
            }
        } else {
            throw new ConflictException("There was a problem with the password: password mustn't be null");
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

        // fetch players from the game
        List<Player> players = game.getPlayers();

        // check if all players are ready
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

        // set flag to running
        game.setStatus(GameStatus.RUNNING);

        // save change
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void leaveGame(Game game, Player player, String token) {
        // check if player is the lobbyLeader
        if (game.getStatus() != GameStatus.ENDED && player.getUser().getId().equals(game.getOwner().getId())) {
            throw new UnauthorizedException("The game owner cannot leave the game. Choose to end the game.");
        }

        // check if the user is authorized to leave the game
        if (!player.getUser().getToken().equals(token)) {
            if (!game.getOwner().getToken().equals(token))
            throw new ConflictException("The user is not authorized to leave this game");
        }

        // remove the player
        game.removePlayer(player);

        // save changes
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void endGame(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        // delete grid so that global tiles wont be deleted
        game.setGrid(null);

        // delete the game
        gameRepository.delete(game);
        gameRepository.flush();
    }

    public List<Player> getPlayers(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        return game.getPlayers();
    }

    public void createGrid(Game game){
        List<Tile> grid = new ArrayList<>();
        String error = "Grid cannot be initialized since the tiles couldn't be found";

        Integer[] doubles = {3, 11,16,28,32,36,38,42,45,48,52,56,59,64,70,84,92,96,98,102,108};
        Integer[] triples= {0,7,14,20,24,76,80,84,88,105};
        Integer[] doubleWord = {16,28,32,42,48,56,64,70};
        Integer[] tripleWord = {0,7,14,105};

        //create half of board, then flip and append testTest
        for(int i =0; i < 112; i++){

            //check if double field
            if(Arrays.asList(doubles).contains(i)){

                //check if word or letter
                Optional<Tile> foundTile;
                if(Arrays.asList(doubleWord).contains(i)){
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2, null, "w");
                } else {
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2, null, "l");
                }

                // check if tile exists and add to grid if yes
                if (foundTile.isEmpty()) {
                    throw new NotFoundException(error);
                } else {
                    grid.add(foundTile.get());
                }
            }

            //check if triple tile
            else if(Arrays.asList(triples).contains(i)){
                Optional<Tile> foundTile;

                //check if word or letter
                if(Arrays.asList(tripleWord).contains(i)){
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(3, null, "w");
                } else {
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(3, null, "l");
                }

                // check if tile exists and add to grid if yes
                if (foundTile.isEmpty()) {
                    throw new NotFoundException(error);
                } else {
                    grid.add(foundTile.get());
                }
            }

            //else its single tile
            else {
                Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(1,null,"l");

                // check if tile exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException(error);
                } else {
                    grid.add(foundTile.get());
                }
            }
        }

        //make a clone and reverse it
        List<Tile> clone = new ArrayList<>(grid);
        Collections.reverse(clone);

        //add the middle star
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2,null,"w");

        // check if tile exists
        if (foundTile.isEmpty()) {
            throw new NotFoundException(error);
        } else {
            grid.add(foundTile.get());
        }

        // fill second half of the grid
        grid.addAll(clone);

        // add grid to game
        game.setGrid(grid);
    }

    private void addAllStones(Game game) {
        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("a", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("b", 3));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("c", 3));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("d", 2));
        }

        for (int i = 0; i < 13; i++) {
            game.addStone(new Stone("e", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("f", 4));
        }

        for (int i = 0; i < 4; i++) {
            game.addStone(new Stone("g", 2));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("h", 4));
        }

        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("i", 1));
        }

        game.addStone(new Stone("j", 8));

        game.addStone(new Stone("k", 5));

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("l", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("m", 3));
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("n", 1));
        }

        for (int i = 0; i < 9; i++) {
            game.addStone(new Stone("o", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("p", 3));
        }

        game.addStone(new Stone("q", 10));

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("r", 1));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("s", 1));
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("t", 1));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("u", 1));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("v", 4));
        }

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("w", 4));
        }

        game.addStone(new Stone("x", 8));

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("y", 4));
        }

        game.addStone(new Stone("z", 10));
    }

    public void checkIfGameEnded(Game game) {
        // check if bag of game is empty
        if (game.getBag().isEmpty()) {
            List<Player> players = game.getPlayers();

            // check if a bag of a player is empty
            for (Player player : players) {

                // if a player has no stones left, the game has ended
                if (player.getBag().isEmpty()) {
                    game.setStatus(GameStatus.ENDED);
                    updateScore(game);
                    break;
                }
            }
        }
    }

    private void updateScore(Game game) {
        List<Player> players = game.getPlayers();

        // update score for every player/user
        for (Player player : players) {
            User user = player.getUser();
            user.setOverallScore(user.getOverallScore() + player.getScore());
            player.setScore(0);
        }
    }
}
