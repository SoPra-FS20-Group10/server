package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Chat;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.entity.Player;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.ChatRepository;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final GameRepository gameRepository;
    private final MessageRepository messageRepository;
    private SimpMessagingTemplate simp;

    public ChatService(@Qualifier("gameRepository")GameRepository gameRepository,
                       @Qualifier("chatRepository")ChatRepository chatRepository,
                       @Qualifier("messageRepository")MessageRepository messageRepository){
        this.gameRepository = gameRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;

        initGlobal();
    }

    public Chat createChat(Game game){
        Chat chat = new Chat();
        chat.initchat();
        chat.setGame(game);

        chat = chatRepository.save(chat);
        chatRepository.flush();

        return chat;
    }

    public Chat addMessage(Chat chat, Message message){
        messageRepository.save(message);
        messageRepository.flush();

        chat.addMessage(message);

        chatRepository.save(chat);
        chatRepository.flush();

        return chat;
    }

    public void deleteChat(Game game){
        Chat chat = game.getChat();

        chatRepository.delete(chat);
        chatRepository.flush();
    }

    public Chat getGlobalChat(){
        Optional<Chat> chat = chatRepository.findById(0L);
        if (chat.isPresent()){
            return chat.get();
        }else{
            Chat newChat = new Chat();
            newChat.setId(0L);

            newChat.initchat();

            chatRepository.save(newChat);
            chatRepository.flush();

            return newChat;
        }
    }

    public Chat getGlobal(){
        Optional<Chat> chat = chatRepository.findByType("global");
        if(chat.isPresent()){
            return chat.get();
        }else{
            throw new NotFoundException("global not found");
        }
    }

    public void initGlobal(){
        Chat chat = new Chat();
        chat.initchat();
        chat.setId(0L);
        chat.setType("global");

        chatRepository.save(chat);
        chatRepository.flush();
    }

    //WS
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