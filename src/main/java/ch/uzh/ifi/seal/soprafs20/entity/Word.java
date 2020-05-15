package ch.uzh.ifi.seal.soprafs20.entity;

import javax.persistence.*;

@Entity
@Table(name = "Word")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String word;

    @Column
    private int value;

    public Word() {}

    public Word(String word, int value) {
        this.word = word;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
