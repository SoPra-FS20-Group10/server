package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Chat;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.ChatRepository;
import ch.uzh.ifi.seal.soprafs20.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatService(@Qualifier("chatRepository")ChatRepository chatRepository,
                       @Qualifier("messageRepository")MessageRepository messageRepository){
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;

        initGlobal();

    }

    public Chat createChat(Game game){
        Chat chat = new Chat();
        chat.initChat();
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

    public void deleteChat(Chat chat){
        chatRepository.delete(chat);
        chatRepository.flush();
    }

    public Chat getGlobal(){
        Optional<Chat> chat = chatRepository.findByType("global");
        if(chat.isPresent()){
            return chat.get();
        } else {
            throw new NotFoundException("global not found");
        }
    }

    public void initGlobal(){
        Chat chat = new Chat();
        chat.initChat();
        chat.setId(0L);
        chat.setType("global");

        chatRepository.save(chat);
        chatRepository.flush();
    }
}