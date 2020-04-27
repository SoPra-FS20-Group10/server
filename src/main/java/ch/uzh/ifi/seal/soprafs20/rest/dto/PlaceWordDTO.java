package ch.uzh.ifi.seal.soprafs20.rest.dto;

import java.util.List;

public class PlaceWordDTO {
    private String token;
    private List<Long> stoneIds;
    private List<Integer> coordinates;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Long> getStoneIds() {
        return stoneIds;
    }

    public void setStoneIds(List<Long> stoneIds) {
        this.stoneIds = stoneIds;
    }

    public List<Integer> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Integer> coordinates) {
        this.coordinates = coordinates;
    }
}
