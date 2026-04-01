package com.auction.server.service;

import com.auction.server.dao.UserDao;
import com.auction.shared.*;

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
  /** @return null nếu thành công; mã lỗi nếu không (vd. duplicate_email). */
  public String updateprofile(int userid, String fullname, String email, String phone) {
    return this.userdao.updateuserprofile(userid, fullname, email, phone);
  }

  public void updateavatar(String username, String ans) throws Exception {
    this.userdao.updateavatar(username, ans);
  }
  public java.util.List<User> getallusers() {
    java.util.List<User> ans = this.userdao.getallusers();
    return ans;
  }
  public boolean setuserlocked(String username, boolean lockstatus) {
    boolean ans = this.userdao.setuserlocked(username, lockstatus);
    return ans;
  }
}
