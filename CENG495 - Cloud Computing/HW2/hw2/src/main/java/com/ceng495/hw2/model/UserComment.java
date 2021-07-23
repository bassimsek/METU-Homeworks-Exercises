package com.ceng495.hw2.model;

public class UserComment {

    private String gamename;

    private String comment;

    public UserComment(String gamename, String comment) {
        this.gamename = gamename;
        this.comment = comment;
    }

    public String getGamename() {
        return gamename;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
