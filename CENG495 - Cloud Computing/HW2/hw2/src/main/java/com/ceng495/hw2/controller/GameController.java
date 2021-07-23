package com.ceng495.hw2.controller;


import com.ceng495.hw2.model.*;
import com.ceng495.hw2.repository.GameRepository;
import com.ceng495.hw2.repository.UserRepository;
import com.ceng495.hw2.util.Authorization;
import com.ceng495.hw2.util.Precision;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
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

@Controller
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Authorization auth;

    @Autowired
    private Precision pre;


    @GetMapping("/addGameForm")
    public String viewAddGameForm(Model theModel) {

        Game theGame = new Game();
        //theGame.setEnableCommentAndRate("Enabled");
        theModel.addAttribute("game", theGame);

        List<String> genres = new ArrayList<>();
        genres.add("Action and Adventure");
        genres.add("Role-playing");
        genres.add("Simulation");
        genres.add("Strategy");
        genres.add("Other");


        theModel.addAttribute("genreChoices", genres);
        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }

        return "forms/add-game";
    }

    @PostMapping("/addGame")
    public String addGame(@ModelAttribute("game") Game theGame, RedirectAttributes redirAttrs) {

        try {
            theGame.setEnableCommentAndRate("Enabled");
            theGame.setPlayTime(0.0);
            theGame.setRating(0.0);
            List<GameComment> gameComments = new ArrayList<>();
            theGame.setAllComments(gameComments);
            gameRepository.save(theGame);
            redirAttrs.addFlashAttribute("success2", "Game is added successfully!");
            return "redirect:/";
        } catch (DuplicateKeyException e) {
            //e.printStackTrace();
            redirAttrs.addFlashAttribute("error_gamename_duplicate", "This game has already been added. Please choose another game to add!!!");
            return "redirect:/addGameForm";
        }
    }



   /* Remove Game deletes the game and all of it’s Play Time, Rating and
Comments. (The user’s total play time attribute will not be affected but the
    other attributes such as Most Played Game, Average of Ratings or
    Comments on user pages should be updated).*/


    @GetMapping("/deleteGame")
    public String deleteGame(@RequestParam("_id") String _id, @RequestParam("gamename") String gamename) {

        // update most Played Game
        Query query = new Query(Criteria.where("mostPlayedGame").is(gamename));

        List<User> toBeUpdatedUsers = mongoTemplate.find(query,User.class);

        Query query2 = new Query(Criteria.where("gamename").is(gamename));
        mongoTemplate.remove(query2, PlayTimeLog.class);


        for(int i=0;i<toBeUpdatedUsers.size();i++) {
            Query query3 = new Query(Criteria.where("username").is(toBeUpdatedUsers.get(i).getUsername()));
            query3.with(Sort.by(Sort.Direction.DESC, "playTime"));
            query3.limit(1);
            PlayTimeLog mostPlayedGameLog = mongoTemplate.findOne(query3, PlayTimeLog.class);

            Query query4 = new Query(Criteria.where("username").is(toBeUpdatedUsers.get(i).getUsername()));
            Update update = new Update();
            if (mostPlayedGameLog != null) {
                update.set("mostPlayedGame", mostPlayedGameLog.getGamename());
            } else {
                update.set("mostPlayedGame", "");
            }
            mongoTemplate.updateFirst(query4, update, User.class);
        }


        // update average of ratings of users
        List<User> users = userRepository.findAll();

        for(int i=0;i<users.size();i++) {
            Query query5 = new Query(Criteria.where("username").is(users.get(i).getUsername()));
            List<PlayTimeLog> logsOfUser = mongoTemplate.find(query5, PlayTimeLog.class);
            double totalRatings = 0.0;
            int count = 0;

            for(int j=0;j<logsOfUser.size();j++) {
                if (logsOfUser.get(j).getRating() > 0) {
                    totalRatings += logsOfUser.get(j).getRating();
                    count++;
                }
            }

            Query query6 = new Query(Criteria.where("username").is(users.get(i).getUsername()));
            Update update2 = new Update();
            if (count > 0) {
                update2.set("averageOfRatings", pre.twoPrecision(totalRatings/count));
            } else { // last game deleted case
                update2.set("averageOfRatings", 0.0);
            }
            mongoTemplate.updateFirst(query6, update2, User.class);
        }


        // update comments

        Query query7 = new Query(Criteria.where("comments.gamename").is(gamename));

        Update updateObj = new Update().pull("comments", new BasicDBObject("gamename", gamename));

        UpdateResult result = mongoTemplate.updateMulti(query7, updateObj, User.class);


        // update play time logs

        Query query8 = new Query(Criteria.where("gamename").is(gamename));
        mongoTemplate.remove(query8, PlayTimeLog.class);



        gameRepository.deleteById(_id);

        return "redirect:/";
    }

    @GetMapping("/enable")
    public String enable(@RequestParam("gamename") String gamename, RedirectAttributes redirAttrs) {

        Query query = new Query(Criteria.where("name").is(gamename));
        Update updateObj = new Update();
        updateObj.set("enableCommentAndRate", "Enabled");
        UpdateResult result = mongoTemplate.updateFirst(query, updateObj, Game.class);
        redirAttrs.addFlashAttribute("success3", "Game: " + gamename + " enabled!");

        return "redirect:/";
    }


    @GetMapping("/disable")
    public String disable(@RequestParam("gamename") String gamename, RedirectAttributes redirAttrs) {

        Query query = new Query(Criteria.where("name").is(gamename));
        Update updateObj = new Update();
        updateObj.set("enableCommentAndRate", "Disabled");
        mongoTemplate.updateFirst(query, updateObj, Game.class);
        redirAttrs.addFlashAttribute("error3", "Game: " + gamename + " disabled!");


        return "redirect:/";
    }


    @GetMapping("/games")
    public String gamesPage(Model theModel) {

        List<Game> games = gameRepository.findAll();
        theModel.addAttribute("games", games);

        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }
        return "games";
    }





    @GetMapping("/rateGameForm")
    public String rateGameForm(@RequestParam("_id") String _id,
                               @RequestParam("gamename") String gamename,
                               Model theModel,
                               RedirectAttributes redirectAttributes) {

        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }
        else {
            redirectAttributes.addFlashAttribute("rating_error", "You need to login to rate a game!!!");
            return "redirect:/";
        }

        List<Integer> points = new ArrayList<>();
        points.add(1);
        points.add(2);
        points.add(3);
        points.add(4);
        points.add(5);

        theModel.addAttribute("points", points);
        theModel.addAttribute("gamename", gamename);
        theModel.addAttribute("chosenPoint", 0);
        theModel.addAttribute("_id", _id);


        return "forms/rateGame";
    }



    @PostMapping("/rateGame")
    public String rateGame(@ModelAttribute(value = "chosenPoint") String chosenPoint,
                           @ModelAttribute(value = "_id") String _id,
                           @ModelAttribute(value = "gamename") String gamename,
                           RedirectAttributes redirAttrs) {

        /*System.out.println("Chosen point: " + chosenPoint);
        System.out.println("Game name " + gamename);
        System.out.println("Game id: " + _id);*/

        // update Average of Ratings of User
        String loggedInUser = auth.getLoggedInUser();

        Query query1 = new Query(Criteria.where("username").is(loggedInUser)
                .andOperator(Criteria.where("gamename").is(gamename)));



        if (mongoTemplate.exists(query1, PlayTimeLog.class)) {
            Update update1 = new Update();
            update1.set("rating", Double.parseDouble(chosenPoint));

            mongoTemplate.upsert(query1, update1, PlayTimeLog.class);
        } else {
            Update update1 = new Update();
            update1.set("playTime", 0.0);
            update1.set("rating", Double.parseDouble(chosenPoint));

            mongoTemplate.upsert(query1, update1, PlayTimeLog.class);
        }


        Query query2 = new Query(Criteria.where("username").is(loggedInUser));
        List<PlayTimeLog> logsOfUser = mongoTemplate.find(query2, PlayTimeLog.class);

        double totalRatings = 0.0;
        int count = 0;
        for(int i=0;i<logsOfUser.size();i++) {
            if (logsOfUser.get(i).getRating() > 0) {
                totalRatings += logsOfUser.get(i).getRating();
                count++;
            }

        }

        if (count > 0) {
            Update update2 = new Update();
            update2.set("averageOfRatings", pre.twoPrecision(totalRatings/count));
            mongoTemplate.updateFirst(query2, update2, User.class);
        }



       // update Rating of the Game

        Query query3 = new Query(Criteria.where("gamename").is(gamename));
        Query query4 = new Query(Criteria.where("name").is(gamename));

        Game game = mongoTemplate.findOne(query4, Game.class);
        double playTimeOfGame = game.getPlayTime();

        List<PlayTimeLog> gameLogs = mongoTemplate.find(query3,PlayTimeLog.class);

        double ratingSum = 0.0;
        for(int i=0;i<gameLogs.size();i++) {
            double playTime = gameLogs.get(i).getPlayTime();
            double rating = gameLogs.get(i).getRating();

            ratingSum += (playTime * rating);
        }

        Update update3 = new Update();
        if (playTimeOfGame == 0) {
            update3.set("rating", 0.0);
        } else {
            update3.set("rating", pre.twoPrecision(ratingSum/playTimeOfGame));
        }

        mongoTemplate.updateFirst(query4, update3, Game.class);



        redirAttrs.addFlashAttribute("rating_success", "Rating of the game " + gamename + " is successful!!!");


        return "redirect:/games";
    }



    @GetMapping("/playGameForm")
    public String playGameForm(@RequestParam("_id") String _id,
                               @RequestParam("gamename") String gamename,
                               Model theModel,
                               RedirectAttributes redirectAttributes) {

        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }
        else {
            redirectAttributes.addFlashAttribute("play_error", "You need to login to play a game!!!");
            return "redirect:/";
        }


        theModel.addAttribute("gamename", gamename);
        theModel.addAttribute("givenPlayTime", 0);
        theModel.addAttribute("_id", _id);


        return "forms/playGame";
    }




    @PostMapping("/playGame")
    public String playGame(@ModelAttribute(value = "givenPlayTime") String givenPlayTime,
                           @ModelAttribute(value = "_id") String _id,
                           @ModelAttribute(value = "gamename") String gamename,
                           RedirectAttributes redirAttrs) {

        String loggedInUser = auth.getLoggedInUser();
        Query query1 = new Query(Criteria.where("username").is(loggedInUser));

        User user = mongoTemplate.findOne(query1, User.class);

        Update update1 = new Update();
        update1.set("totalPlayTime",user.getTotalPlayTime() + Double.parseDouble(givenPlayTime));
        mongoTemplate.upsert(query1, update1, User.class);

        Query query2 = new Query(Criteria.where("username").is(loggedInUser)
                .andOperator(Criteria.where("gamename").is(gamename)));



        if (mongoTemplate.exists(query2, PlayTimeLog.class)) {
            PlayTimeLog log = mongoTemplate.findOne(query2, PlayTimeLog.class);
            Update update2 = new Update();
            update2.set("playTime", Double.parseDouble(givenPlayTime) + log.getPlayTime());

            mongoTemplate.upsert(query2, update2, PlayTimeLog.class);
        } else {
            Update update2 = new Update();
            update2.set("playTime", Double.parseDouble(givenPlayTime));
            update2.set("rating", 0.0);

            mongoTemplate.upsert(query2, update2, PlayTimeLog.class);
        }

        //updates Rating of the Game

        Query query3 = new Query(Criteria.where("gamename").is(gamename));
        Query query4 = new Query(Criteria.where("name").is(gamename));


        Game game = mongoTemplate.findOne(query4, Game.class);
        double playTimeOfGame = game.getPlayTime();

        Update update4 = new Update();
        update4.set("playTime", playTimeOfGame + Double.parseDouble(givenPlayTime));
        mongoTemplate.updateFirst(query4, update4, Game.class);

        List<PlayTimeLog> gameLogs = mongoTemplate.find(query3,PlayTimeLog.class);

        double ratingSum = 0.0;
        for(int i=0;i<gameLogs.size();i++) {
            double playTime = gameLogs.get(i).getPlayTime();
            double rating = gameLogs.get(i).getRating();

            ratingSum += (playTime * rating);
        }

        Update update3 = new Update();
        update3.set("rating", pre.twoPrecision(ratingSum / (playTimeOfGame + Double.parseDouble(givenPlayTime))));
        mongoTemplate.updateFirst(query4, update3, Game.class);


        // check Most Played Game attribute
        Query query5 = new Query(Criteria.where("username").is(loggedInUser));
        query5.with(Sort.by(Sort.Direction.DESC, "playTime"));
        query5.limit(1);
        PlayTimeLog mostPlayedGameLog = mongoTemplate.findOne(query5, PlayTimeLog.class);

        Update update5 = new Update();
        update5.set("mostPlayedGame", mostPlayedGameLog.getGamename());
        mongoTemplate.updateFirst(query5, update5, User.class);


        redirAttrs.addFlashAttribute("playing_success", "Playing of the game " + gamename + " is successful!!!");

        return "redirect:/games";
    }





    @GetMapping("/commentGameForm")
    public String commentGameForm(@RequestParam("_id") String _id,
                               @RequestParam("gamename") String gamename,
                               Model theModel,
                               RedirectAttributes redirectAttributes) {

        if (auth.loginCheck()) {
            theModel.addAttribute("loggedInUser", auth.getLoggedInUser());
        }
        else {
            redirectAttributes.addFlashAttribute("comment_error", "You need to login to comment a game!!!");
            return "redirect:/";
        }


        theModel.addAttribute("gamename", gamename);
        theModel.addAttribute("givenComment", "");
        theModel.addAttribute("_id", _id);


        return "forms/commentGame";
    }



    @PostMapping("/commentGame")
    public String commentGame(@ModelAttribute(value = "givenComment") String givenComment,
                           @ModelAttribute(value = "_id") String _id,
                           @ModelAttribute(value = "gamename") String gamename,
                           RedirectAttributes redirAttrs) {



        String loggedInUser = auth.getLoggedInUser();

        Query query0 = new Query(Criteria.where("username").is(loggedInUser)
        .andOperator(Criteria.where("gamename").is(gamename)));

        PlayTimeLog log = mongoTemplate.findOne(query0, PlayTimeLog.class);
        if (log == null || log.getPlayTime() < 1) {
            redirAttrs.addFlashAttribute("one_hour_exception", "You need to play game (" + gamename + ") at least 1 hour before comment about it." );
            return "redirect:/user";
        }




        // push comment to the user's comments
        Query query1 = new Query();
        Criteria sCriteria = Criteria.where("username").is(loggedInUser);
        sCriteria.and("comments.gamename").is(gamename);
        query1.addCriteria(sCriteria);
        Update sUpdate = new Update();
        sUpdate.set("comments.$.comment", givenComment);
        UpdateResult sUpdateResult = mongoTemplate.updateFirst(query1, sUpdate, User.class);
        if (sUpdateResult.getModifiedCount() == 0) {
            Query pQuery = new Query(Criteria.where("username").is(loggedInUser));
            Update pUpdate = new Update();
            UserComment userComment = new UserComment(gamename, givenComment);
            pUpdate.push("comments", userComment);
            mongoTemplate.updateFirst(pQuery, pUpdate, User.class);
        }


        // push comment to the game's allcomments
        Query query2 = new Query();
        Criteria sCriteria2 = Criteria.where("name").is(gamename);
        sCriteria2.and("allComments.username").is(loggedInUser);
        query2.addCriteria(sCriteria2);
        Update sUpdate2 = new Update();
        sUpdate2.set("allComments.$.comment", givenComment);
        UpdateResult sUpdateResult2 = mongoTemplate.updateFirst(query2, sUpdate2, Game.class);
        if (sUpdateResult2.getModifiedCount() == 0) {
            Query pQuery = new Query(Criteria.where("name").is(gamename));
            Update pUpdate = new Update();
            GameComment gameComment = new GameComment(loggedInUser, givenComment);
            pUpdate.push("allComments", gameComment);
            mongoTemplate.updateFirst(pQuery, pUpdate, Game.class);
        }


        redirAttrs.addFlashAttribute("comment_success", "Thank you for your comment on game: " + gamename);

        return "redirect:/games";
    }


}
