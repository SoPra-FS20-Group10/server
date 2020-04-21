package ch.uzh.ifi.seal.soprafs20.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

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
    private String stonesymbol;

    @Column
    private int value;

    @Column
    private int multiplier;

    @ManyToOne
    private Board board;

    public String getStonesymbol() {
        return stonesymbol;
    }

    public void setStonesymbol(String stonesymbol) {
        this.stonesymbol = stonesymbol;
    }

    public Long getId() {
        return id;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

}