package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {
    private final ChatService chatService;

    ChatController(ChatService chatService){
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getGlobalMessages() {
        //TODO: implement
    }

    @PutMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void sendGlobalMessages() {
        //TODO: implement
    }

    @GetMapping("/chat/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getLocalMessages(@PathVariable ("gameId") Long gameId) {
        //TODO: implement
    }

    @PutMapping("/chat/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void sendLocalMessages() {
        //TODO: implement
    }
}