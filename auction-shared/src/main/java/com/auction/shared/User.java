package com.auction.shared;
public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected String age;
    protected boolean isactive;
    public User() {}
    public User(String u, String p, String e, String a) {
        this.username = u;
        this.password = p;
        this.email = e;
        this.age = a;
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
        String ans = this.password;
        return ans;
    }
    public void setpassword(String p) {
        this.password = p;
    }
    public String getemail() {
        String ans = this.email;
        return ans;
    }
    public void setemail(String e) {
        this.email = e;
    }
    public String getage() {
        String ans = this.age;
        return ans;
    }
    public void setage(String a) {
        this.age = a;
    }
    public boolean isactive() {
        boolean ans = this.isactive;
        return ans;
    }
    public void setactive(boolean a) {
        this.isactive = a;
    }
}