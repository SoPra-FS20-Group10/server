package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(@Qualifier("playerRepository")PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(User user) {
        // check if player already exists
        if (doesPlayerExist(user.getId())) {
            throw new ConflictException("The user is already in another game.");
        }

        // create player
        Player player = new Player();

        // set fields
        player.setId(user.getId());
        player.setUsername(user.getUsername());
        player.setStatus(PlayerStatus.NOT_READY);
        player.setScore(0);
        player.setUser(user);

        // save in db
        Player createdPlayer = playerRepository.save(player);
        playerRepository.flush();

        return createdPlayer;
    }

    public Player getPlayer(long playerId) {
        // fetch player from db
        Player player;
        Optional<Player> foundPlayer = playerRepository.findById(playerId);

        // check if player exists
        if (foundPlayer.isEmpty()) {
            throw new NotFoundException("The player with the id " + playerId + " does not exist.");
        } else {
            player = foundPlayer.get();
        }

        return player;
    }

    public void deletePlayer(Player player) {
        playerRepository.delete(player);
        playerRepository.flush();
    }

    public void addGame(Player player, Game game) {
        player.setGame(game);

        playerRepository.save(player);
        playerRepository.flush();
    }

    private boolean doesPlayerExist(long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);

        return player.isPresent();
    }
}
