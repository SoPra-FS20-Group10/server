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

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public List<Player> getPlayers(long gameId) {
        // fetch game from db
        Game game = getGame(gameId);

        return game.getPlayers();
    }

    public List<Word> getWords(long gameId) {
        Game game;
        Optional<Game> foundGame = gameRepository.findByIdIs(gameId);

        // check if game is present
        if (foundGame.isPresent()) {
            game = foundGame.get();
        } else {
            throw new NotFoundException("The game with the id " + gameId + " could not be found");
        }

        return game.getWords();
    }

    public void saveGame(Game game) {
        gameRepository.save(game);
        gameRepository.flush();
    }

    public void addChat(Chat chat) {
        // add player to the user
        Game game = chat.getGame();
        game.setChat(chat);

        // save change
        saveGame(game);
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

        // check if game is waiting
        if (game.getStatus() != GameStatus.WAITING) {
            throw new ConflictException("The game is already running. You cannot join a running game.");
        }

        // check that only 4 players can play the game
        if (game.getPlayers().size() == 4) {
            throw new ConflictException("The game has already 4 players. You cannot join a full game.");
        }

        // check if password is correct
        if (!(game.getPassword() == null || password == null)){
            if (!(game.getPassword().equals(password))){
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

        // check if there are at least two players
        /*
        if (game.getPlayers().size() < 2) {
            throw new ConflictException("The game must have at least 2 players to start.");
        }

         */

        // set flag to running
        game.setStatus(GameStatus.RUNNING);

        // save change
        saveGame(game);
    }

    public Game leaveGame(Game game, Player player, String token) {
        // check if player is the lobbyLeader
        if (game.getStatus() != GameStatus.ENDED && player.getUser().getId().equals(game.getOwner().getId())) {
            throw new UnauthorizedException("The game owner cannot leave the game. Choose to end the game.");
        }

        // check if the user is authorized to leave the game
        if (!player.getUser().getToken().equals(token) && !game.getOwner().getToken().equals(token)) {
            throw new ConflictException("The user is not authorized to leave this game");
        }

        // remove the player
        game.removePlayer(player);

        // save changes
        game = gameRepository.save(game);
        gameRepository.flush();

        return game;
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
        game.addStone(new Stone("q", 10));
        game.addStone(new Stone("j", 8));
        game.addStone(new Stone("k", 5));
        game.addStone(new Stone("x", 8));
        game.addStone(new Stone("z", 10));

        for (int i = 0; i < 3; i++) {
            game.addStone(new Stone("b", 3));
            game.addStone(new Stone("c", 3));
            game.addStone(new Stone("f", 4));
            game.addStone(new Stone("h", 4));
            game.addStone(new Stone("p", 3));
            game.addStone(new Stone("v", 4));
            game.addStone(new Stone("w", 4));
            game.addStone(new Stone("y", 4));
            game.addStone(new Stone("m", 3));
        }

        for (int i = 0; i < 4; i++) {
            game.addStone(new Stone("g", 2));
        }

        for (int i = 0; i < 5; i++) {
            game.addStone(new Stone("d", 2));
            game.addStone(new Stone("l", 1));
            game.addStone(new Stone("s", 1));
            game.addStone(new Stone("u", 1));
        }

        for (int i = 0; i < 7; i++) {
            game.addStone(new Stone("n", 1));
            game.addStone(new Stone("r", 1));
            game.addStone(new Stone("t", 1));
        }

        for (int i = 0; i < 9; i++) {
            game.addStone(new Stone("o", 1));
        }

        for (int i = 0; i < 10; i++) {
            game.addStone(new Stone("a", 1));
            game.addStone(new Stone("i", 1));
        }

        for (int i = 0; i < 13; i++) {
            game.addStone(new Stone("e", 1));
        }
    }

    public void checkIfGameEnded(Game game) {
        // check if bag of game is empty
        if (!game.getBag().isEmpty()) {
            return;
        }

        // fetch the players from the game
        List<Player> players = game.getPlayers();

        // check if a bag of a player is empty -> if a bag of one player is empty, the game ends
        for (Player player : players) {
            if (player.getBag().isEmpty()) {
                game.setStatus(GameStatus.ENDED);
                updateScore(game);
                break;
            }
        }
    }

    private void updateScore(Game game) {
        List<Player> players = game.getPlayers();

        //determine winner(s)
        List<Player> winners = new ArrayList<>();
        winners.add(players.get(0));

        for(Player player1: players){
            for(Player player2 : players){
                if(player1.getScore() > winners.get(0).getScore()){
                    winners.clear();
                    winners.add(player1);
                }

                if(player2.getScore() == player1.getScore() && player1 != player2){
                    winners.add(player2);
                }
            }
        }

        //if highest score is 0, doesnt count as game
        if(winners.get(0).getScore() == 0){
            return;
        }

        for(Player winner : winners){
            winner.getUser().setWonGames(winner.getUser().getWonGames() + 1);
        }

        // update score for every player/user
        for (Player player : players) {
            User user = player.getUser();
            user.setPlayedGames(user.getPlayedGames() + 1);
            user.setOverallScore(user.getOverallScore() + player.getScore());

            //manage userHistory
            manageHistory(player,user);

            //manage userHistoryTime
            manageHistoryTime(user);
        }
    }

    protected void manageHistory(Player player, User user){
        int length;
        String history = user.getHistory();

        if (history.isEmpty()){
            length = 0;
        } else {
            String[] words = history.split("\\s+");
            length = words.length;
        }

        if (length < 10){
            user.setHistory(user.getHistory() + player.getScore() + " ");
        } else {
            int index = history.indexOf(' ') + 1;
            user.setHistory(user.getHistory().substring(index) + player.getScore() + " ");
        }
    }

    protected void manageHistoryTime(User user){
        int length;
        String historyTime = user.getHistoryTime();

        if (historyTime.isEmpty()){
            length = 0;
        } else {
            String[] words = historyTime.split("\\s+");
            length = words.length;
        }

        if (length < 10){
            user.setHistoryTime(user.getHistoryTime() + System.currentTimeMillis() + " ");
        } else {
            int index = historyTime.indexOf(' ') + 1;
            user.setHistoryTime(user.getHistoryTime().substring(index) + System.currentTimeMillis() + " ");
        }
    }
}