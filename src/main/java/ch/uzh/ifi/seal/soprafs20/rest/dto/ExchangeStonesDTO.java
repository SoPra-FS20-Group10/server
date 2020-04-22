package ch.uzh.ifi.seal.soprafs20.rest.dto;

import java.util.List;

public class ExchangeStonesDTO {
    private String token;
    private List<Long> stoneIds;

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
}
