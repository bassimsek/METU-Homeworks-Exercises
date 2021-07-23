package com.ceng495.hw2.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;


@Document(collection = "playTimeLog")
public class PlayTimeLog {

    @Id
    private String _id;

    @NotBlank
    private String username;

    @NotBlank
    private String gamename;

    @NotBlank
    private Double playTime;

    @NotBlank
    private Double rating;

    public PlayTimeLog() {
        super();
    }

    public PlayTimeLog(String username, String gamename, Double playTime, Double rating) {
        this.username = username;
        this.gamename = gamename;
        this.playTime = playTime;
        this.rating = rating;
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

    public String getGamename() {
        return gamename;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public Double getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Double playTime) {
        this.playTime = playTime;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
