package com.ceng495.hw2.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Document(collection = "game")
public class Game {

    @Id
    private String _id;

    @NotBlank
    @Indexed(unique=true)
    private String name;

    @NotBlank
    private List<String> genres;

    @NotBlank
    private String photo;

    @NotBlank
    private Double playTime;

    @NotBlank
    private Double rating;

    private List<GameComment> allComments;

    private String enableCommentAndRate;

    // optional ones

    private String releaseDate;
    private String pcRequirements;
    private String developerName;
    private String advertisementQuotes;



    public Game() {
        super();
    }

    public Game(String name, List<String> genres, String photo, Double playTime, Double rating, List<GameComment> allComments, String enableCommentAndRate) {
        this.name = name;
        this.genres = genres;
        this.photo = photo;
        this.playTime = playTime;
        this.rating = rating;
        this.allComments = allComments;
        this.enableCommentAndRate = enableCommentAndRate;
    }


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public List<GameComment> getAllComments() {
        return allComments;
    }

    public void setAllComments(List<GameComment> allComments) {
        this.allComments = allComments;
    }

    public String getEnableCommentAndRate() {
        return enableCommentAndRate;
    }

    public void setEnableCommentAndRate(String enableCommentAndRate) {
        this.enableCommentAndRate = enableCommentAndRate;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPcRequirements() {
        return pcRequirements;
    }

    public void setPcRequirements(String pcRequirements) {
        this.pcRequirements = pcRequirements;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getAdvertisementQuotes() {
        return advertisementQuotes;
    }

    public void setAdvertisementQuotes(String advertisementQuotes) {
        this.advertisementQuotes = advertisementQuotes;
    }
}
