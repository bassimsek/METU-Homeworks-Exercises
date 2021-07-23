package com.ceng495.hw2.util;

import org.springframework.stereotype.Service;


@Service
public class Authorization {

    public String loggedInUser;

    public boolean loginCheck() {
        if (loggedInUser != null && !loggedInUser.equals("")) {
            return true;
        }

        return false;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
