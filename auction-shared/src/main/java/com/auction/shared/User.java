package com.auction.shared;

public abstract class User extends Entity {
  protected String username;
  protected String fullname;
  protected String password;
  protected String email;
  protected String age;
  protected String phonenumber;
  protected double balance;
  protected boolean isactive;
  protected boolean islocked;
  protected String avatarurl;
  protected double moneyspent;
  protected int itemsbought;
  protected double moneyreceived;
  protected int itemssold;

  public User() {}

  public User(String u, String p, String e, String a, String ph) {
    this.username = u;
    this.fullname = u;
    this.password = p;
    this.email = e;
    this.age = a;
    this.phonenumber = ph;
    this.balance = 0.0;
    this.moneyspent = 0.0;
    this.itemsbought = 0;
    this.moneyreceived = 0.0;
    this.itemssold = 0;
    this.isactive = true;
    this.islocked = false;
  }

  public abstract UserRole getrole();

  public String getusername() { return this.username; }
  public void setusername(String u) { this.username = u; }
  public String getfullname() { return this.fullname; }
  public void setfullname(String ans) { this.fullname = ans; }
  public String getpassword() { return this.password; }
  public void setpassword(String p) { this.password = p; }
  public String getemail() { return this.email; }
  public void setemail(String e) { this.email = e; }
  public String getage() { return this.age; }
  public void setage(String a) { this.age = a; }
  public String getphonenumber() { return this.phonenumber; }
  public void setphonenumber(String ph) { this.phonenumber = ph; }
  public double getbalance() { return this.balance; }
  public void setbalance(double b) { this.balance = b; }
  public boolean isactive() { return this.isactive; }
  public void setactive(boolean a) { this.isactive = a; }
  public boolean islocked() { return this.islocked; }
  public void setlocked(boolean l) { this.islocked = l; }
  public String getavatarurl() { return avatarurl; }
  public void setavatarurl(String ans) { this.avatarurl = ans; }
  public double getmoneyspent() { return this.moneyspent; }
  public void setmoneyspent(double m) { this.moneyspent = m; }
  public int getitemsbought() { return this.itemsbought; }
  public void setitemsbought(int i) { this.itemsbought = i; }
  public double getmoneyreceived() { return this.moneyreceived; }
  public void setmoneyreceived(double m) { this.moneyreceived = m; }
  public int getitemssold() { return this.itemssold; }
  public void setitemssold(int i) { this.itemssold = i; }
}