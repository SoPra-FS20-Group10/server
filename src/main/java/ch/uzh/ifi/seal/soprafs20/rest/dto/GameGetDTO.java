package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;

import java.util.List;

public class GameGetDTO {
    private Long id;
    private String name;
    private GameStatus status;
    private long currentPlayerId;
    private List<StoneGetDTO> stones;
    private List<TileGetDTO> board;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public List<StoneGetDTO> getStones() {
        return stones;
    }

    public void setStones(List<StoneGetDTO> stones) {
        this.stones = stones;
    }

    public List<TileGetDTO> getBoard() {
        return board;
    }

    public void setBoard(List<TileGetDTO> board) {
        this.board = board;
    }
}