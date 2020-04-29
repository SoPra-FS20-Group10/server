package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unique across the database -> composes the primary key
 */
@Entity
@Table(name = "User")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable = false, unique = true) 
	private String username;
	
	@Column(nullable = false, unique = true) 
	private String token;

    @Column(nullable = false)
    private String password;

	@Column(nullable = false)
	private UserStatus status;

	@Column(nullable = false)
    private Date cakeDay;

	@Column()
    private Date birthday;

    @Column()
    private int playtime;

    @Column()
    private float winPercentage = 0;

    @Column()
    private int overallScore = 0;
 
    @Column()
    private int playedGames = 0;

    @Column()
    private int wonGames = 0;

	@OneToOne(mappedBy = "user")
    private Player player;

	@OneToOne
    private Game game;

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public Date getCakeDay() {
	    return cakeDay;
    }

    public void setCakeDay(Date date) {
	    cakeDay = date;
    }

    public Date getBirthday() {
	    return birthday;
    }

    public void setBirthday(Date date) {
	    this.birthday = date;
    }

    public String getPassword() {
	    return password;
    }

    public void setPassword(String password) {
	    this.password = password;
    }

    public Player getPlayer() {
	    return player;
    }

    public void setPlayer(Player player) {
	    this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getPlaytime() {
        return playtime;
    }

    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    public float getWinPercentage() {
	    if(playedGames == 0){
	        return 0;
        }

        return (float) wonGames / playedGames;
    }

    public void setWinPercentage() {
        if(playedGames == 0){
            this.winPercentage = 0;
        } else {
            this.winPercentage = (float) wonGames / playedGames;
        }
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public int getPlayedGames() {
        return playedGames;
    }

    public void setPlayedGames(int playedGames) {
        this.playedGames = playedGames;
    }

    public int getWonGames() {
        return wonGames;
    }

    public void setWonGames(int wonGames) {
        this.wonGames = wonGames;
    }
}
