package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.boardcomponents.Board;
import ch.uzh.ifi.seal.soprafs20.boardcomponents.Scoreboard;
import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unique across the database -> composes the primary key
 */
@Entity
@Table(name = "Game")
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Transient
    private List<Player> players;

    @Transient
    private Scoreboard scoreboard;

    @Column(nullable = false)
    private GameStatus status;

    @Column(nullable = false)
    private int ownerid;

    @Column(nullable = false)
    private int chatid;

    @Column
    private String password;

    @Transient
    private Board board;







    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
