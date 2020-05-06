package ch.uzh.ifi.seal.soprafs20.entity;

public class ChatMessageWS {

    private String username;
    private String message;
    private int time;

    public ChatMessageWS(String username, String message, int time){
        this.username = username;
        this.message = message;
        this.time = time;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }



}
