package ch.uzh.ifi.seal.soprafs20.rest.dto;

public class StonePutDTO {
    String symbol;
    long index;


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

}
