package com.auction.server.network;
import java.util.concurrent.ConcurrentHashMap;
public class SessionManager {
    private static ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
    public static boolean isvalid(String hwid, String user) {
        boolean ans = true;
        if (sessions.containsKey(hwid) && !sessions.get(hwid).equals(user)) {
            ans = false;
        } else {
            sessions.put(hwid, user);
        }
        return ans;
    }
    public static void remove(String hwid) {
        sessions.remove(hwid);
    }
}