package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class MessageDTO {

    private String username;

    private int time;

    private String message;

    public MessageDTO(){}

    public MessageDTO(String username, String message){
        this.username = username;
        this.message = message;
    }
}
