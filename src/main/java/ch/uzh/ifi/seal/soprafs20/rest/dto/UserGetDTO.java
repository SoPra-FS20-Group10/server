package ch.uzh.ifi.seal.soprafs20.rest.dto;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserGetDTO {
    private Long id;
    private String token;
    private String username;
    private UserStatus status;
    private Date birthday;
    private Date cakeDay;
    private int playTime;
    private int overallScore;
    private int playedGames;
    private int wonGames;
    private float winPercentage;
    private String historyString;
    private List<Integer> historyList = new ArrayList<>();
    private String historyTimeString;
    private List<Integer> historyTimeList = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getCakeDay() {
        return cakeDay;
    }

    public void setCakeDay(Date cakeDay) {
        this.cakeDay = cakeDay;
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
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

    public float getWinPercentage() {
        return winPercentage;
    }

    public void setWinPercentage(float winPercentage) {
        this.winPercentage = winPercentage;
    }
    public String getHistoryString() {
        return historyString;
    }

    public void setHistoryString(String historyString) {
        this.historyString = historyString;
    }

    public List<Integer> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<Integer> historyList) {
        this.historyList = historyList;
    }

    public String getHistoryTimeString() {
        return historyTimeString;
    }

    public void setHistoryTimeString(String historyTimeString) {
        this.historyTimeString = historyTimeString;
    }

    public List<Integer> getHistoryTimeList() {
        return historyTimeList;
    }

    public void setHistoryTimeList(List<Integer> historyTimeList) {
        this.historyTimeList = historyTimeList;
    }
}
