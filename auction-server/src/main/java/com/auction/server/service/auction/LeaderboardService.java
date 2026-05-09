package com.auction.server.service.auction;

import com.auction.shared.LeaderboardEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LeaderboardService {
    private final ConcurrentHashMap<Integer, LeaderboardEntry> map = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<LeaderboardEntry> set = new ConcurrentSkipListSet<>();

    public synchronized void updatescore(int userid, String username, String avatarurl, double amount) {
        LeaderboardEntry res = map.get(userid);
        if (res != null) {
            set.remove(res);
            res.setScore(res.getScore() + amount);
            set.add(res);
        } else {
            LeaderboardEntry ans = new LeaderboardEntry(userid, username, avatarurl, amount);
            map.put(userid, ans);
            set.add(ans);
        }
    }

    public List<LeaderboardEntry> gettop(int limit) {
        List<LeaderboardEntry> ans = new ArrayList<>();
        for (LeaderboardEntry res : set) {
            ans.add(res);
            if (ans.size() >= limit) {
                break;
            }
        }
        return ans;
    }
}