package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.GameStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal Game Representation
 * This class composes the internal representation of the game and defines how the game is stored in the database.
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

    @Column
    private String name;

    @Column
    private GameStatus status;

    @Column
    private String password;

    @OneToOne(mappedBy = "game")
    private User owner;

    @OneToOne
    private Chat chat;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Tile> grid;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Stone> bag;

    @OneToMany(mappedBy = "game")
    private List<Player> players;

    @Column
    private long currentPlayerId;

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Tile> getGrid() {
        return grid;
    }

    public void setGrid(List<Tile> grid) {
        this.grid = grid;
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

    public void removeStone(Stone stone) {
        bag.remove(stone);
    }

    public void initGame() {
        players = new ArrayList<>();
        bag = new ArrayList<>();
        grid = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public long getCurrentPlayerId() {
        return this.currentPlayerId;
    }

    public void setCurrentPlayerId(long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }
}
