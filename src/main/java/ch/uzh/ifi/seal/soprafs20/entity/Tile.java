package ch.uzh.ifi.seal.soprafs20.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unique across the database -> composes the primary key
 */
@Entity
@Table(name = "Tile")
public class Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
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

    public String getStoneSymbol() {
        return stoneSymbol;
    }

    public void setStoneSymbol(String stoneSymbol) {
        this.stoneSymbol = stoneSymbol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMultivariant() {
        return multivariant;
    }

    public void setMultivariant(String multivariant) {
        this.multivariant = multivariant;
    }
}