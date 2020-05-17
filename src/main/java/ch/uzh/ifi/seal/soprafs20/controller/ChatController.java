package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.entity.Chat;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Message;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.rest.dto.MessageDTO;
import ch.uzh.ifi.seal.soprafs20.rest.mapper.DTOMapper;
import ch.uzh.ifi.seal.soprafs20.service.ChatService;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatController {
    private final ChatService chatService;
    private  final GameService gameService;

    ChatController(ChatService chatService,GameService gameService){
        this.chatService = chatService;
        this.gameService = gameService;
    }

    @GetMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<MessageDTO> getGlobalMessages() {
        // fetch global chat from db
        Chat globalChat = chatService.getGlobal();

        // fetch messages from chat
        List<Message> messages = globalChat.getMessages();
        List<MessageDTO> messageDTO = new ArrayList<>();

        // parse messages into MessageDTOs
        for(Message message :messages){
            messageDTO.add(DTOMapper.INSTANCE.convertEntityToMessageDTO(message));
        }

        return messageDTO;
    }

    @PutMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<MessageDTO> sendGlobalMessages(@RequestBody MessageDTO messageDTO) {
        Message message = DTOMapper.INSTANCE.convertMessageDTOtoEntity(messageDTO);

        // fetch globalChat from db
        Chat chat = chatService.getGlobal();

        // add to chat and parse into DTO
        return addToChatAndParseToDTO(chat, message);
    }

    @GetMapping("/chat/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<MessageDTO> getLocalMessages(@PathVariable ("gameId") Long gameId) {
        Game game = gameService.getGame(gameId);
        Chat chat = game.getChat();

        if(chat == null){
            throw new NotFoundException("chat could not be found");
        }

        List<Message> messages = chat.getMessages();
            List<MessageDTO> messageDTOs = new ArrayList<>();

            for(Message message :messages){
            messageDTOs.add(DTOMapper.INSTANCE.convertEntityToMessageDTO(message));
        }

        return messageDTOs;
    }

    @PutMapping("/chat/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<MessageDTO> sendLocalMessages(@PathVariable ("gameId") Long gameId, @RequestBody MessageDTO messageDTO) {
        Message message = DTOMapper.INSTANCE.convertMessageDTOtoEntity(messageDTO);
        Game game = gameService.getGame(gameId);
        Chat chat = game.getChat();

        // check if game has a chat
        if(chat == null){
            throw new NotFoundException("chat could not be found");
        }

        // add to chat and parse into DTO
        return addToChatAndParseToDTO(chat, message);
    }

    private List<MessageDTO> addToChatAndParseToDTO(Chat chat, Message message) {
        // add message to the chat
        chat = chatService.addMessage(chat,message);

        // fetch messages from the chat
        List<Message> messages = chat.getMessages();
        List<MessageDTO> messageDTOs = new ArrayList<>();

        // parse Message entities into MessageDTOs
        for(Message singleMessage :messages){
            messageDTOs.add(DTOMapper.INSTANCE.convertEntityToMessageDTO(singleMessage));
        }

        return messageDTOs;
    }
}