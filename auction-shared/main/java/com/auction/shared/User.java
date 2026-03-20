package com.auction.shared;
import java.util.UUID;
public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected boolean active = true;
    public User() {}
    public User(String username, String password, String email) {
        if (validateinput(username, password, email)) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
    }
    public boolean validateinput(String u, String p, String e) {
        boolean ans = u != null && p != null && e != null && e.contains("@");
        return ans;
    }
    public boolean passwordchecking(String p) {
        boolean ans = this.password.equals(p);
        return ans;
    }
    public String getusername() { return username; }
    public void setusername(String username) { this.username = username; }
    public String getpassword() { return password; }
    public void setpassword(String password) { this.password = password; }
    public String getemail() { return email; }
    public void setemail(String email) { this.email = email; }
    public boolean isactive() { return active; }
    public void setactive(boolean active) { this.active = active; }
    public abstract String getrolename();
}