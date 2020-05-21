package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Chat;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
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
        // check if message is empty
        if (message.getMessage().isEmpty()) {
            throw new ConflictException("Cannot send an empty message.");
        }

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
        Chat chat;
        Optional<Chat> foundChat = chatRepository.findByType("global");

        // check if foundChat exists
        if(foundChat.isPresent()){
            chat = foundChat.get();
        } else {
            throw new NotFoundException("global not found");
        }

        return chat;
    }

    private void initGlobal(){
        Chat chat = new Chat();
        chat.initChat();
        chat.setId(0L);
        chat.setType("global");

        chatRepository.save(chat);
        chatRepository.flush();
    }
}