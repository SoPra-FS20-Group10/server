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

	@Column
    private Long Playerid;
	
	@Column(nullable = false) 
	private String name;
	
	@Column(nullable = false, unique = true) 
	private String username;
	
	@Column(nullable = false, unique = true) 
	private String token;

	@Column(nullable = false)
	private UserStatus status;

	@Column(nullable = false)
    private Date cakeday;

	@Column()
    private Date birthday;

	@Column(nullable = false)
    private String password;

	public Long getId() {
		return id;
	}

	public void setId(long id) {
	    this.id = id;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Date getCakeday() {
	    return cakeday;
    }

    public void setCakeday(Date date) {
	    cakeday = date;
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
}
