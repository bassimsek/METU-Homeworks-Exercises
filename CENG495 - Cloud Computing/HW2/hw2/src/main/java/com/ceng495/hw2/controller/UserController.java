package com.ceng495.hw2.controller;

import com.ceng495.hw2.model.*;
import com.ceng495.hw2.repository.GameRepository;
import com.ceng495.hw2.repository.PlayTimeLogRepository;
import com.ceng495.hw2.repository.UserRepository;
import com.ceng495.hw2.util.Authorization;
import com.ceng495.hw2.util.Precision;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayTimeLogRepository playTimeLogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Authorization auth;

    @Autowired
    private Precision pre;



    @GetMapping(value = "/")
    public String sayGreeting(Model theModel) {

        User theUser = new User();
        theModel.addAttribute("user", theUser);

        List<User> users = userRepository.findAll();
        List<Game> games = gameRepository.findAll();

        theModel.addAttribute("users", users);
        theModel.addAttribute("games", games);

        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }

        return "home";
    }


    @GetMapping("/addForm")
    public String viewAddForm(Model theModel) {

        User theUser = new User();
        theModel.addAttribute("user", theUser);
        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }

        return "forms/add-user";
    }

    @PostMapping("/save")
    public String addUser(@ModelAttribute("user") User theUser, RedirectAttributes redirAttrs) {

        try {
            theUser.setAverageOfRatings(0.0);
            theUser.setTotalPlayTime(0.0);
            theUser.setMostPlayedGame("");
            List<UserComment> comments = new ArrayList<>();
            theUser.setComments(comments);
            userRepository.save(theUser);
            redirAttrs.addFlashAttribute("success", "User is added successfully!");
            return "redirect:/";
        } catch (DuplicateKeyException e) {
            //e.printStackTrace();
            redirAttrs.addFlashAttribute("error_username_duplicate", "This username has already been taken. Please choose another username!!!");
            return "redirect:/addForm";
        }

    }



    @PostMapping("/login")
    public String login(@ModelAttribute("user") User theUser, RedirectAttributes redirAttrs) {

        Query query = new Query(Criteria.where("username").is(theUser.getUsername()));

        boolean isUserExists = mongoTemplate.exists(query, User.class);



        if (isUserExists) {
            //theModel.addAttribute("loggedInUser", theUser.getUsername());
            redirAttrs.addFlashAttribute("success0", "Login is successful with username: "+ theUser.getUsername());
            auth.setLoggedInUser(theUser.getUsername());
        } else {
            redirAttrs.addFlashAttribute("error0", "There is no user with this username. Try again with a correct username!");
        }


        return "redirect:/";

    }



    /*Remove User deletes an entry of a user and all of it’s attributes. When you
    delete a user, all of the affects of it’s actions are also deleted. This means that
    the games that the user played, rated and commented before are also affected.
            (i.e. remove the user’s comments from All Comments, subtract the user’s
    play time from Play Time of games, and calculate Rating of the Game
    without the deleted user’s rating)*/

    @GetMapping("/deleteUser")
    public String deleteUser(@RequestParam("_id") String _id, @RequestParam("username") String username) {

        List<Game> games = gameRepository.findAll();


        // update user comments
        Query query = new Query(Criteria.where("allComments.username").is(username));

        Update updateObj = new Update().pull("allComments", new BasicDBObject("username", username));

        UpdateResult result = mongoTemplate.updateMulti(query, updateObj, Game.class);


        //subtract the user’s play time from Play Time of games

        Query query1 = new Query(Criteria.where("username").is(username));
        List<PlayTimeLog> userPlayTimes = mongoTemplate.find(query1, PlayTimeLog.class);

        for(int i=0;i<userPlayTimes.size();i++) {
            String gameName = userPlayTimes.get(i).getGamename();
            for(int j=0;j<games.size();j++) {
                if (gameName.equals(games.get(j).getName())) {
                    games.get(j).setPlayTime(games.get(j).getPlayTime() - userPlayTimes.get(i).getPlayTime());

                    Query query3 = new Query(Criteria.where("name").is(games.get(j).getName()));
                    Update update = new Update();
                    update.set("playTime", games.get(j).getPlayTime());
                    mongoTemplate.updateFirst(query3, update, Game.class);
                    break;
                }
            }
        }

        Query query2 = new Query(Criteria.where("username").is(username));

        mongoTemplate.remove(query2, PlayTimeLog.class);



        // update playTimeLogs
        Query query5 = new Query(Criteria.where("username").is(username));
        mongoTemplate.remove(query5, PlayTimeLog.class);

        List<PlayTimeLog> updatedPlayTimeLogs = playTimeLogRepository.findAll();

        // calculate Rating of the Game without the deleted user’s rating

        for(int i=0;i<games.size();i++) {
            double ratingSum = 0.0;
            for(int j=0;j<updatedPlayTimeLogs.size();j++) {
                if(games.get(i).getName().equals(updatedPlayTimeLogs.get(j).getGamename())) {
                    double rating = updatedPlayTimeLogs.get(j).getPlayTime() * updatedPlayTimeLogs.get(j).getRating();
                    ratingSum += rating;
                }
            }
            if(games.get(i).getPlayTime() != null && games.get(i).getPlayTime() != 0) {
                games.get(i).setRating(pre.twoPrecision(ratingSum/games.get(i).getPlayTime()));
            }
            else { // last played user deleted case
                games.get(i).setRating(0.0);
            }


            Query query4 = new Query(Criteria.where("name").is(games.get(i).getName()));
            Update update2 = new Update();
            update2.set("rating", games.get(i).getRating());
            mongoTemplate.updateFirst(query4, update2, Game.class);
        }


        userRepository.deleteById(_id);

        if (auth.getLoggedInUser() == null || auth.getLoggedInUser().equals(username)) {
            auth.setLoggedInUser("");
        }

        return "redirect:/";
    }


    /*The order of the comments is done by the user’s own play time on the games that are
    commented. (Game names are also shown with the comments)*/

    @GetMapping("/user")
    public String userPage(Model theModel) {


        List<User> users = userRepository.findAll();
        List<Game> games = gameRepository.findAll();

        theModel.addAttribute("users", users);
        theModel.addAttribute("games", games);
        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }

        return "user";
    }

}
