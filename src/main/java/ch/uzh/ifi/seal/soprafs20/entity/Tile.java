package ch.uzh.ifi.seal.soprafs20.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Internal Tile Representation
 * This class composes the internal representation of the tile and defines how the tile is stored in the database.
 */
@Entity
@Table(name = "Tile")
public class Tile implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String stoneSymbol;

    @Column
    private int value;

    @Column
    private int multiplier;

    @Column
    private String multivariant;

    public Tile() {}

    public Tile(int multiplier){
        this.multiplier = multiplier;
    }

    public Tile(int multiplier, String stoneSymbol){
        this.multiplier = multiplier;
        this.stoneSymbol = stoneSymbol;
    }

    public Tile(int multiplier, String stoneSymbol, String multivariant) {
        this.multiplier = multiplier;
        this.stoneSymbol = stoneSymbol;
        this.multivariant = multivariant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMultivariant() {
        return multivariant;
    }

    public void setMultivariant(String multivariant) {
        this.multivariant = multivariant;
    }
}