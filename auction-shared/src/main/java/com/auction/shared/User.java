package com.auction.shared;
public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected String age;
    protected String phonenumber;
    protected double balance;
    protected boolean isactive;
    protected boolean islocked;
    public User() {}
    public User(String u, String p, String e, String a, String ph) {
        this.username = u;
        this.password = p;
        this.email = e;
        this.age = a;
        this.phonenumber = ph;
        this.balance = 0.0;
        this.isactive = true;
        this.islocked = false;
    }
    public abstract UserRole getrole();
    public String getusername() { String ans = this.username; return ans; }
    public void setusername(String u) { this.username = u; }
    public String getpassword() { String ans = this.password; return ans; }
    public void setpassword(String p) { this.password = p; }
    public String getemail() { String ans = this.email; return ans; }
    public void setemail(String e) { this.email = e; }
    public String getage() { String ans = this.age; return ans; }
    public void setage(String a) { this.age = a; }
    public String getphonenumber() { String ans = this.phonenumber; return ans; }
    public void setphonenumber(String ph) { this.phonenumber = ph; }
    public double getbalance() { double ans = this.balance; return ans; }
    public void setbalance(double b) { this.balance = b; }
    public boolean isactive() { boolean ans = this.isactive; return ans; }
    public void setactive(boolean a) { this.isactive = a; }
    public boolean islocked() { boolean ans = this.islocked; return ans; }
    public void setlocked(boolean l) { this.islocked = l; }
}