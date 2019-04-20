package com.xhinliang.jugg.context;

/**
 * @author xhinliang
 */
public class JuggUser {

    private String userName = "guest";

    private boolean login = false;

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public String getUsername() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
