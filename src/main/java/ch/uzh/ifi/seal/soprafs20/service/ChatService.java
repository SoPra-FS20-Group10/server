package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
    private PlayerRepository playerRepository;
    private GameRepository gameRepository;
    private SimpMessagingTemplate simp;

    public ChatService(@Qualifier("playerRepository")PlayerRepository playerRepository, @Qualifier("gameRepository")GameRepository gameRepository){
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    public void sendToGame(long gameId, String destination, Object message){
        Optional<Game> game1 = gameRepository.findById(gameId);
        Game game = game1.get();
        List<Player> players = game.getPlayers();
        for( Player player: players){

        }

    }

    protected void sendToPlayer(long gameId, String player, Object message) {

        simp.convertAndSendToUser(player, "/queue/lobby/" + gameId, message);
    }




}
