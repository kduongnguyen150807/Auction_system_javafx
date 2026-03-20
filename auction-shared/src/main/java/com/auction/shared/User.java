package com.auction.shared;
public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected boolean isactive;
    public User() {}
    public User(String u, String p, String e) {
        this.username = u;
        this.password = p;
        this.email = e;
        this.isactive = true;
    }
    public abstract String getrolename();
    public String getusername() {
        String ans = this.username;
        return ans;
    }
    public void setusername(String u) {
        this.username = u;
    }
    public String getpassword() {
        return this.password;
    }
    public void setpassword(String p) {
        this.password = p;
    }
    public String getemail() {
        return this.email;
    }
    public void setemail(String e) {
        this.email = e;
    }
    public boolean isactive() {
        return this.isactive;
    }
    public void setactive(boolean a) {
        this.isactive = a;
    }
}s