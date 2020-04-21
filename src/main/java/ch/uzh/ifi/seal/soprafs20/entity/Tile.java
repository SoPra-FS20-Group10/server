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
    private int value;

    @ManyToOne
    private Board board;

    public String getStoneSymbol() {
        return stoneSymbol;
    }

    public void setStoneSymbol(String stonesymbol) {
        this.stoneSymbol = stonesymbol;
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

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}