<<<<<<< HEAD
<<<<<<< HEAD
package com.auction.server.factory;
import com.auction.shared.*;
public class ItemFactory {
    public static Item createitem(String type) {
        Item ans = null;
        if (type.equalsIgnoreCase("electronics")) {
            ans = new Electronics();
        } else if (type.equalsIgnoreCase("art")) {
            ans = new Art();
        } else if (type.equalsIgnoreCase("vehicle")) {
            ans = new Vehicle();
        }
        return ans;
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
