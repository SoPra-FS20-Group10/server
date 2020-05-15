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
        Chat globalChat = chatService.getGlobal();

        if(globalChat == null){
            throw new NotFoundException("global chat could not be found");
        }

        List<Message> messages = globalChat.getMessages();
        List<MessageDTO> messageDTO = new ArrayList<>();

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

        Chat globalChat = chatService.getGlobal();
        if(globalChat == null){
            throw new NotFoundException("global chat could not be found");
        }

        Chat newchat = chatService.addMessage(globalChat,message);

        List<Message> messages = newchat.getMessages();
        List<MessageDTO> messageDTOs = new ArrayList<>();

        for(Message singleMessage :messages){
            messageDTOs.add(DTOMapper.INSTANCE.convertEntityToMessageDTO(singleMessage));
        }

        return messageDTOs;
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

        if(chat == null){
            throw new NotFoundException("chat could not be found");
        }

        Chat newchat = chatService.addMessage(chat,message);

        List<Message> messages = newchat.getMessages();
        List<MessageDTO> messageDTOs = new ArrayList<>();

        for(Message singleMessage :messages){
            messageDTOs.add(DTOMapper.INSTANCE.convertEntityToMessageDTO(singleMessage));
        }

        return messageDTOs;
    }
}