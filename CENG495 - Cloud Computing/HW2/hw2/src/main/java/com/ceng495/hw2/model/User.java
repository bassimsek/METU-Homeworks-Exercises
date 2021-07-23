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

    /*@Override
    public String toString() {
        return String.format(
                "User[id=%s, username='%s', ='%s', name='%s', surname='%s', birthdate='%s' gender='%s', isAdmin='%s']",
                _id, email, password, name, surname, birthdate, gender, isAdmin);
    }*/
}


/*package com.example.srdchw4.models;

        import java.util.Date;
        import javax.validation.constraints.NotBlank;
        import org.springframework.data.annotation.Id;
        import org.springframework.data.mongodb.core.index.Indexed;
        import org.springframework.data.mongodb.core.mapping.Document;



@Document(collection="users")
public class User {
    @Id
    private String _id;

    @NotBlank
    @Indexed(unique=true)
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    private Date birthdate;

    @NotBlank
    private String gender;

    @NotBlank
    private String isAdmin;



    public User() {
        super();
    }


    public User(String email, String password, String name, String surname, Date birthdate, String gender, String isAdmin) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.isAdmin = isAdmin;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getisAdmin() {
        return isAdmin;
    }

    public void setisAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }


    @Override
    public String toString() {
        return String.format(
                "User[id=%s, email='%s', password='%s', name='%s', surname='%s', birthdate='%s' gender='%s', isAdmin='%s']",
                _id, email, password, name, surname, birthdate, gender, isAdmin);
    }



}*/
