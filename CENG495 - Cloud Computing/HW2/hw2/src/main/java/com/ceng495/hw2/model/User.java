package com.ceng495.hw2.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Document(collection = "user")
public class User {

    @Id
    private String _id;

    @NotBlank
    @Indexed(unique = true)
    private String username;


    private Double averageOfRatings;

    private Double totalPlayTime;

    private String mostPlayedGame;

    private List<UserComment> comments;


    public User() {
        super();
    }

    public User(String username, Double averageOfRatings, Double totalPlayTime, String mostPlayedGame, List<UserComment> comments) {
        this.username = username;
        this.averageOfRatings = averageOfRatings;
        this.totalPlayTime = totalPlayTime;
        this.mostPlayedGame = mostPlayedGame;
        this.comments = comments;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getAverageOfRatings() {
        return averageOfRatings;
    }

    public void setAverageOfRatings(Double averageOfRatings) {
        this.averageOfRatings = averageOfRatings;
    }

    public Double getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(Double totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    public String getMostPlayedGame() {
        return mostPlayedGame;
    }

    public void setMostPlayedGame(String mostPlayedGame) {
        this.mostPlayedGame = mostPlayedGame;
    }

    public List<UserComment> getComments() {
        return comments;
    }

    public void setComments(List<UserComment> comments) {
        this.comments = comments;
    }

}
