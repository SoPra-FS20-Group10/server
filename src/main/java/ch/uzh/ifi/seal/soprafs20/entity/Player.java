package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.PlayerStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal Player Representation
 * This class composes the internal representation of the player and defines how the player is stored in the database.
 */
@Entity
@Table(name = "Player")
public class Player implements Serializable {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private PlayerStatus status;

    @Column(nullable = false)
    private Integer score;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Stone> bag;

    @OneToOne
    private User user;

    @ManyToOne
    private Game game;

    public void initPlayer() {
        bag = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<Stone> getBag() {
        return bag;
    }

    public void setBag(List<Stone> bag) {
        this.bag = bag;
    }

    public void addStone(Stone stone) {
        bag.add(stone);
    }

    public void addStone(int index, Stone stone) {
        bag.add(index,stone);
    }

    public void removeStone(Stone stone) {
        bag.remove(stone);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}