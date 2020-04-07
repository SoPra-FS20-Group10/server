package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
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

    public void createPlayer(User user) {
        Player player = new Player();

        player.setId(user.getId());
        player.setUsername(user.getUsername());
        player.setScore(0);

        playerRepository.save(player);
        playerRepository.flush();
    }

    public Player getPlayer(long playerId) {
        // fetch player from db
        Player player;
        Optional<Player> foundPlayer = playerRepository.findById(playerId);

        // check if player exists
        if (foundPlayer.isEmpty()) {
            throw new SopraServiceException("The player with the id " + playerId + " does not exist.");
        } else {
            player = foundPlayer.get();
        }

        return player;
    }
}
