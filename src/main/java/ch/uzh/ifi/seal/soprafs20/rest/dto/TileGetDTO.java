package ch.uzh.ifi.seal.soprafs20.rest.dto;

import javax.persistence.Column;

public class TileGetDTO {
    private String stoneSymbol;
    private int value;
    private int multiplier;
    private String multivariant;

    public String getMultivariant() {
        return multivariant;
    }

    public void setMultivariant(String multivariant) {
        this.multivariant = multivariant;
    }

    public String getStoneSymbol() {
        return stoneSymbol;
    }

    public void setStoneSymbol(String stoneSymbol) {
        this.stoneSymbol = stoneSymbol;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }



}
