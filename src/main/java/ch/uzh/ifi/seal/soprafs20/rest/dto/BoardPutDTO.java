package ch.uzh.ifi.seal.soprafs20.rest.dto;

import java.util.List;

public class BoardPutDTO {
    private String token;
    private List<StoneGetDTO> stones;
    private List<TileGetDTO> tiles;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<StoneGetDTO> getStones() {
        return stones;
    }

    public void setStones(List<StoneGetDTO> stones) {
        this.stones = stones;
    }

    public List<TileGetDTO> getTiles() {
        return tiles;
    }

    public void setTiles(List<TileGetDTO> tiles) {
        this.tiles = tiles;
    }

}
