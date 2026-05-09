package com.auction.shared;

import java.io.Serializable;

public class LeaderboardEntry implements Serializable, Comparable<LeaderboardEntry> {
    private static final long serialVersionUID = 1L;
    private int userid;
    private String username;
    private String avatarurl;
    private double score;

    public LeaderboardEntry(int userid, String username, String avatarurl, double score) {
        this.userid = userid;
        this.username = username;
        this.avatarurl = avatarurl;
        this.score = score;
    }

    public int getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        int ans = Double.compare(other.score, this.score);
        if (ans == 0) {
            ans = Integer.compare(this.userid, other.userid);
        }
        return ans;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LeaderboardEntry ans = (LeaderboardEntry) obj;
        return userid == ans.userid;
    }

    @Override
    public int hashCode() {
        return userid;
    }
}