package com.auction.server.service;
import com.auction.shared.*;
import com.auction.server.dao.UserDao;
public class UserService {
    private UserDao userdao;
    public UserService() {
        this.userdao = new UserDao();
    }
    public User login(String u, String p) {
        User ans = this.userdao.login(u, p);
        return ans;
    }
    public boolean signup(User u) {
        boolean ans = this.userdao.signup(u);
        return ans;
    }
}